package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.AdminDashboardGeneralStatsResponse;
import org.example.rspcm.dto.AdminRecentReportResponse;
import org.example.rspcm.dto.group.GroupResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.service.AdminDashboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin-dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/general-stats")
    public ResponseEntity<AdminDashboardGeneralStatsResponse> stats() {
        return ResponseEntity.ok(dashboardService.getGeneralStats());
    }

    @GetMapping("/recent-reports")
    public ResponseEntity<Page<AdminRecentReportResponse>> recentReports(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        return ResponseEntity.ok(dashboardService.getRecentReports(user, pageable));
    }

    @GetMapping("/groups")
    public ResponseEntity<List<GroupResponse>> getOwnStudyGroups(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getOwnStudyGroups(user));
    }


}
