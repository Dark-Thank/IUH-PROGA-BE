package com.proga.workspace_service.dto;

import com.proga.workspace_service.model.Priority;
import com.proga.workspace_service.model.TaskStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private long id;
    private long spaceId;
    private Long sprintId;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private Long ownerId;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
}
