package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.student.StudentDashboardResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.service.StudentDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student-dashboard")
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentDashboardResponse> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(studentDashboardService.getMe(user));
    }
}
