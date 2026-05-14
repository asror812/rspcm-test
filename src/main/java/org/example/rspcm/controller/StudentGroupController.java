package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.group.StudentGroupResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.service.StudyGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/groups")
@PreAuthorize("hasRole('STUDENT')")
public class StudentGroupController {

    private final StudyGroupService groupService;

    @GetMapping
    public ResponseEntity<List<StudentGroupResponse>> getOwnGroups(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.findOwnStudentGroups(user));
    }
}
