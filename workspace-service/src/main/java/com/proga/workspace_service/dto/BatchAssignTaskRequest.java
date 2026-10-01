package com.proga.workspace_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchAssignTaskRequest {

    private List<AssignItem> assignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignItem {
        private Long taskId;
        private Long ownerId;
    }
}
