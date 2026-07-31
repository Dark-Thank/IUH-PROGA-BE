package com.proga.workspace_service.dto;

import com.proga.workspace_service.model.SprintStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintResponse {
    private long id;
    private long spaceId;
    private String name;
    private String goal;
    private SprintStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
}
