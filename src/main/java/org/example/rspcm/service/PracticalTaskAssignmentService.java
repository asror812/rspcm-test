package org.example.rspcm.service;

import org.example.rspcm.dto.practice.PracticalTaskAssignmentRequest;
import org.example.rspcm.dto.practice.PracticalTaskAssignmentResponse;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.PracticalTaskAssignmentMapper;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.PracticalTaskAssignment;
import org.example.rspcm.model.enums.PracticalTaskAssignmentStatus;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.PracticalTaskAssignmentRepository;
import org.example.rspcm.repository.PracticeRepository;
import org.example.rspcm.repository.PracticeTeamRepository;
import org.example.rspcm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PracticalTaskAssignmentService {

    private final PracticalTaskAssignmentRepository assignmentRepository;
    private final ExamRepository examRepository;
    private final PracticeRepository practicalTaskRepository;
    private final UserRepository userRepository;
    private final PracticeTeamRepository practiceTeamRepository;
    private final PracticalTaskAssignmentMapper practicalTaskAssignmentMapper;

    public List<PracticalTaskAssignmentResponse> findAll() {
        return assignmentRepository.findAll().stream().map(practicalTaskAssignmentMapper::toResponse).toList();
    }

    public PracticalTaskAssignment findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PracticalTaskAssignment topilmadi: " + id));
    }

    public PracticalTaskAssignmentResponse findResponseById(Long id) {
        return practicalTaskAssignmentMapper.toResponse(assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PracticalTaskAssignment topilmadi: " + id)));
    }

    @Transactional
    public PracticalTaskAssignment create(PracticalTaskAssignmentRequest request) {
        var exam = validateExamTypeForPracticalTask(request.examId());
        validatePracticeCapacityForCreate(exam.getId(), exam.getItemLimit());
        PracticalTaskAssignment assignment = practicalTaskAssignmentMapper.toEntity(
                request,
                exam,
                practicalTaskRepository.findById(request.practicalTaskId())
                        .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + request.practicalTaskId())),
                request.studentId() == null ? null : userRepository.findById(request.studentId())
                        .orElseThrow(() -> new NotFoundException("Student topilmadi: " + request.studentId())),
                request.teamId() == null ? null : practiceTeamRepository.findById(request.teamId())
                        .orElseThrow(() -> new NotFoundException("PracticeTeam topilmadi: " + request.teamId())),
                LocalDateTime.now()
        );
        return assignmentRepository.save(assignment);
    }

    public PracticalTaskAssignmentResponse createResponse(PracticalTaskAssignmentRequest request) {
        var exam = validateExamTypeForPracticalTask(request.examId());
        validatePracticeCapacityForCreate(exam.getId(), exam.getItemLimit());
        PracticalTaskAssignment assignment = practicalTaskAssignmentMapper.toEntity(
                request,
                exam,
                practicalTaskRepository.findById(request.practicalTaskId())
                        .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + request.practicalTaskId())),
                request.studentId() == null ? null : userRepository.findById(request.studentId())
                        .orElseThrow(() -> new NotFoundException("Student topilmadi: " + request.studentId())),
                request.teamId() == null ? null : practiceTeamRepository.findById(request.teamId())
                        .orElseThrow(() -> new NotFoundException("PracticeTeam topilmadi: " + request.teamId())),
                LocalDateTime.now()
        );
        return practicalTaskAssignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public PracticalTaskAssignmentResponse update(Long id, PracticalTaskAssignmentRequest request) {
        var exam = validateExamTypeForPracticalTask(request.examId());
        validatePracticeCapacityForUpdate(exam.getId(), exam.getItemLimit(), id);
        PracticalTaskAssignment assignment = findById(id);
        practicalTaskAssignmentMapper.updateEntity(
                assignment,
                request,
                exam,
                practicalTaskRepository.findById(request.practicalTaskId())
                        .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + request.practicalTaskId())),
                request.studentId() == null ? null : userRepository.findById(request.studentId())
                        .orElseThrow(() -> new NotFoundException("Student topilmadi: " + request.studentId())),
                request.teamId() == null ? null : practiceTeamRepository.findById(request.teamId())
                        .orElseThrow(() -> new NotFoundException("PracticeTeam topilmadi: " + request.teamId()))
        );
        if (request.status() == PracticalTaskAssignmentStatus.SUBMITTED && assignment.getSubmittedAt() == null) {
            assignment.setSubmittedAt(LocalDateTime.now());
        }
        return practicalTaskAssignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public void delete(Long id) {
        assignmentRepository.delete(findById(id));
    }

    private Exam validateExamTypeForPracticalTask(Long examId) {
        var exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + examId));
        if (exam.getType() != ExamType.PRACTICAL_TASK) {
            throw new ErrorMessageException("Faqat PRACTICAL_TASK turidagi imtihonga amaliy topshiriq biriktirish mumkin", ErrorCodes.BadRequest);
        }
        return exam;
    }

    private void validatePracticeCapacityForCreate(Long examId, Integer limit) {
        long currentCount = assignmentRepository.countByExamId(examId);
        if (limit != null && currentCount >= limit) {
            throw new ErrorMessageException("Bu imtihonda amaliy topshiriqlar soni yetarli", ErrorCodes.BadRequest);
        }
    }

    private void validatePracticeCapacityForUpdate(Long examId, Integer limit, Long assignmentId) {
        long currentCount = assignmentRepository.countByExamIdAndIdNot(examId, assignmentId);
        if (limit != null && currentCount >= limit) {
            throw new ErrorMessageException("Bu imtihonda amaliy topshiriqlar soni yetarli", ErrorCodes.BadRequest);
        }
    }
}
