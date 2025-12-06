package com.brentcodes.springboot.librarysystem.project;

public record ProjectUpdateRequest(
        String name,
        String description
) {
}
