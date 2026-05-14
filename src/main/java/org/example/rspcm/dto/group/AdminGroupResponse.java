package org.example.rspcm.dto.group;

import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.GroupLanguage;

import java.util.Set;

public record AdminGroupResponse(
        Long id,
        String name,
        String description,
        GroupLanguage language,
        Set<SubjectSummary> subjects,
        Set<UserSummary> teachers,
        Set<UserSummary> students
) {
}
