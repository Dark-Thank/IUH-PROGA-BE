package com.proga.workspace_service.dto;

import com.proga.workspace_service.model.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintRequest {

    @NotNull(message = "Space ID is required")
    private long spaceId;

    @NotBlank(message = "Sprint name cannot be blank")
    private String name;

    private String goal;
    private SprintStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
