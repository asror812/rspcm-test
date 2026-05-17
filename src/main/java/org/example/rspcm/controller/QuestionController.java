package org.example.rspcm.controller;

import org.example.rspcm.dto.question.QuestionRequest;
import org.example.rspcm.dto.question.QuestionResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Map<String, String>> create(
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal User user
    ) {
        questionService.create(request, user);
        return ResponseEntity.ok(Map.of("message", "Savol muvaffaqiyatli yaratildi"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Page<QuestionResponse>> getAll(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false, defaultValue = "false") boolean own,
            Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(questionService.findAll(subjectId, own, user, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<QuestionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.findResponseById(id));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Map<String, String>> update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        questionService.update(id, request);
        return ResponseEntity.ok(Map.of("message", "Savol muvaffaqiyatli yangilandi"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        questionService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
