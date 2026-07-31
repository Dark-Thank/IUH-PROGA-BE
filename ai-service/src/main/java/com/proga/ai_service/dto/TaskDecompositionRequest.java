package com.proga.ai_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDecompositionRequest {
    @NotNull(message = "Space ID is required")
    private Long spaceId;

    @NotBlank(message = "Requirement text is required")
    private String requirementText;
}
