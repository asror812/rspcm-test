package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.practice.PracticeParticipationRequest;
import org.example.rspcm.dto.practice.PracticeParticipationResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.SummaryMapper;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamPractice;
import org.example.rspcm.model.entity.PracticeParticipation;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.PracticeParticipationStatus;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.ExamPracticeRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.PracticeParticipationRepository;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PracticeParticipationService {

    private final PracticeParticipationRepository practiceParticipationRepository;
    private final ExamPracticeRepository examPracticeRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final SummaryMapper summaryMapper;
    private final ExamRepository examRepository;

    @Transactional
    public PracticeParticipationResponse create(PracticeParticipationRequest request, User user) {
        ExamPractice examPractice = resolveExamPractice(request.examPracticeId());

        checkExamPractice(examPractice);

        PracticeParticipation participation = new PracticeParticipation();
        participation.setExamPractice(examPractice);
        participation.setCreatedAt(LocalDateTime.now());

        return toResponse(practiceParticipationRepository.save(participation));
    }

    private void checkExamPractice(ExamPractice examPractice) {
        if (!practiceParticipationRepository.findByExamPracticeExamId(examPractice.getId()).isEmpty()) {

        }
    }

    public Page<PracticeParticipationResponse> findAll(Long examId, User user, Pageable pageable) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new NotFoundException("Exam topilmadi: " + examId));

        validateAccess(user, exam);

        return practiceParticipationRepository.findByExamPracticeExamId(examId, pageable)
                .map(this::toResponse);
    }

    public PracticeParticipationResponse findById(Long id, User user) {
        PracticeParticipation participation = findEntityById(id);
        validateAccess(user, participation.getExamPractice().getExam());
        return toResponse(participation);
    }

    @Transactional
    public PracticeParticipationResponse update(Long id, PracticeParticipationRequest request, User user) {
        PracticeParticipation participation = findEntityById(id);
        ExamPractice examPractice = resolveExamPractice(request.examPracticeId());
        validateAccess(user, participation.getExamPractice().getExam());
        validateAccess(user, examPractice.getExam());

        participation.setExamPractice(examPractice);
        participation.setStatus(request.status());
        return toResponse(practiceParticipationRepository.save(participation));
    }

    @Transactional
    public void delete(Long id, User user) {
        PracticeParticipation participation = findEntityById(id);
        validateAccess(user, participation.getExamPractice().getExam());
        practiceParticipationRepository.delete(participation);
    }

    private PracticeParticipation findEntityById(Long id) {
        return practiceParticipationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PracticeParticipation topilmadi: " + id));
    }

    private ExamPractice resolveExamPractice(Long examPracticeId) {
        return examPracticeRepository.findById(examPracticeId)
                .orElseThrow(() -> new NotFoundException("ExamPractice topilmadi: " + examPracticeId));
    }

    private void validateAccess(User user, Exam exam) {
        if (!canAccess(user, exam)) {
            throw new ErrorMessageException("Faqat o'zingizga biriktirilgan fan examlarini boshqara olasiz", ErrorCodes.Forbidden);
        }
    }

    private boolean canAccess(User user, Exam exam) {
        if (isAdmin(user)) {
            return true;
        }
        if (!isTeacher(user) || exam.getSubject() == null) {
            return false;
        }
        return teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(user.getId(), exam.getSubject().getId());
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);
    }

    private boolean isTeacher(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_TEACHER);
    }

    private PracticeParticipationResponse toResponse(PracticeParticipation participation) {
        ExamPractice examPractice = participation.getExamPractice();
        return new PracticeParticipationResponse(
                participation.getId(),
                examPractice.getId(),
                examPractice.getExam().getId(),
                summaryMapper.toPracticeSummary(examPractice.getPractice()),
                participation.getCreatedAt(),
                participation.getStatus()
        );
    }
}
