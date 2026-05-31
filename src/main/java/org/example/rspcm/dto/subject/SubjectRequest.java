package org.example.rspcm.dto.subject;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record SubjectRequest(
        @NotBlank(message = "Поле не должно быть пустым") String name,
        String description,
        Set<Long> teacherIds
) {
}
