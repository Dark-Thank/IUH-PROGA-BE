package com.proga.ai_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {
    private long id;
    private long spaceId;
    private Long sprintId;
    private String title;
    private String description;
    private String status;   // TODO, IN_PROGRESS, REVIEW, DONE
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private Long ownerId;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
}
