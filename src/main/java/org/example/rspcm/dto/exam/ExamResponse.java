package org.example.rspcm.dto.exam;

import org.example.rspcm.dto.common.GroupSummary;
import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.ExamType;

import java.time.LocalDateTime;
import java.util.Set;

public record ExamResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer maxScore,
        ExamType type,
        Set<GroupSummary> groups,
        Set<UserSummary> students,
        UserSummary createdBy,
        SubjectSummary subject
) {
}
