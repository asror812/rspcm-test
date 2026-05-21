package org.example.rspcm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.practice.*;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.service.PracticeParticipationService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/practice-participations")
public class PracticeParticipationController {

    private final PracticeParticipationService practiceParticipationService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeParticipationResponse> create(
            @Valid @RequestBody PracticeParticipationCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.create(request, user));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Page<PracticeParticipationResponse>> getAll(
            @RequestParam Long examId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @AuthenticationPrincipal User user
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(practiceParticipationService.findAll(examId, user, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PracticeParticipationResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.findById(id, user));
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeParticipationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PracticeParticipationUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.update(id, request, user));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PracticeParticipationResponse> changeStatus(
        @PathVariable Long id,
        @Valid @RequestBody PracticeParticipationStatusUpdateRequest request,
        @AuthenticationPrincipal User user
    )   {
    return ResponseEntity.ok(practiceParticipationService.changeStatus(id, request, user));
    }


    @PostMapping("/{id}/invite")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeParticipationResponse> inviteMember(
            @PathVariable Long id,
            @Valid @RequestBody PracticeParticipationInviteRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.inviteMember(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        practiceParticipationService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
