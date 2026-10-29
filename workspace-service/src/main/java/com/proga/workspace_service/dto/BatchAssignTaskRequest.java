package com.proga.workspace_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        private String startDate;
        private String dueDate;

        public LocalDateTime getParsedStartDate() {
            return parseDateTime(startDate, true);
        }

        public LocalDateTime getParsedDueDate() {
            return parseDateTime(dueDate, false);
        }

        private LocalDateTime parseDateTime(String val, boolean isStart) {
            if (val == null || val.trim().isEmpty()) return null;
            try {
                String clean = val.trim();
                if (clean.length() == 10) {
                    return LocalDate.parse(clean).atTime(isStart ? 8 : 18, 0, 0);
                }
                if (clean.contains("T")) {
                    if (clean.length() > 19) clean = clean.substring(0, 19);
                    return LocalDateTime.parse(clean);
                }
                return LocalDateTime.parse(clean);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
