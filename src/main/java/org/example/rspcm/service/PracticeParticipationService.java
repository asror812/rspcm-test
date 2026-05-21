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
import org.example.rspcm.model.entity.PracticeParticipationMember;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;
import org.example.rspcm.model.enums.PracticeParticipationStatus;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.ExamPracticeRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.PracticeParticipationMemberRepository;
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
    private final PracticeParticipationMemberRepository participationMemberRepository;
    private final ExamPracticeRepository examPracticeRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final SummaryMapper summaryMapper;
    private final ExamRepository examRepository;

    @Transactional
    public PracticeParticipationResponse create(PracticeParticipationRequest request, User user) {
        Exam exam = resolveExam(request.examId());
        checkPracticeExam(exam);

        PracticeParticipation participation = new PracticeParticipation();
        participation.setExam(exam);
        participation.setExamPractice(null);
        participation.setCreatedAt(LocalDateTime.now());
        participation.setStatus(PracticeParticipationStatus.FORMING);

        PracticeParticipation saved = practiceParticipationRepository.save(participation);

        PracticeParticipationMember leader = new PracticeParticipationMember();
        leader.setPracticeParticipation(saved);
        leader.setUser(user);
        leader.setRole(PracticeMemberRole.LEADER);
        leader.setStatus(PracticeParticipationMemberStatus.ACCEPTED);
        participationMemberRepository.save(leader);

        return toResponse(saved);
    }

    private void checkPracticeExam(Exam exam) {
        if (exam.getType() != ExamType.PRACTICE) {
            throw new ErrorMessageException("Faqat PRACTICE turidagi exam uchun participation yaratiladi", ErrorCodes.BadRequest);
        }
    }

    public Page<PracticeParticipationResponse> findAll(Long examId, User user, Pageable pageable) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new NotFoundException("Exam topilmadi: " + examId));

        validateAccess(user, exam);

        return practiceParticipationRepository.findByExamId(examId, pageable)
                .map(this::toResponse);
    }

    public PracticeParticipationResponse findById(Long id, User user) {
        PracticeParticipation participation = findEntityById(id);
        validateAccess(user, participation.getExam());
        return toResponse(participation);
    }

    @Transactional
    public PracticeParticipationResponse update(Long id, PracticeParticipationRequest request, User user) {
        PracticeParticipation participation = findEntityById(id);
        Exam exam = resolveExam(request.examId());
        checkPracticeExam(exam);
        if (isAdmin(user) || isTeacher(user)) {
            validateAccess(user, exam);
        } else if (!isStudent(user)) {
            throw new ErrorMessageException("Ruxsat yo'q", ErrorCodes.Forbidden);
        }

        if (!participation.getExam().getId().equals(exam.getId())) {
            throw new ErrorMessageException("Participation boshqa examga ko'chirilmaydi", ErrorCodes.BadRequest);
        }

        refreshReadyStatusIfEligible(participation);

        if (request.examPracticeId() != null) {
            requireLeaderOrStaff(user, participation);
            if (participation.getStatus() != PracticeParticipationStatus.READY_TO_CHOOSE) {
                throw new ErrorMessageException("Avval barcha a'zolar ACCEPTED bo'lishi kerak", ErrorCodes.BadRequest);
            }

            ExamPractice examPractice = resolveExamPractice(request.examPracticeId());
            if (!examPractice.getExam().getId().equals(exam.getId())) {
                throw new ErrorMessageException("Tanlangan practice ushbu examga tegishli emas", ErrorCodes.BadRequest);
            }

            participation.setExamPractice(examPractice);
            participation.setStatus(PracticeParticipationStatus.PRACTICE_CHOSEN);
            participation.setChosenAt(LocalDateTime.now());
        }

        return toResponse(practiceParticipationRepository.save(participation));
    }

    @Transactional
    public void delete(Long id, User user) {
        PracticeParticipation participation = findEntityById(id);
        validateAccess(user, participation.getExam());
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

    private Exam resolveExam(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + examId));
    }

    private void refreshReadyStatusIfEligible(PracticeParticipation participation) {
        if (participation.getStatus() != PracticeParticipationStatus.FORMING) {
            return;
        }

        boolean hasAnyMember = participationMemberRepository.existsByPracticeParticipationIdAndStatusNot(
                participation.getId(),
                PracticeParticipationMemberStatus.REMOVED
        );
        if (!hasAnyMember) {
            return;
        }

        boolean hasNonAccepted = participationMemberRepository.existsByPracticeParticipationIdAndStatusNot(
                participation.getId(),
                PracticeParticipationMemberStatus.ACCEPTED
        );
        if (!hasNonAccepted) {
            participation.setStatus(PracticeParticipationStatus.READY_TO_CHOOSE);
            participation.setReadyAt(LocalDateTime.now());
        }
    }

    private void requireLeaderOrStaff(User user, PracticeParticipation participation) {
        if (isAdmin(user) || isTeacher(user)) {
            return;
        }
        boolean isAcceptedLeader = participationMemberRepository.existsByPracticeParticipationIdAndUserIdAndRoleAndStatus(
                participation.getId(),
                user.getId(),
                PracticeMemberRole.LEADER,
                PracticeParticipationMemberStatus.ACCEPTED
        );
        if (!isAcceptedLeader) {
            throw new ErrorMessageException("Faqat jamoa lideri yoki o'qituvchi practice tanlashi mumkin", ErrorCodes.Forbidden);
        }
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

    private boolean isStudent(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_STUDENT);
    }

    private PracticeParticipationResponse toResponse(PracticeParticipation participation) {
        ExamPractice examPractice = participation.getExamPractice();
        return new PracticeParticipationResponse(
                participation.getId(),
                examPractice == null ? null : examPractice.getId(),
                participation.getExam().getId(),
                examPractice == null ? null : summaryMapper.toPracticeSummary(examPractice.getPractice()),
                participation.getCreatedAt(),
                participation.getStatus()
        );
    }
}
