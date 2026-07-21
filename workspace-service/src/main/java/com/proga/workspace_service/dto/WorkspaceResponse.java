package com.proga.workspace_service.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID ownerId;
    private LocalDateTime createdAt;
}
