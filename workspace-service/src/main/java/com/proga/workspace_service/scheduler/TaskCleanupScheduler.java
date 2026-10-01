package com.proga.workspace_service.scheduler;

import com.proga.workspace_service.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskCleanupScheduler {

    private final TaskService taskService;

    /**
     * Tự động xóa vĩnh viễn các task trong thùng rác đã xóa quá 15 ngày.
     * Chạy định kỳ mỗi ngày vào lúc 02:00 sáng.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredDeletedTasks() {
        log.info("Bắt đầu tiến trình tự động dọn dẹp các task trong thùng rác quá 15 ngày...");
        try {
            taskService.purgeExpiredDeletedTasks();
            log.info("Hoàn tất dọn dẹp task thùng rác quá hạn.");
        } catch (Exception e) {
            log.error("Lỗi khi chạy tiến trình dọn dẹp task thùng rác: {}", e.getMessage(), e);
        }
    }
}
