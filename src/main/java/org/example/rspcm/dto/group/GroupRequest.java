package org.example.rspcm.dto.group;

import org.example.rspcm.model.enums.GroupLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record GroupRequest(
        @NotBlank(message = "Поле не должно быть пустым") String name,
        String description,
        @NotNull(message = "Обязательное поле") GroupLanguage language,
        Set<Long> subjectIds,
        Set<Long> teacherIds,
        Set<Long> studentIds
) {
}
