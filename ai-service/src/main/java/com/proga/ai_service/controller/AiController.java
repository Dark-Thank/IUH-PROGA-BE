package com.proga.ai_service.controller;

import com.proga.ai_service.dto.*;
import com.proga.ai_service.model.AgentType;
import com.proga.ai_service.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/threads")
    public ResponseEntity<ApiResponse<AiThreadResponse>> getOrCreateThread(@Valid @RequestBody AiThreadRequest request) {
        AiThreadResponse response = aiService.getOrCreateThread(request.getSpaceId(), request.getAgentType());
        return ResponseEntity.ok(ApiResponse.success("AI Thread retrieved/created successfully", response));
    }

    @GetMapping("/threads/space/{spaceId}")
    public ResponseEntity<ApiResponse<List<AiThreadResponse>>> getThreadsBySpace(@PathVariable long spaceId) {
        List<AiThreadResponse> response = aiService.getThreadsBySpace(spaceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/messages/thread/{threadId}")
    public ResponseEntity<ApiResponse<List<AiChatMessageResponse>>> getThreadMessages(@PathVariable long threadId) {
        List<AiChatMessageResponse> response = aiService.getThreadMessages(threadId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatMessageResponse>> chat(@Valid @RequestBody ChatRequest request) {
        AiChatMessageResponse response = aiService.chat(request);
        return ResponseEntity.ok(ApiResponse.success("AI response generated successfully", response));
    }

    @PostMapping("/agents/decompose")
    public ResponseEntity<ApiResponse<TaskDecompositionResponse>> decomposeRequirements(@Valid @RequestBody TaskDecompositionRequest request) {
        TaskDecompositionResponse response = aiService.decomposeRequirements(request);
        return ResponseEntity.ok(ApiResponse.success("Requirements decomposed into tasks successfully", response));
    }

    @PostMapping("/agents/pm-summary/{spaceId}")
    public ResponseEntity<ApiResponse<AiChatMessageResponse>> analyzePmProgressAndRisk(@PathVariable long spaceId) {
        AiChatMessageResponse response = aiService.analyzePmProgressAndRisk(spaceId);
        return ResponseEntity.ok(ApiResponse.success("PM progress and risk summary generated successfully", response));
    }

    @PostMapping("/agents/tech-advisor")
    public ResponseEntity<ApiResponse<AiChatMessageResponse>> getTechnicalAdvice(
            @RequestParam long taskId,
            @RequestParam String problemDescription) {
        AiChatMessageResponse response = aiService.getTechnicalAdvice(taskId, problemDescription);
        return ResponseEntity.ok(ApiResponse.success("Technical advice generated successfully", response));
    }
}
