package com.proga.notification_service.service;

import com.proga.notification_service.dto.NotificationRequest;
import com.proga.notification_service.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse sendNotification(NotificationRequest request);

    List<NotificationResponse> getNotificationsByUserId(long userId);

    List<NotificationResponse> getUnreadNotificationsByUserId(long userId);

    long getUnreadCount(long userId);

    NotificationResponse markAsRead(long id);

    void markAllAsRead(long userId);

    void deleteNotification(long id);
}
