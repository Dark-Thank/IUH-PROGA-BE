package com.proga.workspace_service.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskNoteResponse {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long authorId;
    private String noteContent;
    private LocalDateTime createdAt;
}
