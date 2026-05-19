package org.example.rspcm.controller;

import org.example.rspcm.dto.practice.PracticeAssignmentRequest;
import org.example.rspcm.dto.practice.PracticeAssignmentResponse;
import org.example.rspcm.service.PracticeAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/practice-assignments")
public class PracticeAssignmentController {

    private final PracticeAssignmentService assignmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<PracticeAssignmentResponse>> getAll() {
        return ResponseEntity.ok(assignmentService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<PracticeAssignmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.findResponseById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PracticeAssignmentResponse> create(@Valid @RequestBody PracticeAssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.createResponse(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PracticeAssignmentResponse> update(@PathVariable Long id, @Valid @RequestBody PracticeAssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
