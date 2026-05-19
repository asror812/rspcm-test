package org.example.rspcm.controller;

import org.example.rspcm.dto.exam.ExamQuestionRequest;
import org.example.rspcm.dto.exam.ExamQuestionResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.service.ExamQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exam-questions")
public class ExamQuestionController {

    private final ExamQuestionService examQuestionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ExamQuestionResponse> create(
            @Valid @RequestBody ExamQuestionRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(examQuestionService.create(request, user));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Page<ExamQuestionResponse>> getAll(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false, defaultValue = "false") boolean own,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @AuthenticationPrincipal User user
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(examQuestionService.findAll(examId, subjectId, own, user, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<ExamQuestionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(examQuestionService.findResponseById(id));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ExamQuestionResponse> update(@PathVariable Long id, @Valid @RequestBody ExamQuestionRequest request) {
        return ResponseEntity.ok(examQuestionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        examQuestionService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
