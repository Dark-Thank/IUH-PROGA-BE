package com.proga.workspace_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceRequest {

    @NotBlank(message = "Workspace name cannot be blank")
    private String name;

    private String description;

    @NotNull(message = "Owner ID is required")
    private UUID ownerId;
}
