package org.example.rspcm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.practice.PracticeAttemptCommentRequest;
import org.example.rspcm.dto.practice.PracticeSubmissionAttemptResponse;
import org.example.rspcm.dto.practice.PracticeSubmissionResponse;
import org.example.rspcm.dto.practice.PracticeSubmissionReviewRequest;
import org.example.rspcm.dto.practice.PracticeSubmissionSubmitRequest;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.PracticeSubmissionStatus;
import org.example.rspcm.service.PracticeSubmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/practice-submissions")
public class PracticeSubmissionController {

    private final PracticeSubmissionService submissionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Page<PracticeSubmissionResponse>> getAllByExam(
            @RequestParam Long examId,
            @RequestParam(required = false) PracticeSubmissionStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @AuthenticationPrincipal User user
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(submissionService.findAllByExam(examId, status, user, pageable));
    }

    @GetMapping("/{submissionId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<PracticeSubmissionResponse> getById(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(submissionService.getById(submissionId, user));
    }

    @GetMapping("/participation/{participationId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<PracticeSubmissionResponse> getByParticipation(
            @PathVariable Long participationId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(submissionService.getByParticipation(participationId, user));
    }

    @PostMapping("/participation/{participationId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeSubmissionResponse> submit(
            @PathVariable Long participationId,
            @Valid @RequestBody PracticeSubmissionSubmitRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(submissionService.submit(participationId, request, user));
    }

    @PatchMapping("/{submissionId}/return")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PracticeSubmissionResponse> returnSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody PracticeSubmissionReviewRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(submissionService.returnSubmission(submissionId, request, user));
    }

    @GetMapping("/{submissionId}/history")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<PracticeSubmissionAttemptResponse>> getHistory(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(submissionService.getHistory(submissionId, user));
    }

    @PatchMapping("/attempts/{attemptId}/comment")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PracticeSubmissionAttemptResponse> commentOnAttempt(
            @PathVariable Long attemptId,
            @Valid @RequestBody PracticeAttemptCommentRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(submissionService.commentOnAttempt(attemptId, request, user));
    }

    @PatchMapping("/{submissionId}/grade")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PracticeSubmissionResponse> grade(
            @PathVariable Long submissionId,
            @Valid @RequestBody PracticeSubmissionReviewRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(submissionService.grade(submissionId, request, user));
    }
}
