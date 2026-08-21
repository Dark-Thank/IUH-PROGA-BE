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
    private String sourceReference;
    private String sourceUrl;
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
        private Integer bufferDays; // Optional risk buffer days for complex tasks
        private String assignedRole; // Backend Developer, Frontend Developer, DevOps, QA, Tech Lead, BA
        private String suggestedMemberName; // Optional member name mapped from user chat prompt
        private String riskWarning; // Risk warning only for URGENT/HIGH tasks
    }
}
