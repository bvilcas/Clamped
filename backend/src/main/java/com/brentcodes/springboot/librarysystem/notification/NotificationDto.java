package com.brentcodes.springboot.librarysystem.notification;

import java.time.Instant;

public record NotificationDto(
        Long id,
        NotificationType type,
        String message,
        Long relatedProjectId,
        Long relatedVulnId,
        boolean read,
        Instant createdAt
) {}
