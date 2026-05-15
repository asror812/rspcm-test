package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.group.TeacherGroupResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.service.StudyGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teacher/groups")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherGroupController {

    private final StudyGroupService groupService;

    @GetMapping
    public ResponseEntity<List<TeacherGroupResponse>> getOwnGroups(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.findOwnTeacherGroups(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherGroupResponse> getOwnGroupById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.findOwnTeacherGroupById(id, user));
    }
}
