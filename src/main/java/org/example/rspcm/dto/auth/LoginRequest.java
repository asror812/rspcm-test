package org.example.rspcm.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Поле не должно быть пустым") String identifier,
        @NotBlank(message = "Поле не должно быть пустым") String password
) {
}
