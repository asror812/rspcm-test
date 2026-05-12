package org.example.rspcm.service;

import org.example.rspcm.dto.practice.PracticeJournalRequest;
import org.example.rspcm.dto.practice.PracticeJournalResponse;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.PracticeJournalMapper;
import org.example.rspcm.model.entity.*;
import org.example.rspcm.model.enums.LogbookEntryStatus;
import org.example.rspcm.model.enums.LogbookStatus;
import org.example.rspcm.repository.PracticeLogbookEntryRepository;
import org.example.rspcm.repository.PracticeJournalRepository;
import org.example.rspcm.repository.PracticeRepository;
import org.example.rspcm.repository.PracticeTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PracticeJournalService {

    private final PracticeJournalRepository journalRepository;
    private final PracticeLogbookEntryRepository entryRepository;
    private final PracticeRepository practiceRepository;
    private final PracticeTeamRepository teamRepository;
    private final CurrentUserService currentUserService;

    public List<PracticeLogbook> findMine() {
        User student = currentUserService.getCurrentUser();
        return journalRepository.findByStudentId(student.getId());
    }

    public List<PracticeJournalResponse> findMineResponse() {
        User student = currentUserService.getCurrentUser();
        return journalRepository.findByStudentId(student.getId()).stream().map(PracticeJournalMapper::toResponse).toList();
    }

    public List<PracticeLogbook> findByPracticalTask(Long practicalTaskId) {
        return journalRepository.findByPracticalTaskId(practicalTaskId);
    }

    public List<PracticeJournalResponse> findByPracticalTaskResponse(Long practicalTaskId) {
        return journalRepository.findByPracticalTaskId(practicalTaskId).stream().map(PracticeJournalMapper::toResponse).toList();
    }

    @Transactional
    public PracticeLogbook submit(PracticeJournalRequest request) {
        User student = currentUserService.getCurrentUser();
        PracticalTask practicalTask = practiceRepository.findById(request.practiceId())
                .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + request.practiceId()));

        PracticeTeam team = null;
        if (request.teamId() != null) {
            team = teamRepository.findById(request.teamId())
                    .orElseThrow(() -> new NotFoundException("PracticalTask team topilmadi: " + request.teamId()));
        }

        boolean isDraft = Boolean.TRUE.equals(request.draft());
        LocalDateTime now = LocalDateTime.now();

        PracticeLogbook logbook = journalRepository
                .findFirstByPracticalTaskIdAndStudentId(practicalTask.getId(), student.getId())
                .orElseGet(() -> PracticeLogbook.builder()
                        .practicalTask(practicalTask)
                        .student(student)
                        .build());

        logbook.setTeam(team);
        logbook.setFilePath(request.filePath());
        logbook.setSubmittedAt(now);
        logbook.setStatus(isDraft ? LogbookStatus.DRAFT : LogbookStatus.SUBMITTED);
        PracticeLogbook savedLogbook = journalRepository.save(logbook);

        PracticeLogbookEntry entry = entryRepository
                .findFirstByLogbookIdAndEntryDate(savedLogbook.getId(), request.entryDate())
                .orElseGet(() -> PracticeLogbookEntry.builder()
                        .logbook(savedLogbook)
                        .entryDate(request.entryDate())
                        .build());

        entry.setContent(request.content());
        entry.setStatus(isDraft ? LogbookEntryStatus.DRAFT : LogbookEntryStatus.SUBMITTED);
        entry.setSubmittedAt(now);
        entryRepository.save(entry);

        return journalRepository.findById(savedLogbook.getId())
                .orElseThrow(() -> new NotFoundException("Logbook topilmadi: " + savedLogbook.getId()));
    }

    public PracticeJournalResponse submitResponse(PracticeJournalRequest request) {
        User student = currentUserService.getCurrentUser();
        PracticalTask practicalTask = practiceRepository.findById(request.practiceId())
                .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + request.practiceId()));

        PracticeTeam team = null;
        if (request.teamId() != null) {
            team = teamRepository.findById(request.teamId())
                    .orElseThrow(() -> new NotFoundException("PracticalTask team topilmadi: " + request.teamId()));
        }

        boolean isDraft = Boolean.TRUE.equals(request.draft());
        LocalDateTime now = LocalDateTime.now();

        PracticeLogbook logbook = journalRepository
                .findFirstByPracticalTaskIdAndStudentId(practicalTask.getId(), student.getId())
                .orElseGet(() -> PracticeLogbook.builder()
                        .practicalTask(practicalTask)
                        .student(student)
                        .build());

        logbook.setTeam(team);
        logbook.setFilePath(request.filePath());
        logbook.setSubmittedAt(now);
        logbook.setStatus(isDraft ? LogbookStatus.DRAFT : LogbookStatus.SUBMITTED);
        PracticeLogbook savedLogbook = journalRepository.save(logbook);

        PracticeLogbookEntry entry = entryRepository
                .findFirstByLogbookIdAndEntryDate(savedLogbook.getId(), request.entryDate())
                .orElseGet(() -> PracticeLogbookEntry.builder()
                        .logbook(savedLogbook)
                        .entryDate(request.entryDate())
                        .build());

        entry.setContent(request.content());
        entry.setStatus(isDraft ? LogbookEntryStatus.DRAFT : LogbookEntryStatus.SUBMITTED);
        entry.setSubmittedAt(now);
        entryRepository.save(entry);

        PracticeLogbook reloaded = journalRepository.findById(savedLogbook.getId())
                .orElseThrow(() -> new NotFoundException("Logbook topilmadi: " + savedLogbook.getId()));
        return PracticeJournalMapper.toResponse(reloaded);
    }
}
