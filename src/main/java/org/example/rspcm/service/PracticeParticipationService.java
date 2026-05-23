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
import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;
import org.example.rspcm.model.enums.PracticeParticipationStatus;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.model.enums.WorkMode;
import org.example.rspcm.repository.ExamPracticeRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.PracticeParticipationMemberRepository;
import org.example.rspcm.repository.PracticeParticipationRepository;
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
import java.util.Set;

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

    private void checkPracticeExam(Exam exam) {
        if (exam.getType() != ExamType.PRACTICE) {
            throw new ErrorMessageException("Faqat PRACTICE turidagi exam uchun participation yaratiladi", ErrorCodes.BadRequest);
        }
    }

    public Page<PracticeParticipationResponse> findAll(
            Long examId,
            PracticeParticipationStatus status,
            User user,
            Pageable pageable
    ) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new NotFoundException("Exam topilmadi: " + examId));

        validateAccess(user, exam);

        Page<PracticeParticipation> page = status == null
                ? practiceParticipationRepository.findByExamId(examId, pageable)
                : practiceParticipationRepository.findByExamIdAndStatus(examId, status, pageable);

        return page
                .map(this::toResponse);
    }

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
            throw new ErrorMessageException("Faqat lider ishtirokchilarni taklif qila oladi", ErrorCodes.Forbidden);
        }

        ExamPractice examPractice = participation.getExamPractice();
        if (examPractice == null || examPractice.getPractice().getWorkMode() != WorkMode.TEAM) {
            throw new ErrorMessageException("Faqat TEAM practice uchun invite mumkin", ErrorCodes.BadRequest);
        }

        Integer teamSize = examPractice.getPractice().getTeamSize();
        if (teamSize == null || teamSize <= 1) {
            throw new ErrorMessageException("TEAM practice uchun teamSize 1 dan katta bo'lishi shart", ErrorCodes.BadRequest);
        }

        Set<Long> studentIds = new HashSet<>(request.studentIds());
        if (studentIds.contains(user.getId())) {
            throw new ErrorMessageException("Lider o'zini taklif qila olmaydi", ErrorCodes.BadRequest);
        }

        long activeMembers = participationMemberRepository.countByPracticeParticipationIdAndStatusNot(
                participation.getId(),
                PracticeParticipationMemberStatus.REMOVED
        );
        if (activeMembers + studentIds.size() > teamSize) {
            throw new ErrorMessageException("Ishtirokchilar soni teamSize dan oshib ketadi", ErrorCodes.BadRequest);
        }

        for (Long studentId : studentIds) {
            User invitee = userRepository.findById(studentId)
                    .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi: " + studentId));

            if (!isStudent(invitee)) {
                throw new ErrorMessageException("Faqat student taklif qilinadi", ErrorCodes.BadRequest);
            }

            if (!isAssignedToStudent(participation.getExam(), invitee.getId())) {
                throw new ErrorMessageException("Taklif qilinayotgan student ushbu examga kira olmaydi", ErrorCodes.BadRequest);
            }

            boolean usedInAnotherParticipation = participationMemberRepository
                    .existsByPracticeParticipationExamIdAndUserIdAndStatusAndPracticeParticipationIdNot(
                            participation.getExam().getId(),
                            invitee.getId(),
                            PracticeParticipationMemberStatus.ACCEPTED,
                            participation.getId()
                    );
            if (usedInAnotherParticipation) {
                throw new ErrorMessageException("Bu student ushbu examda boshqa practice tanlagan", ErrorCodes.BadRequest);
            }

            boolean alreadyInParticipation = participationMemberRepository
                    .findByPracticeParticipationIdAndUserId(participation.getId(), invitee.getId())
                    .isPresent();
            if (alreadyInParticipation) {
                throw new ErrorMessageException("Bu student allaqachon ushbu participationda mavjud", ErrorCodes.AlreadyExists);
            }
        }

        for (Long studentId : studentIds) {
            User invitee = userRepository.findById(studentId)
                    .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi: " + studentId));

            PracticeParticipationMember member = new PracticeParticipationMember();
            member.setPracticeParticipation(participation);
            member.setUser(invitee);
            member.setRole(PracticeMemberRole.MEMBER);
            member.setStatus(PracticeParticipationMemberStatus.INVITED);
            participationMemberRepository.save(member);
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
                .orElseThrow(() -> new NotFoundException("Participation member topilmadi"));

        if (member.getStatus() != PracticeParticipationMemberStatus.INVITED) {
            throw new ErrorMessageException("Faqat INVITED holatdagi taklifni qabul qilish mumkin", ErrorCodes.BadRequest);
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
                .orElseThrow(() -> new NotFoundException("Participation member topilmadi"));

        member.setStatus(PracticeParticipationMemberStatus.DECLINED);
        participationMemberRepository.save(member);

        participation.setStatus(PracticeParticipationStatus.WAITING_MEMBERS);
        participation.setReadyAt(null);

        return toResponse(practiceParticipationRepository.save(participation));
    }

    public MyPracticeParticipationResponse getMyParticipationByExam(Long examId, User user) {
        if (!isStudent(user)) {
            throw new ErrorMessageException("Ruxsat yo'q", ErrorCodes.Forbidden);
        }

        Exam exam = resolveExam(examId);
        if (!isAssignedToStudent(exam, user.getId())) {
            throw new NotFoundException("Imtihon topilmadi: " + examId);
        }

        PracticeParticipationMember member = participationMemberRepository
                .findByPracticeParticipationExamIdAndUserIdAndStatusNot(
                        examId,
                        user.getId(),
                        PracticeParticipationMemberStatus.REMOVED
                )
                .orElseThrow(() -> new NotFoundException("Participation topilmadi"));

        PracticeParticipation participation = member.getPracticeParticipation();
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
                        submission.getTeacherComment()
                )
        );
    }

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
                .orElseThrow(() -> new NotFoundException("Participation member topilmadi"));

        if (member.getRole() == PracticeMemberRole.LEADER) {
            throw new ErrorMessageException("Liderni o'chirib bo'lmaydi", ErrorCodes.BadRequest);
        }

        member.setStatus(PracticeParticipationMemberStatus.REMOVED);
        participationMemberRepository.save(member);
        participation.setStatus(PracticeParticipationStatus.WAITING_MEMBERS);
        participation.setReadyAt(null);
        practiceParticipationRepository.save(participation);
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

    private void requireLeader(User user, PracticeParticipation participation) {
        boolean isLeader = participationMemberRepository.existsByPracticeParticipationIdAndUserIdAndRoleAndStatus(
                participation.getId(),
                user.getId(),
                PracticeMemberRole.LEADER,
                PracticeParticipationMemberStatus.ACCEPTED
        );
        if (!isLeader) {
            throw new ErrorMessageException("Faqat liderga ruxsat", ErrorCodes.Forbidden);
        }
    }

    private void ensureTeamParticipation(PracticeParticipation participation) {
        ExamPractice examPractice = participation.getExamPractice();
        if (examPractice == null || examPractice.getPractice().getWorkMode() != WorkMode.TEAM) {
            throw new ErrorMessageException("Faqat TEAM participation uchun amal mavjud", ErrorCodes.BadRequest);
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
    public PracticeParticipationResponse chooseIndividualPractice(Long examId, Long examPracticeId, User user) {
        if (!isStudent(user)) {
            throw new ErrorMessageException("Faqat student practice tanlay oladi", ErrorCodes.Forbidden);
        }

        Exam exam = resolveExam(examId);
        checkPracticeExam(exam);

        if (exam.getStatus() != ExamStatus.PUBLISHED) {
            throw new ErrorMessageException("Faqat PUBLISHED holatdagi exam uchun practice tanlanadi", ErrorCodes.BadRequest);
        }

        if (!isAssignedToStudent(exam, user.getId())) {
            throw new NotFoundException("Imtihon topilmadi: " + examId);
        }

        ExamPractice examPractice = resolveExamPractice(examPracticeId);
        if (!examPractice.getExam().getId().equals(exam.getId())) {
            throw new ErrorMessageException("Tanlangan practice ushbu examga tegishli emas", ErrorCodes.BadRequest);
        }

        if (examPractice.getPractice().getWorkMode() != WorkMode.INDIVIDUAL) {
            throw new ErrorMessageException("Faqat INDIVIDUAL work mode amaliyotni tanlash mumkin", ErrorCodes.BadRequest);
        }

        boolean alreadyChosen = participationMemberRepository.existsByPracticeParticipationExamIdAndUserIdAndStatus(
                exam.getId(),
                user.getId(),
                PracticeParticipationMemberStatus.ACCEPTED
        );
        if (alreadyChosen) {
            throw new ErrorMessageException("Siz ushbu examda allaqachon practice tanlagansiz", ErrorCodes.AlreadyExists);
        }

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

    @Transactional
    public PracticeParticipationResponse createTeamParticipation(Long examId, Long examPracticeId, User user) {
        if (!isStudent(user)) {
            throw new ErrorMessageException("Faqat student jamoa participation yarata oladi", ErrorCodes.Forbidden);
        }

        Exam exam = resolveExam(examId);
        checkPracticeExam(exam);

        if (!isAssignedToStudent(exam, user.getId())) {
            throw new NotFoundException("Imtihon topilmadi: " + examId);
        }

        ExamPractice examPractice = resolveExamPractice(examPracticeId);
        if (!examPractice.getExam().getId().equals(exam.getId())) {
            throw new ErrorMessageException("Tanlangan practice ushbu examga tegishli emas", ErrorCodes.BadRequest);
        }

        if (examPractice.getPractice().getWorkMode() != WorkMode.TEAM) {
            throw new ErrorMessageException("Faqat TEAM work mode amaliyot uchun jamoa participation yaratiladi", ErrorCodes.BadRequest);
        }

        Integer teamSize = examPractice.getPractice().getTeamSize();
        if (teamSize == null || teamSize <= 1) {
            throw new ErrorMessageException("TEAM practice uchun teamSize 1 dan katta bo'lishi shart", ErrorCodes.BadRequest);
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
