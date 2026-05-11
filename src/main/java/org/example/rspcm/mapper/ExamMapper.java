package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.GroupSummary;
import org.example.rspcm.dto.common.PracticeSummary;
import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.exam.ExamResponse;
import org.example.rspcm.model.entity.Exam;

import java.util.Set;
import java.util.stream.Collectors;

public final class ExamMapper {
    private ExamMapper() {
    }

    public static ExamResponse toResponse(Exam exam) {
        Set<GroupSummary> groups = exam.getGroups().stream()
                .map(SummaryMapper::toGroupSummary).collect(Collectors.toSet());

        Set<UserSummary> students = exam.getTargetStudents().stream()
                .map(SummaryMapper::toUserSummary).collect(Collectors.toSet());

        Set<PracticeSummary> practicalTasks = exam.getPracticalTasks().stream()
                .map(SummaryMapper::toPracticeSummary).collect(Collectors.toSet());

        UserSummary createdBy = SummaryMapper.toUserSummary(exam.getCreatedBy());

        SubjectSummary subject = exam.getSubject() == null ? null
                : SummaryMapper.toSubjectSummary(exam.getSubject());

        return new ExamResponse(
                exam.getId(),
                exam.getTitle(),
                exam.getDescription(),
                exam.getStartAt(),
                exam.getEndAt(),
                exam.getMaxScore(),
                exam.getType(),
                groups,
                students,
                practicalTasks,
                createdBy,
                subject
        );
    }
}
