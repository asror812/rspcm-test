package org.example.rspcm.dto.practice;

import org.example.rspcm.dto.common.PracticeSummary;
import org.example.rspcm.dto.common.UserSummary;

public record MyTeamInvitationResponse(
        Long participationId,
        Long examId,
        Long examPracticeId,
        String examTitle,
        PracticeSummary practice,
        UserSummary invitedBy
) {
}
