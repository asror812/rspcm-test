package org.example.rspcm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.group.AdminGroupResponse;
import org.example.rspcm.dto.group.GroupRequest;
import org.example.rspcm.dto.group.GroupResponse;
import org.example.rspcm.service.StudyGroupService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/groups")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGroupController {

    private final StudyGroupService groupService;

    @GetMapping
    public ResponseEntity<List<AdminGroupResponse>> getAll() {
        return ResponseEntity.ok(groupService.findAllForAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminGroupResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.findAdminResponseById(id));
    }

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupRequest request) {
        return ResponseEntity.ok(groupService.createResponse(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody GroupRequest request
    ) {
        return ResponseEntity.ok(groupService.update(id, request));
    }

    @PostMapping(value = "/{id}/import-students", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Integer>> importStudents(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(groupService.importStudentsFromExcel(id, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        groupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
