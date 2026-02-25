package com.brentcodes.springboot.librarysystem.message;

import java.time.Instant;

public record MessageDto(
        Long id,
        String content,
        Long senderId,
        String senderFirstname,
        String senderLastname,
        Long projectId,
        Instant sentAt
) {}
