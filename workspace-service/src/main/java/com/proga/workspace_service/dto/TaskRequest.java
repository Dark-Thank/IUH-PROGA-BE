package com.proga.workspace_service.dto;

import com.proga.workspace_service.model.Priority;
import com.proga.workspace_service.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequest {

    @NotNull(message = "Space ID is required")
    private UUID spaceId;

    @NotBlank(message = "Task title cannot be blank")
    private String title;

    private String description;
    private TaskStatus status;
    private Priority priority;
    private UUID ownerId;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
}
