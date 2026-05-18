package org.example.rspcm.mapper;

import org.example.rspcm.dto.practice.PracticeAssignmentRequest;
import org.example.rspcm.dto.practice.PracticeAssignmentResponse;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamPractice;
import org.example.rspcm.model.entity.PracticeAssignment;
import org.example.rspcm.model.enums.PracticeAssignmentStatus;
import org.example.rspcm.model.entity.PracticeTeam;
import org.example.rspcm.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PracticeAssignmentMapper {
    private final SummaryMapper summaryMapper;

    public PracticeAssignmentResponse toResponse(PracticeAssignment assignment) {
        return new PracticeAssignmentResponse(
                assignment.getId(),
                summaryMapper.toExamSummary(assignment.getExam()),
                summaryMapper.toPracticeSummary(assignment.getExamPractice().getPractice()),
                assignment.getStudent() == null ? null : summaryMapper.toUserSummary(assignment.getStudent()),
                assignment.getTeam() == null ? null : summaryMapper.toPracticeTeamSummary(assignment.getTeam()),
                assignment.getChosenAt(),
                assignment.getSubmittedAt(),
                assignment.getStatus(),
                assignment.getScore(),
                assignment.getTeacherComment()
        );
    }

    public PracticeAssignment toEntity(
            PracticeAssignmentRequest request,
            Exam exam,
            ExamPractice examPractice,
            User student,
            PracticeTeam team,
            LocalDateTime chosenAt
    ) {
        return PracticeAssignment.builder()
                .exam(exam)
                .examPractice(examPractice)
                .student(student)
                .team(team)
                .status(request.status() == null ? PracticeAssignmentStatus.CHOSEN : request.status())
                .chosenAt(chosenAt)
                .score(request.score())
                .teacherComment(request.teacherComment())
                .build();
    }

    public void updateEntity(
            PracticeAssignment assignment,
            PracticeAssignmentRequest request,
            Exam exam,
            ExamPractice examPractice,
            User student,
            PracticeTeam team
    ) {
        assignment.setExam(exam);
        assignment.setExamPractice(examPractice);
        assignment.setStudent(student);
        assignment.setTeam(team);
        assignment.setStatus(request.status() == null ? assignment.getStatus() : request.status());
        assignment.setScore(request.score());
        assignment.setTeacherComment(request.teacherComment());
    }
}
