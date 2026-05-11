package org.example.rspcm.controller;

import org.example.rspcm.dto.practice.PracticeRequest;
import org.example.rspcm.dto.practice.PracticeResponse;
import org.example.rspcm.service.PracticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/practices")
public class PracticeController {

    private final PracticeService practiceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<PracticeResponse>> getAll() {
        return ResponseEntity.ok(practiceService.findAllResponse());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<PracticeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(practiceService.findResponseById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PracticeResponse> create(@Valid @RequestBody PracticeRequest request) {
        return ResponseEntity.ok(practiceService.createResponse(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PracticeResponse> update(@PathVariable Long id, @Valid @RequestBody PracticeRequest request) {
        return ResponseEntity.ok(practiceService.updateResponse(id, request));
    }

    @PatchMapping("/{id}/assign-groups")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PracticeResponse> assignGroups(@PathVariable Long id) {
        return ResponseEntity.ok(practiceService.assignGroupsResponse(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        practiceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
