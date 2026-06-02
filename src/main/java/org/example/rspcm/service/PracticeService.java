package org.example.rspcm.service;

import org.example.rspcm.dto.practice.PracticeRequest;
import org.example.rspcm.dto.practice.PracticeResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.Practice;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.model.enums.SubmissionType;
import org.example.rspcm.repository.PracticeRepository;
import org.example.rspcm.mapper.PracticeMapper;
import lombok.RequiredArgsConstructor;
import org.example.rspcm.repository.SubjectRepository;
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
    private final SubjectRepository subjectRepository;
    private final MessageService messageService;

    public PracticeResponse create(PracticeRequest request, User user) {
        Subject subject = resolveSubject(request.subjectId());
        validateCreateOrUpdateAccess(user, subject.getId());

        Practice practice = practiceMapper.toEntity(
                request,
                resolveSubmissionTypes(request.allowedSubmissionTypes()),
                user
        );
        practice.setSubject(subject);
        return practiceMapper.toResponse(practiceRepository.save(practice));
    }

    @Transactional(readOnly = true)
    public Page<PracticeResponse> findAll(
            String query, boolean own, Long subjectId,
            User user, Pageable pageable) {
        Long userId = user.getId();
        if (isAdmin(user)) {
            return practiceRepository.searchAll(query, own, subjectId, userId, pageable)
                    .map(practiceMapper::toResponse);
        }

        // When fetching own practices the teacher implicitly owns them — no subject filter required
        if (!own) {
            validateTeacherSubjectAccess(userId, subjectId);
        }

        return practiceRepository.searchAll(query, own, subjectId, userId, pageable)
                .map(practiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PracticeResponse findResponseById(Long id, User user) {
        Practice practice = findById(id, user);

        if (isAdmin(user)) {
            return practiceMapper.toResponse(practice);
        }

        validateTeacherSubjectAccess(user.getId(), practice.getSubject().getId());
        return practiceMapper.toResponse(practice);
    }

    @Transactional(readOnly = true)
    public Practice findById(Long id, User user) {
        Practice practice = practiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageService.get("error.practice.not.found", id)));

        if (isAdmin(user)) {
            return practice;
        }

        validateTeacherSubjectAccess(user.getId(), practice.getSubject().getId());

        return practice;
    }

    @Transactional
    public PracticeResponse update(Long id, PracticeRequest request, User user) {
        Practice practice = findById(id, user);
        Subject subject = resolveSubject(request.subjectId());
        validateCreateOrUpdateAccess(user, subject.getId());

        practiceMapper.updateEntity(practice, request, resolveSubmissionTypes(request.allowedSubmissionTypes()));
        practice.setSubject(subject);
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
            return;
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

    private Subject resolveSubject(Long subjectId) {
        if (subjectId == null) {
            throw new ErrorMessageException(messageService.get("error.subject.id.required"), ErrorCodes.BadRequest);
        }
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден: " + subjectId));
    }

    private void validateTeacherSubjectAccess(Long userId, Long subjectId) {
        if (subjectId == null) {
            throw new ErrorMessageException(messageService.get("error.subject.filter.required"), ErrorCodes.BadRequest);
        }

        boolean teachesSubject = subjectRepository.existsByIdAndTeachersId(subjectId, userId);
        if (!teachesSubject) {
            throw new ErrorMessageException(messageService.get("error.subject.access.denied"), ErrorCodes.Forbidden);
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

    private void validateCreateOrUpdateAccess(User user, Long subjectId) {
        if (!isAdmin(user)) {
            validateTeacherSubjectAccess(user.getId(), subjectId);
        }
    }
}
