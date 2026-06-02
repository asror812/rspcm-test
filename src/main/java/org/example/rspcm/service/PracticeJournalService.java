package org.example.rspcm.service;

import org.example.rspcm.dto.practice.PracticeJournalRequest;
import org.example.rspcm.dto.practice.PracticeJournalResponse;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.PracticeJournalMapper;
import org.example.rspcm.model.entity.*;
import org.example.rspcm.model.enums.LogbookEntryStatus;
import org.example.rspcm.model.enums.LogbookStatus;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.example.rspcm.repository.PracticeLogbookEntryRepository;
import org.example.rspcm.repository.PracticeJournalRepository;
import org.example.rspcm.repository.PracticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PracticeJournalMapper practiceJournalMapper;
    private final TeacherProfileRepository teacherProfileRepository;
    private final MessageService messageService;

    @Transactional(readOnly = true)
    public List<PracticeJournalResponse> findMineResponse() {
        User student = currentUser();
        return journalRepository.findByStudentId(student.getId()).stream().map(practiceJournalMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PracticeJournalResponse> findByPracticeResponse(Long practiceId) {
        User user = currentUser();
        Practice practice = practiceRepository.findById(practiceId)
                .orElseThrow(() -> new NotFoundException(messageService.get("error.practice.not.found", practiceId)));

        if (hasRole(user, RoleName.ROLE_TEACHER)) {
            Long subjectId = practice.getSubject() == null ? null : practice.getSubject().getId();
            if (subjectId == null || !teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(user.getId(), subjectId)) {
                throw new NotFoundException(messageService.get("error.practice.not.found", practiceId));
            }
        }

        return journalRepository.findByPracticeId(practiceId).stream().map(practiceJournalMapper::toResponse).toList();
    }

    public PracticeJournalResponse submitResponse(PracticeJournalRequest request) {
        User student = currentUser();
        PracticeLogbook savedLogbook = saveLogbookAndEntry(student, request);

        PracticeLogbook reloaded = journalRepository.findById(savedLogbook.getId())
                .orElseThrow(() -> new NotFoundException("Журнал практики не найден: " + savedLogbook.getId()));
        return practiceJournalMapper.toResponse(reloaded);
    }

    private PracticeLogbook saveLogbookAndEntry(User student, PracticeJournalRequest request) {
        Practice practice = practiceRepository.findById(request.practiceId())
                .orElseThrow(() -> new NotFoundException(messageService.get("error.practice.not.found", request.practiceId())));

        boolean isDraft = Boolean.TRUE.equals(request.draft());
        LocalDateTime now = LocalDateTime.now();

        PracticeLogbook logbook = journalRepository
                .findFirstByPracticeIdAndStudentId(practice.getId(), student.getId())
                .orElseGet(() -> PracticeLogbook.builder()
                        .practice(practice)
                        .student(student)
                        .build());

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
        return savedLogbook;
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == roleName);
    }
}
