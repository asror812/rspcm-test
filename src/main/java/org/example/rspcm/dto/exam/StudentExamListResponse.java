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

public record StudentExamListResponse(
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
        List<ExamQuestionSummary> questions,

        UserSummary createdBy,
        SubjectSummary subject,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        String myStatus
) {
    public static StudentExamListResponse from(ExamResponse r, String myStatus) {
        return new StudentExamListResponse(
                r.id(), r.title(), r.description(),
                r.startAt(), r.endAt(),
                r.maxScore(), r.taskLimit(),
                r.type(), r.status(),
                r.groups(), r.students(), r.practices(), r.questions(),
                r.createdBy(), r.subject(),
                r.createdAt(), r.updatedAt(),
                myStatus
        );
    }
}
