package org.example.rspcm.dto.profile;

import org.example.rspcm.dto.common.GroupSummary;
import org.example.rspcm.dto.common.UserSummary;

public record StudentProfileResponse(
        Long id,
        UserSummary user,
        Integer course,
        GroupSummary group
) {
}
