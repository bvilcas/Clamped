package com.brentcodes.springboot.librarysystem.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank @Size(max = 2000) String message
) {}
