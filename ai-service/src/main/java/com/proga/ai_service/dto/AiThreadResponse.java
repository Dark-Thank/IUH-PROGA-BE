package com.proga.ai_service.dto;

import com.proga.ai_service.model.AgentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiThreadResponse {
    private long id;
    private String openaiThreadId;
    private long spaceId;
    private AgentType agentType;
    private LocalDateTime createdAt;
}
