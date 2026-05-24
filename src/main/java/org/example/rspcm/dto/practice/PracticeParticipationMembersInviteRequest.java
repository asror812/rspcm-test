package org.example.rspcm.dto.practice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PracticeParticipationMembersInviteRequest(
        @NotEmpty List<@NotNull Long> studentIds
) {
}
