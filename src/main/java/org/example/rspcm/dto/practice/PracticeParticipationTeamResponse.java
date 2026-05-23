package org.example.rspcm.dto.practice;

import java.util.List;

public record PracticeParticipationTeamResponse(
        int membersCount,
        Integer maxMembers,
        List<PracticeParticipationTeamMemberResponse> members
) {
}
