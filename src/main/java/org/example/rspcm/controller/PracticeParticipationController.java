package org.example.rspcm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.practice.*;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.PracticeParticipationStatus;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/practice-participations")
public class PracticeParticipationController {

    private final PracticeParticipationService practiceParticipationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Page<PracticeParticipationResponse>> getAll(
            @RequestParam Long examId,
            @RequestParam(required = false) PracticeParticipationStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @AuthenticationPrincipal User user
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(practiceParticipationService.findAll(examId, status, user, pageable));
    }

    @GetMapping("/{participationId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PracticeParticipationResponse> getById(
            @PathVariable Long participationId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.findById(participationId, user));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<MyPracticeParticipationResponse>> getMyParticipation(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.getMyParticipations(user));
    }

    @GetMapping("/members/invitations/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<MyTeamInvitationResponse>> getMyTeamInvitations(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.getMyTeamInvitations(user));
    }

    @PostMapping("/{participationId}/members/invite")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeParticipationResponse> inviteMembers(
            @PathVariable Long participationId,
            @Valid @RequestBody PracticeParticipationMembersInviteRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.inviteMembers(participationId, request, user));
    }

    @PostMapping("/{participationId}/members/accept")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeParticipationResponse> acceptInvitation(
            @PathVariable Long participationId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.acceptInvitation(participationId, user));
    }

    @PostMapping("/{participationId}/members/decline")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeParticipationResponse> declineInvitation(
            @PathVariable Long participationId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.declineInvitation(participationId, user));
    }

    @DeleteMapping("/{participationId}/members/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> leaveMyTeam(
            @PathVariable Long participationId,
            @AuthenticationPrincipal User user
    ) {
        practiceParticipationService.leaveMyTeam(participationId, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{participationId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long participationId,
            @AuthenticationPrincipal User user
    ) {
        practiceParticipationService.delete(participationId, user);
        return ResponseEntity.noContent().build();
    }
}
