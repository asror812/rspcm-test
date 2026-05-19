package org.example.rspcm.dto.practice;

import org.example.rspcm.dto.common.PracticeSummary;
import org.example.rspcm.dto.common.UserSummary;
import java.util.Set;

public record PracticeTeamResponse(
        Long id,
        PracticeSummary practice,
        String name,
        Set<UserSummary> members
) {
}
