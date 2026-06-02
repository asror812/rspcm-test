package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.practice.*;
import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.SummaryMapper;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamPractice;
import org.example.rspcm.model.entity.PracticeParticipation;
import org.example.rspcm.model.entity.PracticeParticipationMember;
import org.example.rspcm.model.entity.PracticeSubmission;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.model.enums.ExamStatus;
import org.example.rspcm.model.enums.NotificationType;
import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;
import org.example.rspcm.model.enums.PracticeParticipationStatus;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.model.enums.WorkMode;
import org.example.rspcm.repository.ExamPracticeRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.PracticeParticipationMemberRepository;
import org.example.rspcm.repository.PracticeParticipationRepository;
import org.example.rspcm.repository.PracticeSubmissionAttemptRepository;
import org.example.rspcm.repository.PracticeSubmissionRepository;
import org.example.rspcm.repository.UserRepository;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Comparator;
import java.util.Set;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class PracticeParticipationService {

    private final PracticeParticipationRepository practiceParticipationRepository;
    private final PracticeParticipationMemberRepository participationMemberRepository;
    private final ExamPracticeRepository examPracticeRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final UserRepository userRepository;
    private final SummaryMapper summaryMapper;
    private final ExamRepository examRepository;
    private final PracticeSubmissionRepository submissionRepository;
    private final PracticeSubmissionAttemptRepository submissionAttemptRepository;
    private final NotificationService notificationService;
    private final FcmService fcmService;
    private final MessageService messageService;

    private void checkPracticeExam(Exam exam) {
        if (exam.getType() != ExamType.PRACTICE) {
            throw new ErrorMessageException(messageService.get("error.participation.exam.type"), ErrorCodes.BadRequest);
        }
    }

    @Transactional(readOnly = true)
    public Page<PracticeParticipationResponse> findAll(
            Long examId,
            PracticeParticipationStatus status,
            User user,
            Pageable pageable
    ) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new NotFoundException(messageService.get("error.exam.not.found", examId)));

        validateAccess(user, exam);

        Page<PracticeParticipation> page = status == null
                ? practiceParticipationRepository.findByExamId(examId, pageable)
                : practiceParticipationRepository.findByExamIdAndStatus(examId, status, pageable);

        return page
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PracticeParticipationResponse findById(Long id, User user) {
        PracticeParticipation participation = findEntityById(id);
        validateAccess(user, participation.getExam());
        return toResponse(participation);
    }

    @Transactional
    public PracticeParticipationResponse inviteMembers(Long participationId, PracticeParticipationMembersInviteRequest request, User user) {
        PracticeParticipation participation = findEntityById(participationId);

        boolean isLeader = participationMemberRepository.existsByPracticeParticipationIdAndUserIdAndRoleAndStatus(
                participation.getId(),
                user.getId(),
                PracticeMemberRole.LEADER,
                PracticeParticipationMemberStatus.ACCEPTED
        );
        if (!isLeader) {
            throw new ErrorMessageException(messageService.get("error.leader.only.invite"), ErrorCodes.Forbidden);
        }

        ExamPractice examPractice = participation.getExamPractice();
        if (examPractice == null || examPractice.getPractice().getWorkMode() != WorkMode.TEAM) {
            throw new ErrorMessageException(messageService.get("error.team.invite.individual"), ErrorCodes.BadRequest);
        }

        Integer teamSize = examPractice.getPractice().getTeamSize();
        if (teamSize == null || teamSize <= 1) {
            throw new ErrorMessageException(messageService.get("error.team.size.one"), ErrorCodes.BadRequest);
        }

        Set<Long> studentIds = new HashSet<>(request.studentIds());
        if (studentIds.contains(user.getId())) {
            throw new ErrorMessageException(messageService.get("error.leader.self.invite"), ErrorCodes.BadRequest);
        }

        long activeMembers = participationMemberRepository.countByPracticeParticipationIdAndStatusNot(
                participation.getId(),
                PracticeParticipationMemberStatus.REMOVED
        );
        if (activeMembers + studentIds.size() > teamSize) {
            throw new ErrorMessageException(messageService.get("error.team.size.exceeded"), ErrorCodes.BadRequest);
        }

        for (Long studentId : studentIds) {
            User invitee = userRepository.findById(studentId)
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + studentId));

            if (!isStudent(invitee)) {
                throw new ErrorMessageException(messageService.get("error.invite.student.only"), ErrorCodes.BadRequest);
            }

            if (!isAssignedToStudent(participation.getExam(), invitee.getId())) {
                throw new ErrorMessageException(messageService.get("error.invite.not.eligible"), ErrorCodes.BadRequest);
            }

            boolean usedInAnotherParticipation = participationMemberRepository
                    .existsByPracticeParticipationExamIdAndUserIdAndStatusAndPracticeParticipationIdNot(
                            participation.getExam().getId(),
                            invitee.getId(),
                            PracticeParticipationMemberStatus.ACCEPTED,
                            participation.getId()
                    );
            if (usedInAnotherParticipation) {
                throw new ErrorMessageException(messageService.get("error.student.in.other.team"), ErrorCodes.BadRequest);
            }

            boolean alreadyInParticipation = participationMemberRepository
                    .findByPracticeParticipationIdAndUserId(participation.getId(), invitee.getId())
                    .isPresent();
            if (alreadyInParticipation) {
                throw new ErrorMessageException(messageService.get("error.student.already.in.participation"), ErrorCodes.AlreadyExists);
            }
        }

        String practiceName = participation.getExamPractice() != null
                ? participation.getExamPractice().getPractice().getName()
                : "практики";
        String inviterName = participation.getMembers().stream()
                .filter(m -> m.getRole() == PracticeMemberRole.LEADER
                        && m.getStatus() == PracticeParticipationMemberStatus.ACCEPTED)
                .findFirst()
                .map(m -> m.getUser().getFirstName() + " " + m.getUser().getLastName())
                .orElse("Студент");

        for (Long studentId : studentIds) {
            User invitee = userRepository.findById(studentId)
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + studentId));

            PracticeParticipationMember member = new PracticeParticipationMember();
            member.setPracticeParticipation(participation);
            member.setUser(invitee);
            member.setRole(PracticeMemberRole.MEMBER);
            member.setStatus(PracticeParticipationMemberStatus.INVITED);
            participationMemberRepository.save(member);

            // Persist + push notification for the invited student
            try {
                String title = "Приглашение в команду";
                String body = inviterName + " приглашает вас в практику «" + practiceName + "»";
                notificationService.create(invitee, title, body,
                        NotificationType.TEAM_INVITATION, participation.getId());
                fcmService.sendToUser(invitee, title, body);
            } catch (Exception ignored) {}
        }

        participation.setStatus(PracticeParticipationStatus.WAITING_MEMBERS);
        participation.setReadyAt(null);

        return toResponse(practiceParticipationRepository.save(participation));
    }

    @Transactional
    public PracticeParticipationResponse acceptInvitation(Long participationId, User user) {
        PracticeParticipation participation = findEntityById(participationId);

        PracticeParticipationMember member = participationMemberRepository
                .findByPracticeParticipationIdAndUserId(participationId, user.getId())
                .orElseThrow(() -> new NotFoundException("Участник участия не найден"));

        if (member.getStatus() != PracticeParticipationMemberStatus.INVITED) {
            throw new ErrorMessageException(messageService.get("error.accept.not.invited"), ErrorCodes.BadRequest);
        }

        member.setStatus(PracticeParticipationMemberStatus.ACCEPTED);
        participationMemberRepository.save(member);

        ExamPractice examPractice = participation.getExamPractice();
        if (examPractice != null && examPractice.getPractice().getWorkMode() == WorkMode.TEAM) {
            Integer teamSize = examPractice.getPractice().getTeamSize();
            if (teamSize != null && teamSize > 1) {
                long acceptedCount = participationMemberRepository.countByPracticeParticipationIdAndStatus(
                        participationId,
                        PracticeParticipationMemberStatus.ACCEPTED
                );
                if (acceptedCount == teamSize.longValue()) {
                    participation.setStatus(PracticeParticipationStatus.READY_TO_CHOOSE);
                    participation.setReadyAt(LocalDateTime.now());
                } else {
                    participation.setStatus(PracticeParticipationStatus.WAITING_MEMBERS);
                }
            }
        }

        return toResponse(practiceParticipationRepository.save(participation));
    }

    @Transactional
    public PracticeParticipationResponse declineInvitation(Long participationId, User user) {
        PracticeParticipation participation = findEntityById(participationId);

        PracticeParticipationMember member = participationMemberRepository
                .findByPracticeParticipationIdAndUserId(participationId, user.getId())
                .orElseThrow(() -> new NotFoundException("Участник участия не найден"));

        if (member.getStatus() != PracticeParticipationMemberStatus.INVITED) {
            throw new ErrorMessageException(messageService.get("error.reject.not.invited"), ErrorCodes.BadRequest);
        }

        member.setStatus(PracticeParticipationMemberStatus.DECLINED);
        participationMemberRepository.save(member);

        participation.setStatus(PracticeParticipationStatus.WAITING_MEMBERS);
        participation.setReadyAt(null);

        return toResponse(practiceParticipationRepository.save(participation));
    }

    @Transactional(readOnly = true)
    public MyPracticeParticipationResponse getMyParticipationByExam(Long examId, User user) {
        if (!isStudent(user)) {
            throw new ErrorMessageException(messageService.get("error.no.access"), ErrorCodes.Forbidden);
        }

        Exam exam = resolveExam(examId);
        if (!isAssignedToStudent(exam, user.getId())) {
            throw new NotFoundException(messageService.get("error.exam.not.found", examId));
        }

        PracticeParticipationMember member = participationMemberRepository
                .findByPracticeParticipationExamIdAndUserIdAndStatusNot(
                        examId,
                        user.getId(),
                        PracticeParticipationMemberStatus.REMOVED
                )
                .orElseThrow(() -> new NotFoundException("Участие не найдено"));

        return toMyParticipationResponse(member.getPracticeParticipation());
    }

    @Transactional(readOnly = true)
    public List<MyPracticeParticipationResponse> getMyParticipations(User user) {
        if (!isStudent(user)) {
            throw new ErrorMessageException(messageService.get("error.no.access"), ErrorCodes.Forbidden);
        }

        List<PracticeParticipationMember> myMembers = participationMemberRepository
                .findByUserIdAndStatusNot(user.getId(), PracticeParticipationMemberStatus.REMOVED);

        LinkedHashMap<Long, PracticeParticipation> uniqParticipations = new LinkedHashMap<>();
        for (PracticeParticipationMember member : myMembers) {
            PracticeParticipation participation = member.getPracticeParticipation();
            uniqParticipations.putIfAbsent(participation.getId(), participation);
        }

        return uniqParticipations.values().stream()
                .map(this::toMyParticipationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyTeamInvitationResponse> getMyTeamInvitations(User user) {
        if (!isStudent(user)) {
            throw new ErrorMessageException(messageService.get("error.no.access"), ErrorCodes.Forbidden);
        }

        List<PracticeParticipationMember> invitedMembers = participationMemberRepository
                .findByUserIdAndStatus(user.getId(), PracticeParticipationMemberStatus.INVITED);

        return invitedMembers.stream()
                .map(this::toMyTeamInvitationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSummary> getAvailableStudentsForInvite(Long participationId, User user) {
        PracticeParticipation participation = findEntityById(participationId);
        requireLeader(user, participation);
        ensureTeamParticipation(participation);

        Exam exam = participation.getExam();
        Set<User> assigned = new HashSet<>(exam.getTargetStudents());
        for (var group : exam.getGroups()) {
            assigned.addAll(group.getStudents());
        }

        return assigned.stream()
                .filter(this::isStudent)
                .filter(candidate -> !candidate.getId().equals(user.getId()))
                .filter(candidate -> participationMemberRepository
                        .findByPracticeParticipationIdAndUserId(participationId, candidate.getId())
                        .isEmpty())
                .filter(candidate -> !participationMemberRepository
                        .existsByPracticeParticipationExamIdAndUserIdAndStatusAndPracticeParticipationIdNot(
                                exam.getId(),
                                candidate.getId(),
                                PracticeParticipationMemberStatus.ACCEPTED,
                                participationId
                        ))
                .map(summaryMapper::toUserSummary)
                .toList();
    }

    @Transactional
    public void removeMember(Long participationId, Long memberId, User user) {
        PracticeParticipation participation = findEntityById(participationId);
        requireLeader(user, participation);
        ensureTeamParticipation(participation);

        PracticeParticipationMember member = participationMemberRepository
                .findByIdAndPracticeParticipationId(memberId, participationId)
                .orElseThrow(() -> new NotFoundException("Участник участия не найден"));

        if (member.getRole() == PracticeMemberRole.LEADER) {
            throw new ErrorMessageException(messageService.get("error.cannot.remove.leader"), ErrorCodes.BadRequest);
        }

        member.setStatus(PracticeParticipationMemberStatus.REMOVED);
        participationMemberRepository.save(member);
        participation.setStatus(PracticeParticipationStatus.WAITING_MEMBERS);
        participation.setReadyAt(null);
        practiceParticipationRepository.save(participation);
    }

    @Transactional
    public void leaveMyTeam(Long participationId, User user) {
        if (!isStudent(user)) {
            throw new ErrorMessageException(messageService.get("error.leave.student.only"), ErrorCodes.Forbidden);
        }

        PracticeParticipation participation = findEntityById(participationId);
        ensureTeamParticipation(participation);

        PracticeParticipationMember myMember = participationMemberRepository
                .findByPracticeParticipationIdAndUserId(participationId, user.getId())
                .orElseThrow(() -> new NotFoundException("Участник участия не найден"));
        if (myMember.getStatus() == PracticeParticipationMemberStatus.REMOVED) {
            throw new ErrorMessageException(messageService.get("error.already.left"), ErrorCodes.BadRequest);
        }

        Exam exam = participation.getExam();
        if (exam.getEndAt() != null && LocalDateTime.now().isAfter(exam.getEndAt())) {
            throw new ErrorMessageException(messageService.get("error.deadline.passed"), ErrorCodes.AlreadyExists);
        }

        boolean hasSubmission = submissionRepository.findByExamParticipationId(participation.getId()).isPresent();
        if (hasSubmission) {
            throw new ErrorMessageException(messageService.get("error.leave.has.submission"), ErrorCodes.AlreadyExists);
        }

        List<PracticeParticipationMember> activeMembers = participationMemberRepository
                .findByPracticeParticipationId(participationId)
                .stream()
                .filter(member -> member.getStatus() != PracticeParticipationMemberStatus.REMOVED)
                .toList();

        if (activeMembers.size() <= 1) {
            participationMemberRepository.deleteByPracticeParticipationId(participationId);
            practiceParticipationRepository.delete(participation);
            return;
        }

        if (myMember.getRole() == PracticeMemberRole.LEADER) {
            PracticeParticipationMember nextLeader = activeMembers.stream()
                    .filter(member -> !member.getUser().getId().equals(user.getId()))
                    .min(Comparator.comparing(member -> member.getStatus() == PracticeParticipationMemberStatus.ACCEPTED ? 0 : 1))
                    .orElseThrow(() -> new ErrorMessageException("Новый лидер не найден", ErrorCodes.BadRequest));
            nextLeader.setRole(PracticeMemberRole.LEADER);
            participationMemberRepository.save(nextLeader);
        }

        myMember.setRole(PracticeMemberRole.MEMBER);
        myMember.setStatus(PracticeParticipationMemberStatus.REMOVED);
        participationMemberRepository.save(myMember);

        participation.setStatus(PracticeParticipationStatus.WAITING_MEMBERS);
        participation.setReadyAt(null);
        participation.setChosenAt(null);
        practiceParticipationRepository.save(participation);
    }

    @Transactional
    public void delete(Long id, User user) {
        PracticeParticipation participation = findEntityById(id);
        validateAccess(user, participation.getExam());
        practiceParticipationRepository.delete(participation);
    }

    @Transactional
    public void cancelMyParticipation(Long examId, User user) {
        if (!isStudent(user)) {
            throw new ErrorMessageException(messageService.get("error.cancel.student.only"), ErrorCodes.Forbidden);
        }

        Exam exam = resolveExam(examId);
        PracticeParticipationMember myMember = participationMemberRepository
                .findByPracticeParticipationExamIdAndUserIdAndStatusNot(
                        examId,
                        user.getId(),
                        PracticeParticipationMemberStatus.REMOVED
                )
                .orElseThrow(() -> new NotFoundException("Участие не найдено"));

        PracticeParticipation participation = myMember.getPracticeParticipation();
        if (participation.getExamPractice() == null) {
            throw new ErrorMessageException(messageService.get("error.practice.not.selected"), ErrorCodes.BadRequest);
        }

        if (exam.getEndAt() != null && LocalDateTime.now().isAfter(exam.getEndAt())) {
            throw new ErrorMessageException(messageService.get("error.deadline.passed"), ErrorCodes.AlreadyExists);
        }

        boolean hasSubmission = submissionRepository.findByExamParticipationId(participation.getId()).isPresent();
        if (hasSubmission) {
            throw new ErrorMessageException(messageService.get("error.cancel.has.submission"), ErrorCodes.AlreadyExists);
        }

        WorkMode workMode = participation.getExamPractice().getPractice().getWorkMode();
        if (workMode == WorkMode.INDIVIDUAL) {
            participationMemberRepository.deleteByPracticeParticipationId(participation.getId());
            practiceParticipationRepository.delete(participation);
            return;
        }

        if (workMode != WorkMode.TEAM) {
            throw new ErrorMessageException(messageService.get("error.unknown.work.mode"), ErrorCodes.BadRequest);
        }

        List<PracticeParticipationMember> activeMembers = participationMemberRepository
                .findByPracticeParticipationId(participation.getId())
                .stream()
                .filter(member -> member.getStatus() != PracticeParticipationMemberStatus.REMOVED)
                .toList();

        if (activeMembers.size() <= 1) {
            participationMemberRepository.deleteByPracticeParticipationId(participation.getId());
            practiceParticipationRepository.delete(participation);
            return;
        }

        if (myMember.getRole() != PracticeMemberRole.LEADER) {
            myMember.setStatus(PracticeParticipationMemberStatus.REMOVED);
            participationMemberRepository.save(myMember);
            participation.setStatus(PracticeParticipationStatus.WAITING_MEMBERS);
            participation.setReadyAt(null);
            participation.setChosenAt(null);
            practiceParticipationRepository.save(participation);
            return;
        }

        PracticeParticipationMember nextLeader = activeMembers.stream()
                .filter(member -> !member.getUser().getId().equals(user.getId()))
                .min(Comparator.comparing(member -> member.getStatus() == PracticeParticipationMemberStatus.ACCEPTED ? 0 : 1))
                .orElseThrow(() -> new ErrorMessageException("Новый лидер не найден", ErrorCodes.BadRequest));

        nextLeader.setRole(PracticeMemberRole.LEADER);
        participationMemberRepository.save(nextLeader);

        myMember.setRole(PracticeMemberRole.MEMBER);
        myMember.setStatus(PracticeParticipationMemberStatus.REMOVED);
        participationMemberRepository.save(myMember);

        participation.setStatus(PracticeParticipationStatus.WAITING_MEMBERS);
        participation.setReadyAt(null);
        participation.setChosenAt(null);
        practiceParticipationRepository.save(participation);
    }

    private PracticeParticipation findEntityById(Long id) {
        return practiceParticipationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Участие в практике не найдено: " + id));
    }

    private ExamPractice resolveExamPractice(Long examPracticeId) {
        return examPracticeRepository.findById(examPracticeId)
                .orElseThrow(() -> new NotFoundException("ExamПрактика не найдена: " + examPracticeId));
    }

    private Exam resolveExam(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException(messageService.get("error.exam.not.found", examId)));
    }

    private void requireLeader(User user, PracticeParticipation participation) {
        boolean isLeader = participationMemberRepository.existsByPracticeParticipationIdAndUserIdAndRoleAndStatus(
                participation.getId(),
                user.getId(),
                PracticeMemberRole.LEADER,
                PracticeParticipationMemberStatus.ACCEPTED
        );
        if (!isLeader) {
            throw new ErrorMessageException(messageService.get("error.leader.only"), ErrorCodes.Forbidden);
        }
    }

    private void ensureTeamParticipation(PracticeParticipation participation) {
        ExamPractice examPractice = participation.getExamPractice();
        if (examPractice == null || examPractice.getPractice().getWorkMode() != WorkMode.TEAM) {
            throw new ErrorMessageException(messageService.get("error.team.operation"), ErrorCodes.BadRequest);
        }
    }

    private void validateAccess(User user, Exam exam) {
        if (!canAccess(user, exam)) {
            throw new ErrorMessageException(messageService.get("error.exam.access.denied"), ErrorCodes.Forbidden);
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

    private MyPracticeParticipationResponse toMyParticipationResponse(PracticeParticipation participation) {
        PracticeSubmission submission = submissionRepository.findByExamParticipationId(participation.getId()).orElse(null);
        var members = participationMemberRepository.findByPracticeParticipationId(participation.getId()).stream()
                .map(m -> new PracticeParticipationMemberResponse(
                        m.getId(),
                        summaryMapper.toUserSummary(m.getUser()),
                        m.getRole(),
                        m.getStatus()
                ))
                .toList();

        return new MyPracticeParticipationResponse(
                participation.getId(),
                participation.getExam().getId(),
                participation.getExamPractice() == null ? null : participation.getExamPractice().getId(),
                participation.getExamPractice() == null ? null : summaryMapper.toPracticeSummaryWithoutSubject(participation.getExamPractice().getPractice()),
                participation.getStatus(),
                members,
                submission == null ? null : new PracticeSubmissionResponse(
                        submission.getId(),
                        participation.getId(),
                        participation.getExam().getId(),
                        participation.getExamPractice() == null ? null : participation.getExamPractice().getId(),
                        submission.getStudent() == null ? null : summaryMapper.toUserSummary(submission.getStudent()),
                        submission.getTextAnswer(),
                        submission.getFileUrl(),
                        submission.getSubmittedAt(),
                        submission.getStatus(),
                        submission.getTeacherComment(),
                        submissionAttemptRepository.countBySubmissionId(submission.getId())
                )
        );
    }

    private MyTeamInvitationResponse toMyTeamInvitationResponse(PracticeParticipationMember invitedMember) {
        PracticeParticipation participation = invitedMember.getPracticeParticipation();
        ExamPractice examPractice = participation.getExamPractice();

        User leader = participationMemberRepository.findByPracticeParticipationId(participation.getId()).stream()
                .filter(member -> member.getRole() == PracticeMemberRole.LEADER)
                .filter(member -> member.getStatus() == PracticeParticipationMemberStatus.ACCEPTED)
                .map(PracticeParticipationMember::getUser)
                .findFirst()
                .orElse(null);

        return new MyTeamInvitationResponse(
                participation.getId(),
                participation.getExam().getId(),
                examPractice == null ? null : examPractice.getId(),
                participation.getExam().getTitle(),
                examPractice == null ? null : summaryMapper.toPracticeSummaryWithoutSubject(examPractice.getPractice()),
                leader == null ? null : summaryMapper.toUserSummary(leader)
        );
    }

    private PracticeParticipationResponse toResponse(PracticeParticipation participation) {
        ExamPractice examPractice = participation.getExamPractice();
        PracticeParticipationTeamResponse team = null;

        if (examPractice != null && examPractice.getPractice().getWorkMode() == WorkMode.TEAM) {
            List<PracticeParticipationTeamMemberResponse> members = participationMemberRepository
                    .findByPracticeParticipationId(participation.getId())
                    .stream()
                    .filter(member -> member.getStatus() != PracticeParticipationMemberStatus.REMOVED)
                    .map(member -> new PracticeParticipationTeamMemberResponse(
                            member.getUser().getId(),
                            (member.getUser().getFirstName() + " " + member.getUser().getLastName()).trim(),
                            member.getRole(),
                            member.getStatus()
                    ))
                    .toList();

            team = new PracticeParticipationTeamResponse(
                    members.size(),
                    examPractice.getPractice().getTeamSize(),
                    members
            );
        }

        return new PracticeParticipationResponse(
                participation.getId(),
                examPractice == null ? null : examPractice.getId(),
                participation.getExam().getId(),
                examPractice == null ? null : new PracticeParticipationPracticeSummary(
                        examPractice.getPractice().getId(),
                        examPractice.getPractice().getName(),
                        examPractice.getPractice().getWorkMode(),
                        examPractice.getPractice().getTeamSize(),
                        examPractice.getPractice().isSchedulingRequired()
                ),
                participation.getCreatedAt(),
                participation.getStatus(),
                team
        );
    }

    @Transactional
    public PracticeParticipationResponse selectPractice(Long examId, Long examPracticeId, User user) {
        if (!isStudent(user)) {
            throw new ErrorMessageException(messageService.get("error.select.student.only"), ErrorCodes.Forbidden);
        }

        Exam exam = resolveExam(examId);
        checkPracticeExam(exam);

        if (exam.getStatus() != ExamStatus.PUBLISHED) {
            throw new ErrorMessageException(messageService.get("error.exam.must.be.published"), ErrorCodes.BadRequest);
        }

        if (!isAssignedToStudent(exam, user.getId())) {
            throw new NotFoundException(messageService.get("error.exam.not.found", examId));
        }

        ExamPractice examPractice = resolveExamPractice(examPracticeId);
        if (!examPractice.getExam().getId().equals(exam.getId())) {
            throw new ErrorMessageException(messageService.get("error.practice.not.for.exam"), ErrorCodes.BadRequest);
        }

        var existingMembership = participationMemberRepository
                .findByPracticeParticipationExamIdAndUserIdAndStatusNot(
                        exam.getId(),
                        user.getId(),
                        PracticeParticipationMemberStatus.REMOVED
                );
        if (existingMembership.isPresent()) {
            PracticeParticipation existingParticipation = existingMembership.get().getPracticeParticipation();
            String existingPracticeName = existingParticipation.getExamPractice() == null
                    ? "nomalum practice"
                    : existingParticipation.getExamPractice().getPractice().getName();
            throw new ErrorMessageException(
                    "Siz allaqachon practice tanlagansiz: " + existingPracticeName + ". Boshqasini tanlash uchun avval participationni bekor qiling yoki teamdan chiqing.",
                    ErrorCodes.AlreadyExists
            );
        }

        if (examPractice.getPractice().getWorkMode() == WorkMode.INDIVIDUAL) {
            PracticeParticipation participation = new PracticeParticipation();
            participation.setExam(exam);
            participation.setExamPractice(examPractice);
            participation.setCreatedAt(LocalDateTime.now());
            participation.setReadyAt(LocalDateTime.now());
            participation.setChosenAt(LocalDateTime.now());
            participation.setStatus(PracticeParticipationStatus.PRACTICE_CHOSEN);
            PracticeParticipation saved = practiceParticipationRepository.save(participation);

            PracticeParticipationMember member = new PracticeParticipationMember();
            member.setPracticeParticipation(saved);
            member.setUser(user);
            member.setRole(PracticeMemberRole.LEADER);
            member.setStatus(PracticeParticipationMemberStatus.ACCEPTED);
            participationMemberRepository.save(member);

            return toResponse(saved);
        }

        if (examPractice.getPractice().getWorkMode() != WorkMode.TEAM) {
            throw new ErrorMessageException(messageService.get("error.unknown.work.mode"), ErrorCodes.BadRequest);
        }

        Integer teamSize = examPractice.getPractice().getTeamSize();
        if (teamSize == null || teamSize <= 1) {
            throw new ErrorMessageException(messageService.get("error.team.size.one"), ErrorCodes.BadRequest);
        }

        PracticeParticipation participation = new PracticeParticipation();
        participation.setExam(exam);
        participation.setExamPractice(examPractice);
        participation.setCreatedAt(LocalDateTime.now());
        participation.setStatus(PracticeParticipationStatus.WAITING_MEMBERS);
        PracticeParticipation saved = practiceParticipationRepository.save(participation);

        PracticeParticipationMember leader = new PracticeParticipationMember();
        leader.setPracticeParticipation(saved);
        leader.setUser(user);
        leader.setRole(PracticeMemberRole.LEADER);
        leader.setStatus(PracticeParticipationMemberStatus.ACCEPTED);
        participationMemberRepository.save(leader);

        return toResponse(saved);
    }

    private boolean isAssignedToStudent(Exam exam, Long studentId) {
        boolean assignedDirectly = exam.getTargetStudents().stream()
                .anyMatch(student -> student.getId().equals(studentId));

        boolean assignedByGroup = exam.getGroups().stream()
                .anyMatch(group -> group.getStudents().stream().anyMatch(student -> student.getId().equals(studentId)));

        return assignedDirectly || assignedByGroup;
    }
}
