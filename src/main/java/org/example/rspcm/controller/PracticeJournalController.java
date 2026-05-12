package org.example.rspcm.controller;

import org.example.rspcm.dto.practice.PracticeJournalRequest;
import org.example.rspcm.dto.practice.PracticeJournalResponse;
import org.example.rspcm.service.PracticeJournalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/practice-journals")
public class PracticeJournalController {

    private final PracticeJournalService journalService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<PracticeJournalResponse>> myJournals() {
        return ResponseEntity.ok(journalService.findMineResponse());
    }

    @GetMapping({"/practice/{practiceId}", "/practical-task/{practicalTaskId}"})
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<PracticeJournalResponse>> byPracticalTask(
            @PathVariable(value = "practiceId", required = false) Long practiceId,
            @PathVariable(value = "practicalTaskId", required = false) Long practicalTaskId
    ) {
        Long resolvedTaskId = practicalTaskId != null ? practicalTaskId : practiceId;
        return ResponseEntity.ok(journalService.findByPracticalTaskResponse(resolvedTaskId));
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeJournalResponse> submit(@Valid @RequestBody PracticeJournalRequest request) {
        return ResponseEntity.ok(journalService.submitResponse(request));
    }
}
