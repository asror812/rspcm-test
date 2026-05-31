package org.example.rspcm.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Поле не должно быть пустым") String firstName,
        @NotBlank(message = "Поле не должно быть пустым") String lastName,
        @NotBlank(message = "Поле не должно быть пустым") @Email(message = "Некорректный формат электронной почты") String email,
        @NotBlank(message = "Пароль не должен быть пустым") @Size(min = 6, message = "Пароль должен содержать минимум {min} символов") String password
) {
}
