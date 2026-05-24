package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.service.PracticeParticipationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/practice-participations")
public class StudentPracticeParticipationController {

    private final PracticeParticipationService practiceParticipationService;

    @GetMapping("/{participationId}/members/available")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<UserSummary>> getAvailableStudents(
            @PathVariable Long participationId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(practiceParticipationService.getAvailableStudentsForInvite(participationId, user));
    }

    @DeleteMapping("/{participationId}/members/{memberId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long participationId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal User user
    ) {
        practiceParticipationService.removeMember(participationId, memberId, user);
        return ResponseEntity.noContent().build();
    }
}
