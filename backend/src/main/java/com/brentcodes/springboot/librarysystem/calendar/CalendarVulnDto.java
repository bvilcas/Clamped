package com.brentcodes.springboot.librarysystem.calendar;

import com.brentcodes.springboot.librarysystem.vulnerability.Severity;
import com.brentcodes.springboot.librarysystem.vulnerability.VulnStatus;

import java.time.Instant;

public record CalendarVulnDto(
        Long id,
        String title,
        Severity severity,
        VulnStatus status,
        Instant dueAt,
        Long projectId,
        String projectName
) {}
