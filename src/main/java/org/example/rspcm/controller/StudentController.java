package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.exam.ExamResponse;
import org.example.rspcm.dto.exam.ExamPracticeResponse;
import org.example.rspcm.dto.practice.MyPracticeParticipationResponse;
import org.example.rspcm.dto.practice.PracticeParticipationResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.service.ExamPracticeService;
import org.example.rspcm.service.ExamService;
import org.example.rspcm.service.PracticeParticipationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exams")
public class StudentController {

    private final PracticeParticipationService practiceParticipationService;
    private final ExamPracticeService examPracticeService;
    private final ExamService examService;

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

    @PostMapping("/{examId}/practices/{examPracticeId}/choose")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeParticipationResponse> choosePractice(
            @PathVariable Long examId,
            @PathVariable Long examPracticeId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.chooseIndividualPractice(examId, examPracticeId, user));
    }

    @PostMapping("/{examId}/practices/{examPracticeId}/team")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeParticipationResponse> createTeamParticipation(
            @PathVariable Long examId,
            @PathVariable Long examPracticeId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.createTeamParticipation(examId, examPracticeId, user));
    }
}
