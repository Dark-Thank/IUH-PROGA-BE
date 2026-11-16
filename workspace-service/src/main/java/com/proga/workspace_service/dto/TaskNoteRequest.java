package com.proga.workspace_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskNoteRequest {
    private Long authorId;

    @NotBlank(message = "Nội dung thảo luận không được để trống")
    private String noteContent;
}
