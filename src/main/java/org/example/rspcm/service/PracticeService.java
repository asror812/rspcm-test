package org.example.rspcm.service;

import org.example.rspcm.dto.practice.PracticeRequest;
import org.example.rspcm.dto.practice.PracticeResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.PracticalTask;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.model.enums.SubmissionType;
import org.example.rspcm.model.enums.WorkMode;
import org.example.rspcm.repository.PracticeRepository;
import org.example.rspcm.mapper.PracticeMapper;
import lombok.RequiredArgsConstructor;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final PracticeRepository practiceRepository;
    private final PracticeMapper practiceMapper;
    private final TeacherProfileRepository teacherProfileRepository;

    public Page<PracticeResponse> findAll(
            String query, boolean own, Long subjectId,
            User user, Pageable pageable) {

        Long userId = user.getId();

        if (isAdmin(user)) {
            return practiceRepository.searchAll(query, own, subjectId, userId, pageable)
                    .map(practiceMapper::toResponse);
        }

        validateTeacherSubjectAccess(userId, subjectId);

        return practiceRepository.searchAll(query, own, subjectId, userId, pageable)
                .map(practiceMapper::toResponse);
    }

    public PracticalTask findById(Long id, User user) {
        PracticalTask practicalTask = practiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + id));

        if (isAdmin(user)) {
            return practicalTask;
        }

        validateTeacherSubjectAccess(user.getId(), practicalTask.getSubject().getId());

        return practicalTask;
    }

    public PracticeResponse findResponseById(Long id, User user) {
        PracticalTask practicalTask = practiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + id));

        if (isStudent(user)) {

            Long studentId = user.getId();
            boolean assigned = practicalTask.getExams() != null && practicalTask.getExams().stream().anyMatch(exam ->
                    exam.getTargetStudents().stream().anyMatch(student -> Objects.equals(student.getId(), studentId)) ||
                            exam.getGroups().stream().anyMatch(group ->
                                    group.getStudents().stream().anyMatch(student -> Objects.equals(student.getId(), studentId))));
            if (!assigned) {
                throw new NotFoundException("PracticalTask topilmadi: " + id);
            }
        }

        return practiceMapper.toResponse(practicalTask);
    }


    public PracticeResponse createResponse(PracticeRequest request, User user) {
        validateTeamConfig(request.workMode(), request.teamSize());

        PracticalTask practicalTask = practiceMapper.toEntity(
                request,
                resolveSubmissionTypes(request.allowedSubmissionTypes()),
                user
        );
        return practiceMapper.toResponse(practiceRepository.save(practicalTask));
    }

    @Transactional
    public PracticeResponse update(Long id, PracticeRequest request, User user) {
        validateTeamConfig(request.workMode(), request.teamSize());

        PracticalTask practicalTask = findById(id, user);
        practiceMapper.updateEntity(practicalTask, request, resolveSubmissionTypes(request.allowedSubmissionTypes()));
        return practiceMapper.toResponse(practiceRepository.save(practicalTask));
    }

/*    @Transactional
    public PracticalTask assignGroups(Long practiceId, User user) {
        PracticalTask practicalTask = findById(practiceId, user);
        return practiceRepository.save(practicalTask);
    }*/

    public PracticeResponse assignGroupsResponse(Long practiceId, User user) {
        PracticalTask practicalTask = findById(practiceId, user);
        return practiceMapper.toResponse(practiceRepository.save(practicalTask));
    }

    @Transactional
    public void delete(Long id, User user) {
        PracticalTask practicalTask = findById(id, user);
        practiceRepository.delete(practicalTask);
    }

    private void validateTeamConfig(WorkMode mode, Integer teamSize) {
        if (mode == WorkMode.TEAM && (teamSize == null || teamSize < 2)) {
            throw new ErrorMessageException("TEAM mode uchun teamSize kamida 2 bo'lishi kerak", ErrorCodes.BadRequest);
        }
        if (mode == WorkMode.INDIVIDUAL && teamSize != null) {
            throw new ErrorMessageException("INDIVIDUAL mode uchun teamSize bo'sh bo'lishi kerak", ErrorCodes.BadRequest);
        }
    }

    private Set<SubmissionType> resolveSubmissionTypes(Set<SubmissionType> requestedTypes) {
        if (requestedTypes == null || requestedTypes.isEmpty()) {
            return Set.of(SubmissionType.TEXT);
        }
        return requestedTypes;
    }

    private void validateTeacherSubjectAccess(Long userId, Long subjectId) {
        if (subjectId == null) {
            throw new ErrorMessageException("Fan bo'yicha filtr kiritilishi shart", ErrorCodes.BadRequest);
        }

        boolean teachesSubject = teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(userId, subjectId);
        if (!teachesSubject) {
            throw new ErrorMessageException("Faqat o'zingizga biriktirilgan fan imtihonlarini ko'ra olasiz", ErrorCodes.Forbidden);
        }
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == roleName);
    }

    private boolean isAdmin(User user) {
        return hasRole(user, RoleName.ROLE_ADMIN);
    }

    private boolean isTeacher(User user) {
        return hasRole(user, RoleName.ROLE_TEACHER);
    }

    private boolean isStudent(User user) {
        return hasRole(user, RoleName.ROLE_STUDENT);
    }
}
