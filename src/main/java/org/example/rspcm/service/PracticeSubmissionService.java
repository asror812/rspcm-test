package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.practice.PracticeSubmissionResponse;
import org.example.rspcm.dto.practice.PracticeSubmissionReviewRequest;
import org.example.rspcm.dto.practice.PracticeSubmissionSubmitRequest;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.SummaryMapper;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.PracticeParticipation;
import org.example.rspcm.model.entity.PracticeSubmission;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;
import org.example.rspcm.model.enums.PracticeParticipationStatus;
import org.example.rspcm.model.enums.PracticeSubmissionStatus;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.PracticeParticipationMemberRepository;
import org.example.rspcm.repository.PracticeParticipationRepository;
import org.example.rspcm.repository.PracticeSubmissionRepository;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.example.rspcm.repository.ExamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PracticeSubmissionService {

    private final PracticeSubmissionRepository submissionRepository;
    private final PracticeParticipationRepository participationRepository;
    private final PracticeParticipationMemberRepository participationMemberRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final ExamRepository examRepository;
    private final SummaryMapper summaryMapper;
    private final FcmService fcmService;

    public PracticeSubmissionResponse getByParticipation(Long participationId, User user) {
        PracticeParticipation participation = findParticipation(participationId);
        validateCanView(user, participation);

        PracticeSubmission submission = submissionRepository.findByExamParticipationId(participationId)
                .orElseThrow(() -> new NotFoundException("Отправка практики не найдена"));
        return toResponse(submission);
    }

    public PracticeSubmissionResponse getById(Long submissionId, User user) {
        PracticeSubmission submission = findSubmission(submissionId);
        validateCanView(user, submission.getExamParticipation());
        return toResponse(submission);
    }

    public Page<PracticeSubmissionResponse> findAllByExam(Long examId, PracticeSubmissionStatus status, User user, Pageable pageable) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("Экзамен не найден: " + examId));
        validateStaffAccess(user, exam);

        Page<PracticeSubmission> page = status == null
                ? submissionRepository.findByExamParticipationExamId(examId, pageable)
                : submissionRepository.findByExamParticipationExamIdAndStatus(examId, status, pageable);

        return page.map(this::toResponse);
    }

    @Transactional
    public PracticeSubmissionResponse submit(Long participationId, PracticeSubmissionSubmitRequest request, User user) {
        PracticeParticipation participation = findParticipation(participationId);
        validateLeaderSubmit(user, participation);

        PracticeSubmission submission = submissionRepository.findByExamParticipationId(participationId)
                .orElseGet(() -> PracticeSubmission.builder()
                        .examParticipation(participation)
                        .status(PracticeSubmissionStatus.RETURNED)
                        .build());

        submission.setStudent(user);
        submission.setTextAnswer(request.textAnswer());
        submission.setFileUrl(request.fileUrl());
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(PracticeSubmissionStatus.SUBMITTED);

        return toResponse(submissionRepository.save(submission));
    }

    @Transactional
    public PracticeSubmissionResponse grade(Long submissionId, PracticeSubmissionReviewRequest request, User user) {
        PracticeSubmission submission = findSubmission(submissionId);
        validateStaffAccess(user, submission.getExamParticipation().getExam());

        if (submission.getStatus() != PracticeSubmissionStatus.SUBMITTED) {
            throw new ErrorMessageException("Оценивать можно только работы в статусе SUBMITTED", ErrorCodes.BadRequest);
        }

        submission.setStatus(PracticeSubmissionStatus.GRADED);
        submission.setTeacherComment(request.teacherComment());
        PracticeSubmissionResponse result = toResponse(submissionRepository.save(submission));

        notifyParticipationMembers(submission, "Практика проверена",
                "Ваша работа по практике «" + getPracticeName(submission) + "» оценена преподавателем.");
        return result;
    }

    @Transactional
    public PracticeSubmissionResponse returnSubmission(Long submissionId, PracticeSubmissionReviewRequest request, User user) {
        PracticeSubmission submission = findSubmission(submissionId);
        validateStaffAccess(user, submission.getExamParticipation().getExam());

        if (submission.getStatus() == PracticeSubmissionStatus.RETURNED) {
            throw new ErrorMessageException("Submission уже в статусе RETURNED", ErrorCodes.BadRequest);
        }

        submission.setStatus(PracticeSubmissionStatus.RETURNED);
        submission.setTeacherComment(request.teacherComment());
        PracticeSubmissionResponse result = toResponse(submissionRepository.save(submission));

        notifyParticipationMembers(submission, "Практика возвращена на доработку",
                "Ваша работа по практике «" + getPracticeName(submission) + "» возвращена. Проверьте комментарий преподавателя.");
        return result;
    }

    private PracticeParticipation findParticipation(Long participationId) {
        return participationRepository.findById(participationId)
                .orElseThrow(() -> new NotFoundException("Участие в практике не найдено: " + participationId));
    }

    private PracticeSubmission findSubmission(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Отправка практики не найдена: " + submissionId));
    }

    private void validateLeaderSubmit(User user, PracticeParticipation participation) {
        if (participation.getStatus() != PracticeParticipationStatus.PRACTICE_CHOSEN || participation.getExamPractice() == null) {
            throw new ErrorMessageException("Сначала нужно выбрать практику", ErrorCodes.BadRequest);
        }

        boolean isAcceptedLeader = participationMemberRepository.existsByPracticeParticipationIdAndUserIdAndRoleAndStatus(
                participation.getId(),
                user.getId(),
                PracticeMemberRole.LEADER,
                PracticeParticipationMemberStatus.ACCEPTED
        );
        if (!isAcceptedLeader) {
            throw new ErrorMessageException("Только лидер участия может отправить submission", ErrorCodes.Forbidden);
        }
    }

    private void validateCanView(User user, PracticeParticipation participation) {
        if (isAdmin(user) || isTeacher(user)) {
            validateStaffAccess(user, participation.getExam());
            return;
        }
        boolean isMember = participationMemberRepository.existsByPracticeParticipationIdAndUserIdAndStatus(
                participation.getId(),
                user.getId(),
                PracticeParticipationMemberStatus.ACCEPTED
        );
        if (!isMember) {
            throw new ErrorMessageException("Нет доступа", ErrorCodes.Forbidden);
        }
    }

    private void validateStaffAccess(User user, Exam exam) {
        if (isAdmin(user)) {
            return;
        }
        if (!isTeacher(user)) {
            throw new ErrorMessageException("Нет доступа", ErrorCodes.Forbidden);
        }
        Long subjectId = exam.getSubject() == null ? null : exam.getSubject().getId();
        if (subjectId == null || !teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(user.getId(), subjectId)) {
            throw new ErrorMessageException("Вы можете управлять submissions только по закреплённым за вами предметам", ErrorCodes.Forbidden);
        }
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);
    }

    private boolean isTeacher(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_TEACHER);
    }

    private void notifyParticipationMembers(PracticeSubmission submission, String title, String body) {
        try {
            List<User> members = participationMemberRepository
                    .findByPracticeParticipationId(submission.getExamParticipation().getId())
                    .stream()
                    .filter(m -> m.getStatus() == org.example.rspcm.model.enums.PracticeParticipationMemberStatus.ACCEPTED)
                    .map(org.example.rspcm.model.entity.PracticeParticipationMember::getUser)
                    .toList();
            fcmService.sendToUsers(members, title, body);
        } catch (Exception e) {
            // notification failure must never break the main flow
        }
    }

    private String getPracticeName(PracticeSubmission submission) {
        if (submission.getExamParticipation().getExamPractice() == null) return "практики";
        return submission.getExamParticipation().getExamPractice().getPractice().getName();
    }

    private PracticeSubmissionResponse toResponse(PracticeSubmission submission) {
        return new PracticeSubmissionResponse(
                submission.getId(),
                submission.getExamParticipation().getId(),
                submission.getExamParticipation().getExam().getId(),
                submission.getExamParticipation().getExamPractice() == null ? null : submission.getExamParticipation().getExamPractice().getId(),
                submission.getStudent() == null ? null : summaryMapper.toUserSummary(submission.getStudent()),
                submission.getTextAnswer(),
                submission.getFileUrl(),
                submission.getSubmittedAt(),
                submission.getStatus(),
                submission.getTeacherComment()
        );
    }
}
