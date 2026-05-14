package org.example.rspcm.mapper;

import org.example.rspcm.dto.practice.PracticalTaskAssignmentRequest;
import org.example.rspcm.dto.practice.PracticalTaskAssignmentResponse;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.PracticalTask;
import org.example.rspcm.model.entity.PracticalTaskAssignment;
import org.example.rspcm.model.enums.PracticalTaskAssignmentStatus;
import org.example.rspcm.model.entity.PracticeTeam;
import org.example.rspcm.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PracticalTaskAssignmentMapper {
    private final SummaryMapper summaryMapper;

    public PracticalTaskAssignmentResponse toResponse(PracticalTaskAssignment assignment) {
        return new PracticalTaskAssignmentResponse(
                assignment.getId(),
                summaryMapper.toExamSummary(assignment.getExam()),
                summaryMapper.toPracticeSummary(assignment.getPracticalTask()),
                assignment.getStudent() == null ? null : summaryMapper.toUserSummary(assignment.getStudent()),
                assignment.getTeam() == null ? null : summaryMapper.toPracticeTeamSummary(assignment.getTeam()),
                assignment.getChosenAt(),
                assignment.getSubmittedAt(),
                assignment.getStatus(),
                assignment.getScore(),
                assignment.getTeacherComment()
        );
    }

    public PracticalTaskAssignment toEntity(
            PracticalTaskAssignmentRequest request,
            Exam exam,
            PracticalTask practicalTask,
            User student,
            PracticeTeam team,
            LocalDateTime chosenAt
    ) {
        return PracticalTaskAssignment.builder()
                .exam(exam)
                .practicalTask(practicalTask)
                .student(student)
                .team(team)
                .status(request.status() == null ? PracticalTaskAssignmentStatus.CHOSEN : request.status())
                .chosenAt(chosenAt)
                .score(request.score())
                .teacherComment(request.teacherComment())
                .build();
    }

    public void updateEntity(
            PracticalTaskAssignment assignment,
            PracticalTaskAssignmentRequest request,
            Exam exam,
            PracticalTask practicalTask,
            User student,
            PracticeTeam team
    ) {
        assignment.setExam(exam);
        assignment.setPracticalTask(practicalTask);
        assignment.setStudent(student);
        assignment.setTeam(team);
        assignment.setStatus(request.status() == null ? assignment.getStatus() : request.status());
        assignment.setScore(request.score());
        assignment.setTeacherComment(request.teacherComment());
    }
}
