package com.proga.notification_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    private Long workspaceId;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    private String typeCode; // TASK_ASSIGNED, DEADLINE_WARNING, STATUS_CHANGED, MENTION

    private String recipientEmail; // Optional: Send email if provided
}
