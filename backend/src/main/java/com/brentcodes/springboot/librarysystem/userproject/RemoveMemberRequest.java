package com.brentcodes.springboot.librarysystem.userproject;

import jakarta.validation.constraints.NotNull;

public record RemoveMemberRequest(
        @NotNull Long projectId,
        @NotNull Long userId
        ) {}