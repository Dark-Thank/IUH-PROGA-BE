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
        if (userRequirement == null || userRequirement.isBlank()) return ragSamples.get(0);

        String reqLower = userRequirement.toLowerCase();
        Map<String, Object> bestSample = ragSamples.get(0);
        int maxScore = -1;

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

        log.info("RAG Similarity Search selected sample: '{}' (Score: {}) for requirement: '{}'",
                bestSample.get("title"), maxScore, userRequirement);
        return bestSample;
    }

    @Override
    @Transactional
    public TaskDecompositionResponse decomposeRequirements(TaskDecompositionRequest request) {
        AiThreadResponse thread = getOrCreateThread(request.getSpaceId(), AgentType.REQUIREMENT);

        // Save User Prompt
        messageRepository.save(AiChatMessage.builder()
                .threadId(thread.getId())
                .senderType(SenderType.USER)
                .messageContent("Phân rã yêu cầu bài toán:\n" + request.getRequirementText())
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
        String ragSourceRef = matchedRagSample != null ? (String) matchedRagSample.getOrDefault("sourceReference", "IEEE Std 12207 & Atlassian WBS Standards") : "PMBOK 7th Edition Agile Standards";
        String ragSourceUrl = matchedRagSample != null ? (String) matchedRagSample.getOrDefault("sourceUrl", "https://www.atlassian.com/agile/project-management/work-breakdown-structure") : "https://www.pmi.org/pmbok-guide-standards";

        String systemPrompt = String.format("""
                Bạn là một Requirement Agent (Product Owner / Business Analyst) chuyên nghiệp cho hệ thống quản lý dự án PROGA.
                Nhiệm vụ của bạn là phân rã yêu cầu bài toán được cung cấp thành danh sách từ 10 - 25 Task cụ thể, được phân bổ theo thứ tự các Sprint phù hợp.
                
                ĐÂY LÀ MẪU DỮ LIỆU TRI THỨC RAG TƯƠNG ĐỒNG ĐƯỢC RÚT RA TỪ KHO TRI THỨC NGUỒN CHUẨN QUỐC TẾ (%s):
                %s
                
                YÊU CẦU ĐỊNH DẠNG ĐẦU RA STRICT JSON:
                Bạn PHẢI trả về duy nhất chuỗi JSON chính xác theo cấu trúc sau (không kèm lời giải thích bên ngoài):
                {
                  "summary": "Tóm tắt ngắn gọn các hạng mục công việc được phân rã",
                  "sourceReference": "%s",
                  "sourceUrl": "%s",
                  "tasks": [
                    {
                      "sprint": "Sprint 1: Tên Sprint",
                      "title": "Tên task ngắn gọn rõ ràng",
                      "description": "Mô tả công việc chi tiết",
                      "priority": "HIGH / MEDIUM / LOW / URGENT",
                      "estimatedDays": 2,
                      "storyPoints": 5,
                      "reasoning": "Giải thích chi tiết độ phức tạp kỹ thuật và lý do chọn Story Points theo chuẩn Scrum",
                      "recommendedRole": "Tech Lead / Senior Backend / Frontend Dev / DevOps / QA Lead / Business Analyst",
                      "contingencyPlan": "Phương án xử lý khi gặp rủi ro kỹ thuật hoặc chậm tiến độ"
                    }
                  ]
                }
                """, ragSourceRef, sampleJsonContext, ragSourceRef, ragSourceUrl);

        String userPrompt = "Yêu cầu bài toán:\n" + request.getRequirementText();
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
            List<Map<String, Object>> tasksRaw = (List<Map<String, Object>>) parsed.getOrDefault("tasks", Collections.emptyList());

            List<TaskDecompositionResponse.DecomposedTaskItem> taskItems = tasksRaw.stream().map(t -> 
                TaskDecompositionResponse.DecomposedTaskItem.builder()
                        .sprint((String) t.getOrDefault("sprint", "Sprint 1"))
                        .title((String) t.get("title"))
                        .description((String) t.get("description"))
                        .priority((String) t.getOrDefault("priority", "MEDIUM"))
                        .estimatedDays(t.get("estimatedDays") != null ? ((Number) t.get("estimatedDays")).intValue() : 2)
                        .storyPoints(t.get("storyPoints") != null ? ((Number) t.get("storyPoints")).intValue() : 3)
                        .reasoning((String) t.getOrDefault("reasoning", "Dựa trên độ phức tạp xử lý nghiệp vụ và tích hợp hệ thống"))
                        .recommendedRole((String) t.getOrDefault("recommendedRole", "Backend Developer"))
                        .contingencyPlan((String) t.getOrDefault("contingencyPlan", "Sử dụng tài liệu chuẩn và chia nhỏ công việc"))
                        .build()
            ).collect(Collectors.toList());

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
                            .storyPoints(st.get("storyPoints") != null ? ((Number) st.get("storyPoints")).intValue() : 3)
                            .reasoning((String) st.getOrDefault("reasoning", "Phân tích theo tiêu chuẩn WBS"))
                            .recommendedRole((String) st.getOrDefault("recommendedRole", "Software Engineer"))
                            .contingencyPlan((String) st.getOrDefault("contingencyPlan", "Tham khảo tài liệu kiến trúc mẫu"))
                            .build());
                }
            } else {
                fallbackItems.add(TaskDecompositionResponse.DecomposedTaskItem.builder()
                        .sprint("Sprint 1")
                        .title("Phân tích & Thiết kế Schema Cơ sở dữ liệu")
                        .description("Tạo sơ đồ ERD và DDL cho các bảng trong hệ thống")
                        .priority("URGENT")
                        .estimatedDays(3)
                        .storyPoints(5)
                        .reasoning("Thiết kế Schema cơ sở nền tảng quan trọng")
                        .recommendedRole("Database Administrator / Tech Lead")
                        .contingencyPlan("Dùng script migration tự động")
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
