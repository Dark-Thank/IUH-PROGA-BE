package com.proga.workspace_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaceResponse {
    private long id;
    private long workspaceId;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isPrivate;
    private LocalDateTime createdAt;
}
