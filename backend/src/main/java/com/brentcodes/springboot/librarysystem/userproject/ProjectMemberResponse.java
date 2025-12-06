package com.brentcodes.springboot.librarysystem.userproject;

public record ProjectMemberResponse(
        Long id,
        String firstname,
        String lastname,
        ProjectRole projectRole
) {}
