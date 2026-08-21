package com.proga.ai_service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proga.ai_service.client.WorkspaceClient;
import com.proga.ai_service.dto.*;
import com.proga.ai_service.exception.AiProcessingException;
import com.proga.ai_service.exception.ResourceNotFoundException;
import com.proga.ai_service.model.AgentType;
import com.proga.ai_service.model.AiChatMessage;
import com.proga.ai_service.model.AiThread;
import com.proga.ai_service.model.SenderType;
import com.proga.ai_service.repository.AiChatMessageRepository;
import com.proga.ai_service.repository.AiThreadRepository;
import com.proga.ai_service.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiThreadRepository threadRepository;
    private final AiChatMessageRepository messageRepository;
    private final WorkspaceClient workspaceClient;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private ChatModel chatModel;

    @Override
    @Transactional
    public AiThreadResponse getOrCreateThread(long spaceId, AgentType agentType) {
        AiThread thread = threadRepository.findBySpaceIdAndAgentType(spaceId, agentType)
                .orElseGet(() -> threadRepository.save(
                        AiThread.builder()
                                .spaceId(spaceId)
                                .agentType(agentType)
                                .openaiThreadId("thread_" + UUID.randomUUID().toString())
                                .build()
                ));
        return mapToThreadResponse(thread);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiThreadResponse> getThreadsBySpace(long spaceId) {
        return threadRepository.findBySpaceId(spaceId).stream()
                .map(this::mapToThreadResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiChatMessageResponse> getThreadMessages(long threadId) {
        if (!threadRepository.existsById(threadId)) {
            throw new ResourceNotFoundException("AiThread not found with id: " + threadId);
        }
        return messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId).stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AiChatMessageResponse chat(ChatRequest request) {
        AiThreadResponse thread = getOrCreateThread(request.getSpaceId(), request.getAgentType());

        // Save User Message
        AiChatMessage userMsg = messageRepository.save(AiChatMessage.builder()
                .threadId(thread.getId())
                .senderType(SenderType.USER)
                .messageContent(request.getPrompt())
                .build());

        // Construct System Context based on Agent Type
        String systemPromptStr = buildSystemPrompt(request.getAgentType());
        String fullUserPrompt = request.getPrompt();

        // Optional task context
        if (request.getTaskId() != null) {
            try {
                ApiResponse<TaskDto> taskResp = workspaceClient.getTaskById(request.getTaskId());
                if (taskResp != null && taskResp.getData() != null) {
                    TaskDto t = taskResp.getData();
                    fullUserPrompt += String.format("\n\n[Context Task details: Title='%s', Status='%s', Priority='%s', Desc='%s']",
                            t.getTitle(), t.getStatus(), t.getPriority(), t.getDescription());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch task context via Feign client: {}", e.getMessage());
            }
        }

        String assistantReply = callAiModel(systemPromptStr, fullUserPrompt);

        // Save Assistant Message
        AiChatMessage assistantMsg = messageRepository.save(AiChatMessage.builder()
                .threadId(thread.getId())
                .senderType(SenderType.ASSISTANT)
                .messageContent(assistantReply)
                .build());

        return mapToMessageResponse(assistantMsg);
    }

    private List<Map<String, Object>> ragSamples = new ArrayList<>();

    @jakarta.annotation.PostConstruct
    public void initRagKnowledgeBase() {
        try (InputStream is = getClass().getResourceAsStream("/rag/task_decomposition_samples.json")) {
            if (is != null) {
                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                ragSamples = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
                log.info("Successfully loaded {} RAG Task Decomposition sample datasets into AI-Service memory.", ragSamples.size());
            } else {
                log.warn("RAG sample dataset file /rag/task_decomposition_samples.json not found.");
            }
        } catch (Exception e) {
            log.error("Failed to load RAG sample dataset: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> findBestMatchingRagSample(String userRequirement) {
        if (ragSamples.isEmpty()) return null;
        if (userRequirement == null || userRequirement.isBlank()) return null;

        String reqLower = userRequirement.toLowerCase();
        Map<String, Object> bestSample = null;
        int maxScore = 0;

        for (Map<String, Object> sample : ragSamples) {
            String domain = (String) sample.getOrDefault("domain", "");
            String title = (String) sample.getOrDefault("title", "");
            String reqText = (String) sample.getOrDefault("requirementText", "");
            String combinedText = (domain + " " + title + " " + reqText).toLowerCase();

            int score = 0;
            String[] keywords = reqLower.split("\\s+");
            for (String kw : keywords) {
                if (kw.length() > 2 && combinedText.contains(kw)) {
                    score += 1;
                }
            }

            if (score > maxScore) {
                maxScore = score;
                bestSample = sample;
            }
        }

        if (bestSample != null) {
            log.info("RAG Similarity Search selected sample: '{}' (Score: {}) for requirement: '{}'",
                    bestSample.get("title"), maxScore, userRequirement);
        } else {
            log.info("RAG Similarity Search found no direct static sample match for: '{}'. Proceeding with AI prompt engineering.", userRequirement);
        }
        return bestSample;
    }

    @Override
    @Transactional
    public TaskDecompositionResponse decomposeRequirements(TaskDecompositionRequest request) {
        AiThread thread;
        if (request.getThreadId() != null && request.getThreadId() > 0) {
            thread = threadRepository.findById(request.getThreadId())
                    .orElseGet(() -> threadRepository.save(
                            AiThread.builder()
                                    .spaceId(request.getSpaceId())
                                    .agentType(AgentType.REQUIREMENT)
                                    .openaiThreadId("thread_" + UUID.randomUUID().toString())
                                    .build()
                    ));
        } else {
            // Always create a BRAND NEW thread when starting a new conversation session
            thread = threadRepository.save(
                    AiThread.builder()
                            .spaceId(request.getSpaceId())
                            .agentType(AgentType.REQUIREMENT)
                            .openaiThreadId("thread_" + UUID.randomUUID().toString())
                            .build()
            );
        }

        // Fetch existing message history in this specific thread BEFORE saving current prompt
        List<AiChatMessage> existingMsgs = messageRepository.findByThreadIdOrderByCreatedAtAsc(thread.getId());
        boolean isFirstTurn = existingMsgs.isEmpty();

        // Save User Prompt into Thread
        messageRepository.save(AiChatMessage.builder()
                .threadId(thread.getId())
                .senderType(SenderType.USER)
                .messageContent(request.getRequirementText())
                .build());

        // Perform RAG Similarity Retrieval
        Map<String, Object> matchedRagSample = findBestMatchingRagSample(request.getRequirementText());
        String sampleJsonContext = "";
        if (matchedRagSample != null) {
            try {
                sampleJsonContext = objectMapper.writeValueAsString(matchedRagSample);
            } catch (Exception ignored) {}
        }

        // Extract source references from RAG sample
        String ragSourceRef = matchedRagSample != null ? (String) matchedRagSample.getOrDefault("sourceReference", "Quy trình Đồ án Khoa CNTT IUH & IEEE Std 12207") : "PMBOK 7th Edition Agile Standards";
        String ragSourceUrl = matchedRagSample != null ? (String) matchedRagSample.getOrDefault("sourceUrl", "https://fit.iuh.edu.vn/") : "https://www.atlassian.com/agile/project-management/work-breakdown-structure";

        String systemPrompt;
        if (isFirstTurn) {
            // Turn 1: Clarification & Interview Phase (DO NOT GENERATE TASKS YET!)
            systemPrompt = String.format("""
                Bạn là một Requirement Agent (Product Owner / Business Analyst Co-Pilot) chuyên nghiệp cho hệ thống PROGA.
                ĐÂY LÀ LƯỢT ĐÀM THOẠI ĐẦU TIÊN để làm rõ phạm vi bài toán với người dùng.
                Nhiệm vụ của bạn:
                1. Đọc kỹ mô tả bài toán CỦA NGƯỜI DÙNG và phân tích các góc độ nghiệp vụ/kỹ thuật.
                2. Đưa ra 3-4 CÂU HỎI PHỎNG VẤN ĐÀM THOẠI ĐƯỢC THIẾT KẾ DÀNH RIÊNG CHO CHÍNH BÀI TOÁN ĐÓ trong trường 'summary' (Ví dụ: Nếu bài toán là Quản lý Nhà hàng thì hỏi về gọi món QR Code/Màn hình bếp/POS; Nếu là Y tế thì hỏi về WebRTC Video call/Mã hóa bệnh án AES-256; Nếu là Đồ án IUH thì hỏi về quy trình duyệt đề tài; Hỏi thêm về phân công nhân sự Nam/Linh/Tuấn nếu chưa rõ).
                3. BẮT BUỘC TRẢ VỀ MẢNG 'tasks': [] RỖNG NGUYÊN BẢN (KHÔNG TẠO BẤT KỲ TASK NÀO Ở LƯỢT NÀY!).
                
                YÊU CẦU ĐỊNH DẠNG ĐẦU RA STRICT JSON:
                {
                  "summary": "Lời chào và 3-4 câu hỏi phỏng vấn nghiệp vụ được thiết kế DÀNH RIÊNG cho bài toán của người dùng",
                  "sourceReference": "%s",
                  "sourceUrl": "%s",
                  "tasks": []
                }
                """, ragSourceRef, ragSourceUrl);
        } else {
            // Turn 2+: Task Breakdown Phase (User has provided context / answered questions)
            systemPrompt = String.format("""
                Bạn là một Requirement Agent (Product Owner / Business Analyst) chuyên nghiệp cho hệ thống PROGA.
                CẢNH BÁO TỐI CAO: BẠN BẮT BUỘC BÓC TÁCH TASK DỰA TRÊN CHÍNH NGÀNH NGHỀ BÀI TOÁN CỦA NGƯỜI DÙNG.
                NẾU YÊU CẦU LÀ Y TẾ / TELEHEALTH / BÁC SĨ / BỆNH NHÂN / BỆNH ÁN ĐIỆN TỬ, BẠN BẮT BUỘC PHẢI TẠO CÁC TASK VỀ Y TẾ VÀ TELEHEALTH (Ví dụ: WebRTC Video Call, Đặt lịch khám bác sĩ, Đơn thuốc điện tử mã hóa AES-256). TUYỆT ĐỐI KHÔNG ĐƯỢC TẠO CÁC TASK VỀ BÁN HÀNG / E-COMMERCE!
                
                Người dùng đã đàm thoại và bổ sung chi tiết yêu cầu. Bây giờ hãy bóc tách danh sách từ 10 - 25 Task cụ thể theo Sprint 1 tuần (5-7 ngày làm việc).
                NẾU TRONG NỘI DUNG NÊU TÊN THÀNH VIÊN VÀ VAI TRÒ (ví dụ: 'Nam làm Backend, Linh làm Frontend, Tuấn làm QA'), BẠN BẮT BUỘC ĐIỀN TÊN THÀNH VIÊN ĐÓ VÀO TRƯỜNG suggestedMemberName.
                
                ĐÂY LÀ MẪU DỮ LIỆU TRI THỨC RAG TƯƠNG ĐỒNG CHỈ DÙNG THAM KHẢO CẤU TRÚC JSON (%s):
                %s
                
                YÊU CẦU ĐỊNH DẠNG ĐẦU RA STRICT JSON:
                {
                  "summary": "Tóm tắt ngắn gọn việc bóc tách danh sách WBS Tasks dựa trên đàm thoại",
                  "sourceReference": "%s",
                  "sourceUrl": "%s",
                  "tasks": [
                    {
                      "sprint": "Sprint 1: Tên Sprint",
                      "title": "Tên task ngắn gọn rõ ràng",
                      "description": "Mô tả công việc chi tiết",
                      "priority": "HIGH / MEDIUM / LOW / URGENT",
                      "estimatedDays": 3,
                      "bufferDays": 1,
                      "assignedRole": "Backend Developer / Frontend Developer / DevOps / QA Lead / Tech Lead / BA",
                      "suggestedMemberName": "Tên thành viên nếu người dùng nêu tên (ví dụ: Nam / Linh)",
                      "riskWarning": "Cảnh báo rủi ro ngắn gọn (Chỉ điền nếu priority là URGENT hoặc HIGH, để null nếu bình thường)"
                    }
                  ]
                }
                """, ragSourceRef, sampleJsonContext, ragSourceRef, ragSourceUrl);
        }

        String userPrompt = "Nội dung người dùng gửi:\n" + request.getRequirementText();
        String rawResponse = callAiModel(systemPrompt, userPrompt);

        TaskDecompositionResponse responseObj;
        String jsonPayloadStr;

        try {
            // Clean markdown blocks if present (```json ... ```)
            String cleanedJson = extractJson(rawResponse);
            Map<String, Object> parsed = objectMapper.readValue(cleanedJson, new TypeReference<Map<String, Object>>() {});
            
            String summary = (String) parsed.getOrDefault("summary", "Đã phân rã yêu cầu thành công");
            String respSourceRef = (String) parsed.getOrDefault("sourceReference", ragSourceRef);
            String respSourceUrl = (String) parsed.getOrDefault("sourceUrl", ragSourceUrl);
            List<TaskDecompositionResponse.DecomposedTaskItem> taskItems;

            if (isFirstTurn) {
                // TURN 1 GUARANTEE: Strictly return empty tasks array, forcing AI to ask clarifying questions first!
                taskItems = Collections.emptyList();
                if (summary == null || summary.isBlank() || summary.contains("Đã phân rã")) {
                    summary = "Chào bạn! Tôi là Requirement Agent (PO/BA). Để hỗ trợ bóc tách danh sách WBS Tasks chính xác nhất cho dự án của bạn, tôi cần làm rõ 3 thông tin sau:\n" +
                              "1. Đội ngũ phát triển của bạn gồm bao nhiêu người và phân vai ra sao (ví dụ: Ai làm Backend, Frontend, QA)?\n" +
                              "2. Dự án có tiêu chuẩn bảo mật/thanh toán hoặc tích hợp bên thứ ba nào đặc thù không (ví dụ: VNPay, MoMo, OAuth2)?\n" +
                              "3. Thời gian triển khai dự kiến hoặc số lượng Sprint bạn kỳ vọng là bao nhiêu?\n\n" +
                              "👉 Bạn vui lòng nhắn tin phản hồi lại các thông tin trên trong khung chat bên dưới để tôi bắt đầu bóc tách danh sách Tasks nhé!";
                }
            } else {
                List<Map<String, Object>> tasksRaw = (List<Map<String, Object>>) parsed.getOrDefault("tasks", Collections.emptyList());
                taskItems = tasksRaw.stream().map(t -> 
                    TaskDecompositionResponse.DecomposedTaskItem.builder()
                            .sprint((String) t.getOrDefault("sprint", "Sprint 1"))
                            .title((String) t.get("title"))
                            .description((String) t.get("description"))
                            .priority((String) t.getOrDefault("priority", "MEDIUM"))
                            .estimatedDays(t.get("estimatedDays") != null ? ((Number) t.get("estimatedDays")).intValue() : 2)
                            .bufferDays(t.get("bufferDays") != null ? ((Number) t.get("bufferDays")).intValue() : 0)
                            .assignedRole((String) t.getOrDefault("assignedRole", (String) t.getOrDefault("recommendedRole", "Backend Developer")))
                            .suggestedMemberName((String) t.get("suggestedMemberName"))
                            .riskWarning((String) t.get("riskWarning"))
                            .build()
                ).collect(Collectors.toList());
            }

            responseObj = TaskDecompositionResponse.builder()
                    .threadId(thread.getId())
                    .summary(summary)
                    .sourceReference(respSourceRef)
                    .sourceUrl(respSourceUrl)
                    .tasks(taskItems)
                    .build();

            jsonPayloadStr = objectMapper.writeValueAsString(taskItems);

        } catch (Exception e) {
            log.error("Error parsing AI JSON response, generating RAG fallback structure: {}", e.getMessage());
            
            // Rich RAG Fallback Response using matched sample
            List<TaskDecompositionResponse.DecomposedTaskItem> fallbackItems = new ArrayList<>();
            String fallbackSummary = "Đã phân rã bài toán dựa trên kho tri thức RAG";

            if (matchedRagSample != null && matchedRagSample.containsKey("tasks")) {
                fallbackSummary = (String) matchedRagSample.getOrDefault("summary", fallbackSummary);
                List<Map<String, Object>> sampleTasks = (List<Map<String, Object>>) matchedRagSample.get("tasks");
                for (Map<String, Object> st : sampleTasks) {
                    fallbackItems.add(TaskDecompositionResponse.DecomposedTaskItem.builder()
                            .sprint((String) st.getOrDefault("sprint", "Sprint 1"))
                            .title((String) st.get("title"))
                            .description((String) st.get("description"))
                            .priority((String) st.getOrDefault("priority", "HIGH"))
                            .estimatedDays(st.get("estimatedDays") != null ? ((Number) st.get("estimatedDays")).intValue() : 2)
                            .bufferDays(st.get("bufferDays") != null ? ((Number) st.get("bufferDays")).intValue() : 0)
                            .assignedRole((String) st.getOrDefault("assignedRole", "Backend Developer"))
                            .riskWarning((String) st.get("riskWarning"))
                            .build());
                }
            } else {
                fallbackItems.add(TaskDecompositionResponse.DecomposedTaskItem.builder()
                        .sprint("Sprint 1")
                        .title("Phân tích & Thiết kế Schema Cơ sở dữ liệu")
                        .description("Tạo sơ đồ ERD và DDL cho các bảng trong hệ thống")
                        .priority("URGENT")
                        .estimatedDays(3)
                        .bufferDays(1)
                        .assignedRole("Database Architect")
                        .riskWarning("Cần kiểm tra kỹ ràng buộc để tránh lỗi về sau")
                        .build());
            }

            responseObj = TaskDecompositionResponse.builder()
                    .threadId(thread.getId())
                    .summary(fallbackSummary)
                    .sourceReference(ragSourceRef)
                    .sourceUrl(ragSourceUrl)
                    .tasks(fallbackItems)
                    .build();

            try {
                jsonPayloadStr = objectMapper.writeValueAsString(fallbackItems);
            } catch (Exception ignored) {
                jsonPayloadStr = "[]";
            }
        }

        // Save Assistant Response
        messageRepository.save(AiChatMessage.builder()
                .threadId(thread.getId())
                .senderType(SenderType.ASSISTANT)
                .messageContent(responseObj.getSummary() + " - Đã tạo " + responseObj.getTasks().size() + " tasks.")
                .jsonPayload(jsonPayloadStr)
                .build());

        return responseObj;
    }

    @Override
    @Transactional
    public AiChatMessageResponse analyzePmProgressAndRisk(long spaceId) {
        AiThreadResponse thread = getOrCreateThread(spaceId, AgentType.PM);

        List<TaskDto> tasks = Collections.emptyList();
        SpaceDto space = null;

        try {
            ApiResponse<SpaceDto> spaceResp = workspaceClient.getSpaceById(spaceId);
            if (spaceResp != null && spaceResp.getData() != null) {
                space = spaceResp.getData();
            }
            ApiResponse<List<TaskDto>> tasksResp = workspaceClient.getTasksBySpace(spaceId);
            if (tasksResp != null && tasksResp.getData() != null) {
                tasks = tasksResp.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch space/task information via Feign client: {}", e.getMessage());
        }

        long total = tasks.size();
        long done = tasks.stream().filter(t -> "DONE".equalsIgnoreCase(t.getStatus())).count();
        long inProgress = tasks.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long todo = tasks.stream().filter(t -> "TODO".equalsIgnoreCase(t.getStatus())).count();
        long review = tasks.stream().filter(t -> "REVIEW".equalsIgnoreCase(t.getStatus())).count();

        String spaceName = space != null ? space.getName() : "Space #" + spaceId;

        String systemPrompt = """
                Bạn là PM Agent (Project Manager & Risk Advisor) chuyên nghiệp cho hệ thống PROGA.
                Nhiệm vụ của bạn là tóm tắt tiến độ công việc trong Space, phát hiện rủi ro trễ deadline, và đề xuất giải pháp xử lý.
                """;

        String userPrompt = String.format("""
                Hãy phân tích tiến độ cho Space '%s' (ID: %d):
                - Tổng số công việc: %d
                - Đã hoàn thành (DONE): %d
                - Đang thực hiện (IN_PROGRESS): %d
                - Đang chờ duyệt (REVIEW): %d
                - Cần làm (TODO): %d
                
                Chi tiết danh sách công việc:
                %s
                
                Yêu cầu báo cáo:
                1. Tóm tắt tỷ lệ hoàn thành dự án.
                2. Phân tích rủi ro trễ tiến độ (nếu số công việc TODO / IN_PROGRESS còn nhiều).
                3. Đề xuất hành động cho PM để đẩy nhanh tiến độ.
                """,
                spaceName, spaceId, total, done, inProgress, review, todo,
                tasks.stream().map(t -> String.format("- [%s] %s (Trạng thái: %s, Ưu tiên: %s)", t.getId(), t.getTitle(), t.getStatus(), t.getPriority()))
                        .collect(Collectors.joining("\n"))
        );

        // Save User Request
        messageRepository.save(AiChatMessage.builder()
                .threadId(thread.getId())
                .senderType(SenderType.USER)
                .messageContent("Yêu cầu tóm tắt tiến độ và đánh giá rủi ro cho Space ID: " + spaceId)
                .build());

        String analysisResult = callAiModel(systemPrompt, userPrompt);

        // Save Assistant Message
        AiChatMessage assistantMsg = messageRepository.save(AiChatMessage.builder()
                .threadId(thread.getId())
                .senderType(SenderType.ASSISTANT)
                .messageContent(analysisResult)
                .build());

        return mapToMessageResponse(assistantMsg);
    }

    @Override
    @Transactional
    public AiChatMessageResponse getTechnicalAdvice(long taskId, String problemDescription) {
        TaskDto task = null;
        try {
            ApiResponse<TaskDto> taskResp = workspaceClient.getTaskById(taskId);
            if (taskResp != null && taskResp.getData() != null) {
                task = taskResp.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch task information via Feign client: {}", e.getMessage());
        }

        long spaceId = task != null ? task.getSpaceId() : 1L;
        AiThreadResponse thread = getOrCreateThread(spaceId, AgentType.TECHNICAL_ADVISOR);

        String systemPrompt = """
                Bạn là Technical Advisor Agent (Principal Technical Architect / Tech Lead) trong hệ thống PROGA.
                Nhiệm vụ của bạn là tư vấn kiến trúc kỹ thuật, thiết kế giải pháp code, cấu hình thư viện, và hướng dẫn sửa lỗi / bug cho công việc.
                """;

        String userPrompt = String.format("""
                Công việc ID: %d
                Tên công việc: %s
                Mô tả công việc: %s
                Vấn đề kỹ thuật / Bug gặp phải: %s
                
                Hãy cung cấp:
                1. Phân tích nguyên nhân kỹ thuật có thể xảy ra.
                2. Hướng xử lý từng bước (Step-by-step resolution).
                3. Gợi ý cấu hình / đoạn code mẫu (nếu có).
                """,
                taskId,
                task != null ? task.getTitle() : "N/A",
                task != null ? task.getDescription() : "N/A",
                problemDescription
        );

        // Save User Message
        messageRepository.save(AiChatMessage.builder()
                .threadId(thread.getId())
                .senderType(SenderType.USER)
                .messageContent("Tư vấn kỹ thuật cho Task #" + taskId + ": " + problemDescription)
                .build());

        String adviceResult = callAiModel(systemPrompt, userPrompt);

        // Save Assistant Message
        AiChatMessage assistantMsg = messageRepository.save(AiChatMessage.builder()
                .threadId(thread.getId())
                .senderType(SenderType.ASSISTANT)
                .messageContent(adviceResult)
                .build());

        return mapToMessageResponse(assistantMsg);
    }

    private String callAiModel(String systemPrompt, String userPrompt) {
        if (chatModel != null) {
            try {
                Prompt prompt = new Prompt(List.of(
                        new SystemMessage(systemPrompt),
                        new UserMessage(userPrompt)
                ));
                return chatModel.call(prompt).getResult().getOutput().getText();
            } catch (Exception e) {
                log.error("Call to Spring AI ChatModel failed: {}. Falling back to default response generator.", e.getMessage());
            }
        }

        // Standard Intelligent Fallback Engine
        return generateMockAiResponse(systemPrompt, userPrompt);
    }

    private String generateMockAiResponse(String systemPrompt, String userPrompt) {
        if (systemPrompt.contains("Requirement Agent")) {
            if (systemPrompt.contains("LƯỢT ĐÀM THOẠI ĐẦU TIÊN")) {
                String promptLower = userPrompt.toLowerCase();
                String dynamicQuestions;

                if (promptLower.contains("nhà hàng") || promptLower.contains("quản lý bàn") || promptLower.contains("thực đơn") || promptLower.contains("gọi món")) {
                    dynamicQuestions = "Chào bạn! Tôi là Requirement Agent (PO/BA) chuyên trách bài toán QUẢN LÝ NHÀ HÀNG. Để hỗ trợ bóc tách danh sách WBS Tasks chính xác và sát thực tế nhất cho dự án của bạn, tôi cần trao đổi 3 câu hỏi nghiệp vụ sau:\\n" +
                                       "1. Mô hình gọi món tại nhà hàng của bạn là gì (Khách hàng tự quét mã QR tại bàn, Nhân viên phục vụ cầm Tablet ghi món, hay Gọi món tại quầy thu ngân)?\\n" +
                                       "2. Hệ thống có yêu cầu màn hình Bếp (Kitchen Display System) đẩy đơn thời gian thực và tự động in hóa đơn VNPAY tại bàn không?\\n" +
                                       "3. Đội ngũ phát triển của bạn có bao nhiêu thành viên và phân công vai trò cụ thể thế nào (ví dụ: Nam làm Backend, Linh làm Frontend, Tuấn làm QA)?\\n\\n" +
                                       "👉 Bạn vui lòng nhắn tin trả lời các thông tin trên trong khung chat bên dưới để tôi bắt đầu bóc tách Bảng Task chuẩn nhé!";
                } else if (promptLower.contains("y tế") || promptLower.contains("telehealth") || promptLower.contains("bác sĩ") || promptLower.contains("bệnh nhân") || promptLower.contains("khám")) {
                    dynamicQuestions = "Chào bạn! Tôi là Requirement Agent (PO/BA) chuyên trách bài toán Y TẾ SỐ & TELEHEALTH. Để bóc tách WBS Tasks chuẩn xác cho dự án Y tế của bạn, tôi cần trao đổi 3 câu hỏi sau:\\n" +
                                       "1. Tính năng tư vấn khám từ xa có yêu cầu phòng gọi Video Call WebRTC 1-1 trực tiếp giữa Bác sĩ & Bệnh nhân kết hợp chat gửi file xét nghiệm không?\\n" +
                                       "2. Hồ sơ bệnh án điện tử (EHR) có yêu cầu mã hóa bảo mật AES-256 theo Thông tư 46/2018/TT-BYT và gửi tin nhắn Zalo ZNS/Email nhắc lịch uống thuốc không?\\n" +
                                       "3. Đội ngũ của bạn gồm những ai đảm nhiệm vai trò gì và bạn kỳ vọng hoàn thành trong bao nhiêu Sprint?\\n\\n" +
                                       "👉 Bạn vui lòng nhắn tin phản hồi trong khung chat bên dưới để tôi khởi tạo danh sách Tasks nhé!";
                } else if (promptLower.contains("đồ án") || promptLower.contains("khóa luận") || promptLower.contains("iuh")) {
                    dynamicQuestions = "Chào bạn! Tôi là Requirement Agent (PO/BA) phụ trách dự án QUẢN LÝ ĐỒ ÁN KHÓA LUẬN. Để bóc tách WBS Tasks chuẩn quy trình Khoa CNTT - IUH, tôi cần trao đổi 3 câu hỏi:\\n" +
                                       "1. Quy trình đăng ký và duyệt đề tài có qua Trưởng bộ môn phê duyệt và phân công Giảng viên phản biện không?\\n" +
                                       "2. Sinh viên nộp báo cáo tiến độ tuần có cần giới hạn file PDF/DOCX và chấm điểm điện tử trực tiếp trên hệ thống không?\\n" +
                                       "3. Đội ngũ phát triển dự án của bạn gồm những ai đảm nhiệm vai trò gì?\\n\\n" +
                                       "👉 Bạn vui lòng nhắn tin phản hồi trong khung chat để tôi bắt đầu bóc tách Tasks nhé!";
                } else {
                    dynamicQuestions = "Chào bạn! Tôi là Requirement Agent (PO/BA) của hệ thống PROGA. Tôi đã nhận được bài toán phát triển của bạn. Để hỗ trợ bóc tách danh sách WBS Tasks chính xác và phù hợp nhất với dự án, bạn vui lòng cho tôi biết thêm 3 thông tin sau:\\n" +
                                       "1. Đội ngũ phát triển của bạn gồm bao nhiêu người và phân vai ra sao (vd: Ai làm Backend, Frontend, QA)?\\n" +
                                       "2. Hệ thống có yêu cầu bảo mật, thanh toán hoặc tích hợp bên thứ ba nào đặc thù không (vd: VNPay, MoMo, OAuth2, WebRTC)?\\n" +
                                       "3. Bạn dự kiến triển khai dự án trong bao nhiêu Sprint hoặc thời gian là bao nhiêu lâu?\\n\\n" +
                                       "👉 Bạn vui lòng nhắn tin phản hồi lại các thông tin trên trong khung chat để tôi bắt đầu bóc tách danh sách Tasks nhé!";
                }

                return String.format("""
                    {
                      "summary": "%s",
                      "sourceReference": "PMBOK 7th Edition Agile Standards",
                      "sourceUrl": "https://www.atlassian.com/agile/project-management/work-breakdown-structure",
                      "tasks": []
                    }
                    """, dynamicQuestions);
            }

            String promptLower = userPrompt.toLowerCase();

            // Domain: Restaurant Management (Nhà hàng, Gọi món, Thực đơn, Đặt bàn)
            if (promptLower.contains("nhà hàng") || promptLower.contains("quản lý bàn") || promptLower.contains("thực đơn") || promptLower.contains("gọi món") || promptLower.contains("pos")) {
                return """
                    {
                      "summary": "Phân rã hệ thống Quản lý Nhà hàng & Gọi món tại bàn thành các WBS Tasks chuẩn theo Sprint.",
                      "sourceReference": "Enterprise POS & Restaurant Architecture Standards",
                      "sourceUrl": "https://www.atlassian.com/agile/project-management/work-breakdown-structure",
                      "tasks": [
                        {
                          "sprint": "Sprint 1",
                          "title": "Thiết kế Schema Database Bàn ăn, Thực đơn & Đơn món",
                          "description": "Xây dựng DDL các bảng tables, categories, menu_items, order_bills, payment_transactions.",
                          "priority": "URGENT",
                          "estimatedDays": 3,
                          "bufferDays": 1,
                          "assignedRole": "Database Architect",
                          "riskWarning": "Cần thiết kế khóa ngoại chặt chẽ giữa đơn món và bàn ăn để tránh xung đột trạng thái bàn."
                        },
                        {
                          "sprint": "Sprint 1",
                          "title": "Xây dựng Module Quản lý Sơ Đồ Bàn Ăn & Trạng Thái Bàn Trực Tuyến",
                          "description": "Màn hình sơ đồ bàn ăn thời gian thực theo khu vực (Tầng 1, Tầng 2, VIP).",
                          "priority": "URGENT",
                          "estimatedDays": 4,
                          "bufferDays": 1,
                          "assignedRole": "Fullstack Developer"
                        },
                        {
                          "sprint": "Sprint 2",
                          "title": "Phát triển Chức năng Khách Hàng Quét Mã QR Tại Bàn Để Gọi Món",
                          "description": "Khách hàng quét mã QR dán tại bàn để xem Menu thực đơn và bấm chọn món.",
                          "priority": "HIGH",
                          "estimatedDays": 4,
                          "bufferDays": 1,
                          "assignedRole": "Frontend Developer"
                        },
                        {
                          "sprint": "Sprint 2",
                          "title": "Tích hợp Module POS Bán Hàng Cho Phục Vụ & Màn Hình Bếp (Kitchen Display)",
                          "description": "Đơn món gửi từ bàn lập tức đẩy sang màn hình Bếp/Quầy pha chế theo WebSocket real-time.",
                          "priority": "HIGH",
                          "estimatedDays": 4,
                          "bufferDays": 1,
                          "assignedRole": "Backend Developer"
                        },
                        {
                          "sprint": "Sprint 3",
                          "title": "Tích hợp Cổng Thanh Toán VNPAY IPN Callback & Xuất Hóa Đơn Điện Tử",
                          "description": "Khách hàng thanh toán tiền ăn tại bàn qua VNPAY QR và tự động in hóa đơn.",
                          "priority": "HIGH",
                          "estimatedDays": 3,
                          "bufferDays": 0,
                          "assignedRole": "Security & Backend Developer"
                        }
                      ]
                    }
                    """;
            }

            // Domain: Healthtech / Telehealth (Y tế, Bác sĩ, Bệnh nhân, Khám bệnh)
            if (promptLower.contains("y tế") || promptLower.contains("telehealth") || promptLower.contains("bác sĩ") || promptLower.contains("bệnh nhân") || promptLower.contains("khám")) {
                return """
                    {
                      "summary": "Phân rã hệ thống Y tế Số, Tư vấn Khám Bệnh Từ Xa Telehealth WebRTC và Hồ sơ Bệnh án Điện tử EHR.",
                      "sourceReference": "Bộ Y tế Việt Nam - Thông tư 46/2018/TT-BYT về Hồ sơ Bệnh án Điện tử",
                      "sourceUrl": "https://moh.gov.vn/",
                      "tasks": [
                        {
                          "sprint": "Sprint 1",
                          "title": "Thiết kế Schema Database Bác sĩ, Bệnh nhân, Lịch khám & Bệnh án Điện tử EHR",
                          "description": "Xây dựng bảng doctors, patients, appointment_slots, medical_records, prescriptions.",
                          "priority": "URGENT",
                          "estimatedDays": 3,
                          "bufferDays": 1,
                          "assignedRole": "Database Architect",
                          "riskWarning": "Dữ liệu bệnh án cá nhân (chẩn đoán, tiền sử bệnh) phải mã hóa AES-256 trước khi lưu DB."
                        },
                        {
                          "sprint": "Sprint 1",
                          "title": "Module Đặt Lịch Hẹn Khám Bác Sĩ & Thanh Toán Tiền Khám VNPAY",
                          "description": "Bệnh nhân tìm bác sĩ theo chuyên khoa, chọn khung giờ trống và thanh toán đặt chỗ VNPAY.",
                          "priority": "URGENT",
                          "estimatedDays": 4,
                          "bufferDays": 1,
                          "assignedRole": "Backend Developer"
                        },
                        {
                          "sprint": "Sprint 2",
                          "title": "Module Tư Vấn Khám Từ Xa Telehealth Video Call WebRTC 1-1",
                          "description": "Phòng gọi Video trực tiếp 1-1 giữa Bác sĩ và Bệnh nhân trên WebRTC kết hợp Chat gửi file xét nghiệm.",
                          "priority": "HIGH",
                          "estimatedDays": 4,
                          "bufferDays": 1,
                          "assignedRole": "Fullstack Developer"
                        },
                        {
                          "sprint": "Sprint 2",
                          "title": "Module Kê Đơn Thuốc Điện Tử & Mã Hóa AES-256 Hồ Sơ Bệnh Án Điện Tử",
                          "description": "Bác sĩ tạo đơn thuốc điện tử trong ca khám và mã hóa lưu trữ hồ sơ bệnh án bệnh nhân an toàn.",
                          "priority": "HIGH",
                          "estimatedDays": 3,
                          "bufferDays": 0,
                          "assignedRole": "Security Specialist"
                        },
                        {
                          "sprint": "Sprint 3",
                          "title": "Tích hợp Service Nhắc Lịch Uống Thuốc Hàng Ngày qua Zalo ZNS & Email",
                          "description": "Cron job tự động quét đơn thuốc và gửi tin nhắn Zalo ZNS / Email nhắc bệnh nhân uống thuốc đúng giờ.",
                          "priority": "MEDIUM",
                          "estimatedDays": 2,
                          "bufferDays": 0,
                          "assignedRole": "Backend Developer"
                        }
                      ]
                    }
                    """;
            }

            // Generic SaaS System Fallback
            return """
                {
                  "summary": "Phân rã bài toán phần mềm theo tiêu chuẩn Kiến trúc Microservices & Agile Scrum.",
                  "sourceReference": "PMBOK 7th Edition Agile Standards",
                  "sourceUrl": "https://www.atlassian.com/agile/project-management/work-breakdown-structure",
                  "tasks": [
                    {
                      "sprint": "Sprint 1",
                      "title": "Phân tích Yêu cầu Nghiệp vụ & Thiết kế Schema Cơ sở Dữ liệu",
                      "description": "Xây dựng biểu đồ ERD và viết DDL khởi tạo các bảng trong hệ thống.",
                      "priority": "URGENT",
                      "estimatedDays": 3,
                      "bufferDays": 1,
                      "assignedRole": "System Architect"
                    },
                    {
                      "sprint": "Sprint 1",
                      "title": "Triển khai Auth-Service & Phân Quyền Bảo Mật JWT RBAC",
                      "description": "Tạo API Đăng nhập, Đăng ký, Quên mật khẩu và cấp Token JWT an toàn.",
                      "priority": "HIGH",
                      "estimatedDays": 3,
                      "bufferDays": 0,
                      "assignedRole": "Backend Developer"
                    },
                    {
                      "sprint": "Sprint 2",
                      "title": "Xây dựng Giao diện Dashboard Quản Trị Trực Quan",
                      "description": "Thiết kế các biểu đồ thống kê chỉ số nghiệp vụ chính của hệ thống.",
                      "priority": "HIGH",
                      "estimatedDays": 4,
                      "bufferDays": 1,
                      "assignedRole": "Frontend Developer"
                    },
                    {
                      "sprint": "Sprint 2",
                      "title": "Tích hợp Cổng Thanh Toán Trực Tuyến VNPAY IPN Callback",
                      "description": "Xử lý tạo mã QR thanh toán và xác nhận giao dịch tự động.",
                      "priority": "HIGH",
                      "estimatedDays": 3,
                      "bufferDays": 0,
                      "assignedRole": "Backend Developer"
                    }
                  ]
                }
                """;
        }

        if (systemPrompt.contains("PM Agent")) {
            return """
                    ### 📊 BÁO CÁO TIẾN ĐỘ & RỦI RO SPACE
                    
                    **1. Tóm tắt trạng thái:**
                    - Tỷ lệ hoàn thành công việc đang tiến triển tốt.
                    - Cần lưu ý các công việc có độ ưu tiên `HIGH` và `URGENT` chưa chuyển sang trạng thái `DONE`.
                    
                    **2. Đánh giá rủi ro trễ deadline:**
                    - Khối lượng công việc tồn đọng tập trung ở giai đoạn phát triển Backend và Tích hợp AI.
                    - Rủi ro trễ hạn nếu các công việc `IN_PROGRESS` không hoàn thành đúng Sprint.
                    
                    **3. Đề xuất hành động cho PM:**
                    - Ưu tiên giải quyết các công việc đang ở trạng thái `IN_PROGRESS`.
                    - Phân công thêm thành viên hỗ trợ cho các công việc có độ ưu tiên `URGENT`.
                    - Thường xuyên theo dõi bảng Kanban để cập nhật tiến độ tức thì.
                    """;
        } else if (systemPrompt.contains("Technical Advisor")) {
            return """
                    ### 💡 TƯ VẤN GIẢI PHÁP KỸ THUẬT & KIẾN TRÚC
                    
                    **1. Nguyên nhân có thể xảy ra:**
                    - Xung đột cấu hình kết nối giữa các Microservices hoặc thiếu Header trong Yêu cầu HTTP.
                    - Chưa xử lý đúng Exception hoặc thiếu Timeout configuration.
                    
                    **2. Hướng xử lý từng bước:**
                    - **Bước 1:** Kiểm tra log chi tiết từ Eureka Discovery Service và Spring Cloud OpenFeign.
                    - **Bước 2:** Đảm bảo Annotations `@EnableFeignClients` và `@RestControllerAdvice` đã được khai báo chính xác.
                    - **Bước 3:** Bổ sung xử lý `@ExceptionHandler` để phản hồi rõ ràng về phía Client.
                    
                    **3. Gợi ý cấu hình / Code mẫu:**
                    ```java
                    // Ví dụ cấu hình Feign Client Retryer:
                    @Bean
                    public Retryer retryer() {
                        return new Retryer.Default(1000, 2000, 3);
                    }
                    ```
                    """;
        } else {
            return """
                    Xin chào! Tôi là AI Agent thuộc hệ thống PROGA.
                    
                    Tôi đã nhận được yêu cầu của bạn:
                    > """ + userPrompt + """
                    
                    Hệ thống đã ghi nhận thông tin và sẵn sàng hỗ trợ bạn phân rã công việc, quản lý rủi ro và giải quyết các bài toán kỹ thuật!
                    """;
        }
    }

    private String buildSystemPrompt(AgentType agentType) {
        return switch (agentType) {
            case REQUIREMENT -> "Bạn là Requirement Agent (Business Analyst / PO) của PROGA. Hỗ trợ phân rã bài toán và làm rõ yêu cầu nghiệp vụ.";
            case PM -> "Bạn là PM Agent của PROGA. Hỗ trợ tóm tắt tiến độ, dự báo rủi ro deadline và tư vấn quản lý dự án.";
            case TECHNICAL_ADVISOR -> "Bạn là Technical Advisor Agent của PROGA. Tư vấn kiến trúc kỹ thuật, giải pháp phát triển và khắc phục bug.";
        };
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private AiThreadResponse mapToThreadResponse(AiThread entity) {
        return AiThreadResponse.builder()
                .id(entity.getId())
                .openaiThreadId(entity.getOpenaiThreadId())
                .spaceId(entity.getSpaceId())
                .agentType(entity.getAgentType())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private AiChatMessageResponse mapToMessageResponse(AiChatMessage entity) {
        return AiChatMessageResponse.builder()
                .id(entity.getId())
                .threadId(entity.getThreadId())
                .senderType(entity.getSenderType())
                .messageContent(entity.getMessageContent())
                .jsonPayload(entity.getJsonPayload())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
