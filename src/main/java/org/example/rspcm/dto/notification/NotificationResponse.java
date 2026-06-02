package org.example.rspcm.dto.notification;

import org.example.rspcm.model.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String body,
        NotificationType type,
        Long referenceId,
        boolean read,
        LocalDateTime createdAt
) {}
