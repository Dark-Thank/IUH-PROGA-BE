package com.proga.notification_service.service;

import com.proga.notification_service.dto.EmailNotificationRequest;

public interface EmailService {
    void sendEmail(EmailNotificationRequest request);
    void sendEmail(String to, String subject, String body);
    void sendEmail(String to, String subject, String body, String spaceName, String taskCode, String actionUrl);
    void sendEmail(String to, String subject, String body, String spaceName, String taskCode, String actionUrl, Long spaceId, Long taskId, String senderName, String replyToEmail);
    String sendTestEmail(String to);
}
