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

    public List<Exam> findAll() {
        User currentUser = currentUserService.getCurrentUser();
        if (isStudent(currentUser)) {
            return examRepository.findDistinctByGroupsStudentsIdOrTargetStudentsId(currentUser.getId(), currentUser.getId());
        }
        return examRepository.findAll();
    }

    public List<ExamResponse> findAllResponse() {
        return findAll().stream().map(ExamMapper::toResponse).toList();
    }

    public Exam findById(Long id) {
        Exam exam = examRepository.findById(id).orElseThrow(() -> new NotFoundException("Exam topilmadi: " + id));
        User currentUser = currentUserService.getCurrentUser();
        if (!isStudent(currentUser)) {
            return exam;
        }
        boolean assignedByGroup = exam.getGroups().stream()
                .anyMatch(group -> group.getStudents().stream().anyMatch(student -> student.getId().equals(currentUser.getId())));
        boolean assignedDirectly = exam.getTargetStudents().stream()
                .anyMatch(student -> student.getId().equals(currentUser.getId()));
        if (!assignedByGroup && !assignedDirectly) {
            throw new NotFoundException("Exam topilmadi: " + id);
        }
        return exam;
    }

    public ExamResponse findResponseById(Long id) {
        return ExamMapper.toResponse(findById(id));
    }

    public List<Exam> findOwnCreated() {
        return examRepository.findByCreatedById(currentUserService.getCurrentUser().getId());
    }

    public List<ExamResponse> findOwnCreatedResponse() {
        return findOwnCreated().stream().map(ExamMapper::toResponse).toList();
    }

    @Transactional
    public Exam create(ExamRequest request) {
        Exam exam = Exam.builder()
                .title(request.title())
                .description(request.description())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .maxScore(request.maxScore())
                .type(request.type())
                .groups(resolveGroups(request.groupIds()))
                .targetStudents(resolveStudents(request.studentIds()))
                .createdBy(currentUserService.getCurrentUser())
                .subject(request.subjectId() == null ? null : subjectRepository.findById(request.subjectId())
                        .orElseThrow(() -> new NotFoundException("Subject topilmadi: " + request.subjectId())))
                .build();
        return examRepository.save(exam);
    }

    public ExamResponse createResponse(ExamRequest request) {
        return ExamMapper.toResponse(create(request));
    }

    @Transactional
    public Exam update(Long id, ExamRequest request) {
        Exam exam = findById(id);
        exam.setTitle(request.title());
        exam.setDescription(request.description());
        exam.setStartAt(request.startAt());
        exam.setEndAt(request.endAt());
        exam.setMaxScore(request.maxScore());
        exam.setType(request.type());
        exam.setGroups(resolveGroups(request.groupIds()));
        exam.setTargetStudents(resolveStudents(request.studentIds()));
        exam.setPracticalTasks(resolvePracticalTasks(request.practicalTaskIds()));
        exam.setSubject(request.subjectId() == null ? null : subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new NotFoundException("Subject topilmadi: " + request.subjectId())));
        return examRepository.save(exam);
    }

    public ExamResponse updateResponse(Long id, ExamRequest request) {
        return ExamMapper.toResponse(update(id, request));
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
