package com.proga.notification_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private long id;
    private long userId;
    private Long workspaceId;
    private String title;
    private String content;
    private Boolean isRead;
    private Short typeId;
    private String typeCode;
    private String typeLabel;
    private LocalDateTime createdAt;
}
