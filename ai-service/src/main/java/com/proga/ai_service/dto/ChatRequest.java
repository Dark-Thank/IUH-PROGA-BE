package com.proga.ai_service.dto;

import com.proga.ai_service.model.AgentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {
    @NotNull(message = "Space ID is required")
    private Long spaceId;

    @NotNull(message = "Agent Type is required")
    private AgentType agentType;

    @NotBlank(message = "Prompt/message content cannot be blank")
    private String prompt;

    private Long taskId; // Optional context task ID (e.g. for Technical Advisor)
}
