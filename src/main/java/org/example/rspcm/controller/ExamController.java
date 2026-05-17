package org.example.rspcm.controller;

import org.example.rspcm.dto.exam.ExamRequest;
import org.example.rspcm.dto.exam.ExamResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.service.ExamService;
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
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Page<ExamResponse>> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ExamType type,
            @RequestParam(required = false) boolean own,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @AuthenticationPrincipal User user
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(examService.findAll(user, query, type, own, subjectId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ExamResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(examService.findById(id, user));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ExamResponse> create(
            @Valid @RequestBody ExamRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(examService.create(user, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ExamResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExamRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(examService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        examService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
