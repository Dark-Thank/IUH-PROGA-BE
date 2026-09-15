package com.proga.ai_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeHarvestRequest {
    private Long spaceId;

    @NotBlank(message = "Space title is required")
    private String title;

    private String domain;

    @NotBlank(message = "Requirement text is required")
    private String requirementText;

    private String summary;

    private String tasksJson;
}
