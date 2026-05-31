package org.example.rspcm.dto.practice;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record PracticeAssignGroupsRequest(
        @NotEmpty(message = "Список групп не должен быть пустым") Set<Long> groupIds
) {
}
