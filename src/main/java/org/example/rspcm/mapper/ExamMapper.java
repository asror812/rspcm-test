package org.example.rspcm.mapper;

import org.example.rspcm.dto.common.GroupSummary;
import org.example.rspcm.dto.common.PracticeSummary;
import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.exam.ExamRequest;
import org.example.rspcm.dto.exam.ExamResponse;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.PracticalTask;
import org.example.rspcm.model.entity.StudyGroup;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public final class ExamMapper {
    private ExamMapper() {
    }

    public ExamResponse toResponse(Exam exam) {
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

    public Exam toEntity(
            ExamRequest request,
            Set<StudyGroup> groups,
            Set<User> students,
            User createdBy,
            Subject subject
    ) {
        return Exam.builder()
                .title(request.title())
                .description(request.description())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .maxScore(request.maxScore())
                .type(request.type())
                .groups(groups)
                .targetStudents(students)
                .createdBy(createdBy)
                .subject(subject)
                .status(ExamStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateEntity(
            Exam exam,
            ExamRequest request,
            Set<StudyGroup> groups,
            Set<User> students,
            Subject subject
    ) {
        exam.setTitle(request.title());
        exam.setDescription(request.description());
        exam.setStartAt(request.startAt());
        exam.setEndAt(request.endAt());
        exam.setMaxScore(request.maxScore());
        exam.setType(request.type());
        exam.setGroups(groups);
        exam.setTargetStudents(students);
        exam.setSubject(subject);
    }
}
