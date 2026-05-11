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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final PracticeRepository practiceRepository;
    private final CurrentUserService currentUserService;

    public List<PracticalTask> findAll() {
        User currentUser = currentUserService.getCurrentUser();
        if (!isStudent(currentUser)) {
            return practiceRepository.findAll();
        }
        Long studentId = currentUser.getId();
        return practiceRepository.findAll().stream()
                .filter(practicalTask -> practicalTask.getExams() != null)
                .filter(practicalTask -> practicalTask.getExams().stream().anyMatch(exam ->
                        exam.getTargetStudents().stream().anyMatch(student -> Objects.equals(student.getId(), studentId)) ||
                                exam.getGroups().stream().anyMatch(group ->
                                        group.getStudents().stream().anyMatch(student -> Objects.equals(student.getId(), studentId)))
                ))
                .toList();
    }

    public List<PracticeResponse> findAllResponse() {
        return findAll().stream().map(PracticeMapper::toResponse).toList();
    }

    public PracticalTask findById(Long id) {
        PracticalTask practicalTask = practiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + id));
        User currentUser = currentUserService.getCurrentUser();
        if (!isStudent(currentUser)) {
            return practicalTask;
        }
        Long studentId = currentUser.getId();
        boolean assigned = practicalTask.getExams() != null && practicalTask.getExams().stream().anyMatch(exam ->
                exam.getTargetStudents().stream().anyMatch(student -> Objects.equals(student.getId(), studentId)) ||
                        exam.getGroups().stream().anyMatch(group ->
                                group.getStudents().stream().anyMatch(student -> Objects.equals(student.getId(), studentId))));
        if (!assigned) {
            throw new NotFoundException("PracticalTask topilmadi: " + id);
        }
        return practicalTask;
    }

    public PracticeResponse findResponseById(Long id) {
        return PracticeMapper.toResponse(findById(id));
    }

    @Transactional
    public PracticalTask create(PracticeRequest request) {
        validateTeamConfig(request.workMode(), request.teamSize());
        PracticalTask practicalTask = PracticalTask.builder()
                .name(request.name())
                .description(request.description())
                .resourceUrl(request.resourceUrl())
                .requirements(request.requirements())
                .deadline(request.deadline())
                .workMode(request.workMode())
                .teamSize(request.teamSize())
                .schedulingRequired(request.schedulingRequired())
                .allowedSubmissionTypes(resolveSubmissionTypes(request.allowedSubmissionTypes()))
                .createdBy(currentUserService.getCurrentUser())
                .build();
        return practiceRepository.save(practicalTask);
    }

    public PracticeResponse createResponse(PracticeRequest request) {
        return PracticeMapper.toResponse(create(request));
    }

    @Transactional
    public PracticalTask update(Long id, PracticeRequest request) {
        validateTeamConfig(request.workMode(), request.teamSize());
        PracticalTask practicalTask = findById(id);
        practicalTask.setName(request.name());
        practicalTask.setDescription(request.description());
        practicalTask.setResourceUrl(request.resourceUrl());
        practicalTask.setRequirements(request.requirements());
        practicalTask.setDeadline(request.deadline());
        practicalTask.setWorkMode(request.workMode());
        practicalTask.setTeamSize(request.teamSize());
        practicalTask.setSchedulingRequired(request.schedulingRequired());
        practicalTask.setAllowedSubmissionTypes(resolveSubmissionTypes(request.allowedSubmissionTypes()));
        return practiceRepository.save(practicalTask);
    }

    public PracticeResponse updateResponse(Long id, PracticeRequest request) {
        return PracticeMapper.toResponse(update(id, request));
    }

    @Transactional
    public PracticalTask assignGroups(Long practiceId) {
        PracticalTask practicalTask = findById(practiceId);
        return practiceRepository.save(practicalTask);
    }

    public PracticeResponse assignGroupsResponse(Long practiceId) {
        return PracticeMapper.toResponse(assignGroups(practiceId));
    }

    @Transactional
    public void delete(Long id) {
        PracticalTask practicalTask = findById(id);
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

    private boolean isStudent(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_STUDENT);
    }

    private Set<SubmissionType> resolveSubmissionTypes(Set<SubmissionType> requestedTypes) {
        if (requestedTypes == null || requestedTypes.isEmpty()) {
            return Set.of(SubmissionType.TEXT);
        }
        return requestedTypes;
    }
}
