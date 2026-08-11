package com.proga.notification_service.service.impl;

import com.proga.notification_service.dto.EmailNotificationRequest;
import com.proga.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Async
    @Override
    public void sendEmail(EmailNotificationRequest request) {
        sendEmail(request.getRecipientEmail(), request.getSubject(), request.getBody());
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Email to [{}] with subject [{}] skipped.", to, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("no-reply@proga.iuh.edu.vn");

            mailSender.send(message);
            log.info("Successfully sent notification email to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
