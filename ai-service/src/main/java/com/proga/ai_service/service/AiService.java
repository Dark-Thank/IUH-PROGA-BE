package com.proga.ai_service.service;

import com.proga.ai_service.dto.*;
import com.proga.ai_service.model.AgentType;

import java.util.List;

public interface AiService {

    AiThreadResponse getOrCreateThread(long spaceId, AgentType agentType);

    List<AiThreadResponse> getThreadsBySpace(long spaceId);

    List<AiChatMessageResponse> getThreadMessages(long threadId);

    AiChatMessageResponse chat(ChatRequest request);

    TaskDecompositionResponse decomposeRequirements(TaskDecompositionRequest request);

    AiChatMessageResponse analyzePmProgressAndRisk(long spaceId);

    AiChatMessageResponse getTechnicalAdvice(long taskId, String problemDescription);
}
