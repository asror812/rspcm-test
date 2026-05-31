package org.example.rspcm.dto.practice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PracticeParticipationMembersInviteRequest(
        @NotEmpty(message = "Список студентов не должен быть пустым") List<@NotNull(message = "Идентификатор студента обязателен") Long> studentIds
) {
}
