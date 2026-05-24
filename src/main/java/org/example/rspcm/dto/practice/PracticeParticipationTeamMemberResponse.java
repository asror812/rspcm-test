package org.example.rspcm.dto.practice;

import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;

public record PracticeParticipationTeamMemberResponse(
        Long userId,
        String fullName,
        PracticeMemberRole role,
        PracticeParticipationMemberStatus status
) {
}
