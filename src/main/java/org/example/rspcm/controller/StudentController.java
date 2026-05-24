package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.exam.ExamResponse;
import org.example.rspcm.dto.exam.ExamPracticeResponse;
import org.example.rspcm.dto.exam.student.StudentExamAnswerRequest;
import org.example.rspcm.dto.exam.student.StudentExamAttemptResponse;
import org.example.rspcm.dto.exam.student.StudentExamQuestionResponse;
import org.example.rspcm.dto.answer.AnswerResponse;
import org.example.rspcm.dto.practice.MyPracticeParticipationResponse;
import org.example.rspcm.dto.practice.PracticeParticipationResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.service.ExamPracticeService;
import org.example.rspcm.service.ExamService;
import org.example.rspcm.service.PracticeParticipationService;
import org.example.rspcm.service.StudentQuestionExamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exams")
public class StudentController {

    private final PracticeParticipationService practiceParticipationService;
    private final ExamPracticeService examPracticeService;
    private final ExamService examService;
    private final StudentQuestionExamService studentQuestionExamService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<ExamResponse>> getMyExams(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ExamType type,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @AuthenticationPrincipal User user
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(examService.findMyExams(user, query, type, subjectId, pageable));
    }

    @GetMapping("/{examId}/practices")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ExamPracticeResponse>> getExamPractices(
            @PathVariable Long examId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(examPracticeService.findAllForStudent(examId, user));
    }

    @GetMapping("/{examId}/participation/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<MyPracticeParticipationResponse> getMyParticipation(
            @PathVariable Long examId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.getMyParticipationByExam(examId, user));
    }

    @PostMapping("/{examId}/practices/{examPracticeId}/select")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeParticipationResponse> selectPractice(
            @PathVariable Long examId,
            @PathVariable Long examPracticeId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.selectPractice(examId, examPracticeId, user));
    }

    @DeleteMapping("/{examId}/participation/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> cancelMyParticipation(
            @PathVariable Long examId,
            @AuthenticationPrincipal User user
    ) {
        practiceParticipationService.cancelMyParticipation(examId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{examId}/attempt/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentExamAttemptResponse> startAttempt(
            @PathVariable Long examId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(studentQuestionExamService.startAttempt(examId, user));
    }

    @GetMapping("/{examId}/questions/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<StudentExamQuestionResponse>> getMyExamQuestions(
            @PathVariable Long examId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(studentQuestionExamService.getQuestions(examId, user));
    }

    @PostMapping("/{examId}/questions/{examQuestionId}/answer")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AnswerResponse> saveMyExamAnswer(
            @PathVariable Long examId,
            @PathVariable Long examQuestionId,
            @Valid @RequestBody StudentExamAnswerRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(studentQuestionExamService.saveAnswer(examId, examQuestionId, request, user));
    }

    @PostMapping("/{examId}/attempt/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentExamAttemptResponse> submitAttempt(
            @PathVariable Long examId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(studentQuestionExamService.submitAttempt(examId, user));
    }
}
