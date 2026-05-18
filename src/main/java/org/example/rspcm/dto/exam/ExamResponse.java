package org.example.rspcm.dto.exam;

import org.example.rspcm.dto.common.GroupSummary;
import org.example.rspcm.dto.common.PracticeSummary;
import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.enums.ExamStatus;
import org.example.rspcm.model.enums.ExamType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record ExamResponse(
        Long id,
        String title,
        String description,

        LocalDateTime startAt,
        LocalDateTime endAt,

        Integer maxScore,
        Integer taskLimit,

        ExamType type,
        ExamStatus status,

        Set<GroupSummary> groups,
        Set<UserSummary> students,
        List<PracticeSummary> practices,
        List<ExamQuestionSummary> examQuestions,

        UserSummary createdBy,
        SubjectSummary subject,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
