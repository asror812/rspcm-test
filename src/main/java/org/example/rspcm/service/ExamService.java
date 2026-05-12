package org.example.rspcm.service;

import org.example.rspcm.dto.exam.ExamRequest;
import org.example.rspcm.dto.exam.ExamResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.PracticalTask;
import org.example.rspcm.model.entity.StudyGroup;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.UserRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.PracticeRepository;
import org.example.rspcm.repository.StudyGroupRepository;
import org.example.rspcm.repository.SubjectRepository;
import org.example.rspcm.mapper.ExamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final StudyGroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final PracticeRepository practiceRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public List<ExamResponse> findAll() {
        User currentUser = currentUserService.getCurrentUser();
        if (isStudent(currentUser)) {
            return examRepository.findDistinctByGroupsStudentsIdOrTargetStudentsId(currentUser.getId(), currentUser.getId())
                    .stream().map(ExamMapper::toResponse).toList();
        }

        return examRepository.findAll().stream().map(ExamMapper::toResponse).toList();
    }

    public Exam findById(Long id) {
        Exam exam = examRepository.findById(id).orElseThrow(() -> new NotFoundException("Exam topilmadi: " + id));
        User currentUser = currentUserService.getCurrentUser();
        if (!isStudent(currentUser)) {
            return exam;
        }

        boolean assignedByGroup = exam.getGroups().stream()
                .anyMatch(group -> group.getStudents()
                        .stream().anyMatch(student -> student.getId().equals(currentUser.getId())));

        boolean assignedDirectly = exam.getTargetStudents().stream()
                .anyMatch(student -> student.getId().equals(currentUser.getId()));

        if (!assignedByGroup && !assignedDirectly) {
            throw new NotFoundException("Exam topilmadi: " + id);
        }

        return exam;
    }

    public ExamResponse findResponseById(Long id) {
        Exam exam = examRepository.findById(id).orElseThrow(() -> new NotFoundException("Exam topilmadi: " + id));
        User currentUser = currentUserService.getCurrentUser();
        if (isStudent(currentUser)) {
            boolean assignedByGroup = exam.getGroups().stream()
                    .anyMatch(group -> group.getStudents().stream().anyMatch(student -> student.getId().equals(currentUser.getId())));
            boolean assignedDirectly = exam.getTargetStudents().stream()
                    .anyMatch(student -> student.getId().equals(currentUser.getId()));
            if (!assignedByGroup && !assignedDirectly) {
                throw new NotFoundException("Exam topilmadi: " + id);
            }
        }
        return ExamMapper.toResponse(exam);
    }

    public List<ExamResponse> findOwnCreated() {
        return examRepository.findByCreatedById(currentUserService.getCurrentUser().getId())
                .stream().map(ExamMapper::toResponse).toList();
    }

    @Transactional
    public ExamResponse create(ExamRequest request) {
        Exam exam = ExamMapper.toEntity(
                request,
                resolveGroups(request.groupIds()),
                resolveStudents(request.studentIds()),
                resolvePracticalTasks(request.practicalTaskIds()),
                currentUserService.getCurrentUser(),
                request.subjectId() == null ? null : subjectRepository.findById(request.subjectId())
                                                     .orElseThrow(() -> new NotFoundException("Subject topilmadi: " + request.subjectId()))
        );

        Exam save = examRepository.save(exam);

        return ExamMapper.toResponse(save);
    }

    @Transactional
    public ExamResponse update(Long id, ExamRequest request) {
        Exam exam = findById(id);
        ExamMapper.updateEntity(
                exam,
                request,
                resolveGroups(request.groupIds()),
                resolveStudents(request.studentIds()),
                resolvePracticalTasks(request.practicalTaskIds()),
                request.subjectId() == null ? null : subjectRepository.findById(request.subjectId())
                                                     .orElseThrow(() -> new NotFoundException("Subject topilmadi: " + request.subjectId()))
        );
        return ExamMapper.toResponse(examRepository.save(exam));
    }

    @Transactional
    public void delete(Long id) {
        Exam exam = findById(id);
        examRepository.delete(exam);
    }

    private Set<StudyGroup> resolveGroups(Set<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(groupRepository.findAllById(groupIds));
    }

    private Set<User> resolveStudents(Set<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(userRepository.findAllById(studentIds));
    }

    private Set<PracticalTask> resolvePracticalTasks(Set<Long> practicalTaskIds) {
        if (practicalTaskIds == null || practicalTaskIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(practiceRepository.findAllById(practicalTaskIds));
    }

    private boolean isStudent(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_STUDENT);
    }
}
