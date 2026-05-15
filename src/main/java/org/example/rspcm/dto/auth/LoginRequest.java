package org.example.rspcm.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email String identifier,
        @NotBlank String password
) {
}
