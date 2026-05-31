package org.example.rspcm.dto.user;

import org.example.rspcm.model.enums.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UserUpdateRequest(
        @NotBlank(message = "Поле не должно быть пустым") String firstName,
        @NotBlank(message = "Поле не должно быть пустым") String lastName,
        @NotEmpty(message = "Набор ролей не должен быть пустым") Set<RoleName> roles,
        boolean enabled
) {
}
