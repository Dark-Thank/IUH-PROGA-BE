package com.proga.notification_service.service.impl;

import com.proga.notification_service.dto.NotificationRequest;
import com.proga.notification_service.dto.NotificationResponse;
import com.proga.notification_service.exception.ResourceNotFoundException;
import com.proga.notification_service.model.Notification;
import com.proga.notification_service.model.NotificationType;
import com.proga.notification_service.repository.NotificationRepository;
import com.proga.notification_service.repository.NotificationTypeRepository;
import com.proga.notification_service.service.EmailService;
import com.proga.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository typeRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    @Override
    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        Short typeId = null;
        String typeCode = request.getTypeCode();
        String typeLabel = null;

        if (typeCode != null && !typeCode.isBlank()) {
            Optional<NotificationType> typeOpt = typeRepository.findByCode(typeCode);
            if (typeOpt.isPresent()) {
                typeId = typeOpt.get().getId();
                typeLabel = typeOpt.get().getLabel();
            }
        }

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .workspaceId(request.getWorkspaceId())
                .title(request.getTitle())
                .content(request.getContent())
                .isRead(false)
                .typeId(typeId)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = mapToResponse(saved, typeCode, typeLabel);

        // Realtime Push via STOMP WebSocket to user topic: /topic/notifications/{userId}
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + request.getUserId(), response);
            log.info("Broadcasted realtime notification to /topic/notifications/{}", request.getUserId());
        } catch (Exception e) {
            log.error("Failed to send STOMP WebSocket notification: {}", e.getMessage());
        }

        // Email sending if recipient email provided
        if (request.getRecipientEmail() != null && !request.getRecipientEmail().isBlank()) {
            emailService.sendEmail(request.getRecipientEmail(), request.getTitle(), request.getContent());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByUserId(long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotificationsByUserId(long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void markAllAsRead(long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }

    private NotificationResponse mapToResponse(Notification entity) {
        String typeCode = null;
        String typeLabel = null;

        if (entity.getTypeId() != null) {
            Optional<NotificationType> typeOpt = typeRepository.findById(entity.getTypeId());
            if (typeOpt.isPresent()) {
                typeCode = typeOpt.get().getCode();
                typeLabel = typeOpt.get().getLabel();
            }
        }

        return mapToResponse(entity, typeCode, typeLabel);
    }

    private NotificationResponse mapToResponse(Notification entity, String typeCode, String typeLabel) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .workspaceId(entity.getWorkspaceId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .isRead(entity.getIsRead())
                .typeId(entity.getTypeId())
                .typeCode(typeCode)
                .typeLabel(typeLabel)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
