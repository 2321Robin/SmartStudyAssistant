package com.smartstudy.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(min = 8, max = 255) String password
) {
}
