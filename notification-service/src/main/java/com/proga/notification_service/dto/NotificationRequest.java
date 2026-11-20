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

    private Long spaceId;

    private Long taskId;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    private String typeCode; // TASK_ASSIGNED, DEADLINE_WARNING, STATUS_CHANGED, MENTION

    private String recipientEmail; // Optional: Send email if provided

    private String taskCode; // e.g. Task-8

    private String spaceName; // e.g. Dự Án Khóa Luận Tốt Nghiệp IUH-PROGA

    private String actionUrl; // Direct link to task

    private String senderName; // e.g. Trần Đỗ Đăng Khánh

    private String replyToEmail; // e.g. khanh@gmail.com
}
