package com.proga.notification_service.controller;

import com.proga.notification_service.dto.*;
import com.proga.notification_service.service.EmailService;
import com.proga.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.ok(ApiResponse.success("Notification sent successfully", response));
    }

    @PostMapping("/email")
    public ResponseEntity<ApiResponse<Void>> sendEmail(@Valid @RequestBody EmailNotificationRequest request) {
        emailService.sendEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Email process initiated", null));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationsByUserId(@PathVariable long userId) {
        List<NotificationResponse> response = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotificationsByUserId(@PathVariable long userId) {
        List<NotificationResponse> response = notificationService.getUnreadNotificationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable long id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read for user", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    // In-memory version state for dynamic version checking and easy testing
    private static volatile AppVersionResponse currentVersionConfig = AppVersionResponse.builder()
            .latestVersion("1.0.1")
            .latestVersionCode(2)
            .minRequiredVersion("1.0.0")
            .forceUpdate(false)
            .downloadUrl("https://github.com/iuh-proga/proga-mobile/releases/download/v1.0.1/iuh-proga-v1.0.1.apk")
            .releaseNotes("• Bổ sung tính năng thông báo cập nhật công việc thời gian thực\n• Cải tiến hiệu năng bảng Kanban\n• Nâng cấp giao diện Jira theme")
            .releaseDate("18/11/2026")
            .build();

    @GetMapping("/app-version")
    public ResponseEntity<ApiResponse<AppVersionResponse>> getAppVersion(
            @RequestParam(defaultValue = "android") String platform,
            @RequestParam(defaultValue = "1.0.0") String currentVersion) {
        return ResponseEntity.ok(ApiResponse.success("App version configuration retrieved", currentVersionConfig));
    }

    @PostMapping("/app-version")
    public ResponseEntity<ApiResponse<AppVersionResponse>> updateAppVersionConfig(
            @RequestBody AppVersionResponse newConfig) {
        currentVersionConfig = newConfig;
        return ResponseEntity.ok(ApiResponse.success("App version configuration updated successfully", currentVersionConfig));
    }
}
