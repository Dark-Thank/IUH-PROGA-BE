package com.proga.ai_service.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDecompositionResponse {
    private long threadId;
    private String summary;
    private List<DecomposedTaskItem> tasks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DecomposedTaskItem {
        private String sprint; // e.g. Sprint 1, Sprint 2, Sprint 3
        private String title;
        private String description;
        private String priority; // LOW, MEDIUM, HIGH, URGENT
        private Integer estimatedDays;
    }
}
