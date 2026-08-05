package com.proga.ai_service.dto;

import com.proga.ai_service.model.SenderType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatMessageResponse {
    private long id;
    private long threadId;
    private SenderType senderType;
    private String messageContent;
    private String jsonPayload;
    private LocalDateTime createdAt;
}
