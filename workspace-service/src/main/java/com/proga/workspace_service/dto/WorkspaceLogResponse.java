package com.proga.workspace_service.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceLogResponse {
    private Long id;
    private Long workspaceId;
    private Long taskId;
    private String taskTitle;
    private Long userId;
    private String actionType;
    private String oldValue;
    private String newValue;
    private String logMessage;
    private LocalDateTime createdAt;
}
