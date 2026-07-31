package com.proga.notification_service.service;

import com.proga.notification_service.dto.EmailNotificationRequest;

public interface EmailService {
    void sendEmail(EmailNotificationRequest request);
    void sendEmail(String to, String subject, String body);
}
