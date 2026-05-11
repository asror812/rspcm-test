package org.example.rspcm.controller;

import org.example.rspcm.dto.profile.StudentProfileResponse;
import org.example.rspcm.dto.profile.StudentProfileUpdateRequest;
import org.example.rspcm.dto.profile.TeacherProfileResponse;
import org.example.rspcm.dto.profile.TeacherProfileUpdateRequest;
import org.example.rspcm.service.CurrentUserService;
import org.example.rspcm.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final CurrentUserService currentUserService;

    @GetMapping("/students/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<StudentProfileResponse> getStudentProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getStudentProfileResponse(userId));
    }

    @PutMapping("/students/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<StudentProfileResponse> updateStudentProfile(@PathVariable Long userId,
                                                                       @Valid @RequestBody StudentProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateStudentProfileResponse(userId, request));
    }

    @GetMapping("/students/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileResponse> myStudentProfile() {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(profileService.getStudentProfileResponse(userId));
    }

    @PutMapping("/students/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileResponse> updateMyStudentProfile(@Valid @RequestBody StudentProfileUpdateRequest request) {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(profileService.updateStudentProfileResponse(userId, request));
    }

    @GetMapping("/teachers/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<TeacherProfileResponse> getTeacherProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getTeacherProfileResponse(userId));
    }

    @PutMapping("/teachers/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<TeacherProfileResponse> updateTeacherProfile(@PathVariable Long userId,
                                                                       @Valid @RequestBody TeacherProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateTeacherProfileResponse(userId, request));
    }

    @GetMapping("/teachers/me")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<TeacherProfileResponse> myTeacherProfile() {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(profileService.getTeacherProfileResponse(userId));
    }

    @PutMapping("/teachers/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherProfileResponse> updateMyTeacherProfile(@Valid @RequestBody TeacherProfileUpdateRequest request) {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(profileService.updateTeacherProfileResponse(userId, request));
    }
}
