package com.proga.ai_service.dto;

import com.proga.ai_service.model.AgentType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiThreadRequest {
    @NotNull(message = "Space ID is required")
    private Long spaceId;

    @NotNull(message = "Agent Type is required")
    private AgentType agentType;
}
