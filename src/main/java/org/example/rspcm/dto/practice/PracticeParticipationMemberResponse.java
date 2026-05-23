package org.example.rspcm.dto.practice;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;

public record PracticeParticipationMemberResponse(
        Long id,
        UserSummary user,
        PracticeMemberRole role,
        PracticeParticipationMemberStatus status
) {
}
