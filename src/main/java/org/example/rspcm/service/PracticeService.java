package org.example.rspcm.service;

import org.example.rspcm.dto.practice.PracticeRequest;
import org.example.rspcm.dto.practice.PracticeResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.Practice;
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

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final PracticeRepository practiceRepository;
    private final PracticeMapper practiceMapper;
    private final TeacherProfileRepository teacherProfileRepository;

    public PracticeResponse create(PracticeRequest request, User user) {
        Practice practice = practiceMapper.toEntity(
                request,
                resolveSubmissionTypes(request.allowedSubmissionTypes()),
                user
        );
        return practiceMapper.toResponse(practiceRepository.save(practice));
    }

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

    public PracticeResponse findResponseById(Long id, User user) {
        Practice practice = findById(id, user);

        if (isAdmin(user)) {
            return practiceMapper.toResponse(practice);
        }

        validateTeacherSubjectAccess(user.getId(), practice.getSubject().getId());
        return practiceMapper.toResponse(practice);
    }

    public Practice findById(Long id, User user) {
        Practice practice = practiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Practice topilmadi: " + id));

        if (isAdmin(user)) {
            return practice;
        }

        validateTeacherSubjectAccess(user.getId(), practice.getSubject().getId());

        return practice;
    }

    @Transactional
    public PracticeResponse update(Long id, PracticeRequest request, User user) {
        Practice practice = findById(id, user);
        practiceMapper.updateEntity(practice, request, resolveSubmissionTypes(request.allowedSubmissionTypes()));
        return practiceMapper.toResponse(practiceRepository.save(practice));
    }

    public PracticeResponse assignGroupsResponse(Long practiceId, User user) {
        Practice practice = findById(practiceId, user);
        return practiceMapper.toResponse(practiceRepository.save(practice));
    }

    @Transactional
    public void delete(Long id, User user) {
        Practice practice = findById(id, user);

        if (isAdmin(user)) {
            practice.setDeleted(true);
            practiceRepository.save(practice);
        }

        validateTeacherSubjectAccess(user.getId(), practice.getSubject().getId());
        practice.setDeleted(true);
        practiceRepository.save(practice);
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
