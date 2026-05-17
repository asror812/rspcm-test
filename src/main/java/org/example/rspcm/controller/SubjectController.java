package org.example.rspcm.controller;

import org.example.rspcm.dto.common.SubjectSummary;
import org.example.rspcm.dto.subject.SubjectRequest;
import org.example.rspcm.dto.subject.SubjectResponse;
import org.example.rspcm.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping("/own")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SubjectSummary>> getOwn() {
        return ResponseEntity.ok(subjectService.findOwnSummaries());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<Page<SubjectResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(subjectService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<SubjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.findByIdResponse(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody SubjectRequest request) {
        subjectService.createResponse(request);
        return ResponseEntity.ok(Map.of("message", "Fan muvaffaqiyatli yaratildi"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Map<String, String>> update(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        subjectService.update(id, request);
        return ResponseEntity.ok(Map.of("message", "Fan muvaffaqiyatli yangilandi"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
