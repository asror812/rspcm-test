package org.example.rspcm.service;

import org.example.rspcm.dto.practice.PracticeAssignmentRequest;
import org.example.rspcm.dto.practice.PracticeAssignmentResponse;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.PracticeAssignmentMapper;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamPractice;
import org.example.rspcm.model.entity.PracticeSubmission;
import org.example.rspcm.model.enums.PracticeAssignmentStatus;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.ExamPracticeRepository;
import org.example.rspcm.repository.PracticeAssignmentRepository;
import org.example.rspcm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PracticeAssignmentService {

    private final PracticeAssignmentRepository assignmentRepository;
    private final ExamRepository examRepository;
    private final ExamPracticeRepository examPracticeRepository;
    private final UserRepository userRepository;
    private final PracticeAssignmentMapper practiceAssignmentMapper;

    public List<PracticeAssignmentResponse> findAll() {
        return assignmentRepository.findAll().stream().map(practiceAssignmentMapper::toResponse).toList();
    }

    public PracticeSubmission findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PracticeSubmission topilmadi: " + id));
    }

    public PracticeAssignmentResponse findResponseById(Long id) {
        return practiceAssignmentMapper.toResponse(assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PracticeSubmission topilmadi: " + id)));
    }

    @Transactional
    public PracticeSubmission create(PracticeAssignmentRequest request) {
        var exam = validateExamTypeForPractice(request.examId());
        validatePracticeCapacityForCreate(exam.getId(), exam.getTaskLimit());
        PracticeSubmission assignment = practiceAssignmentMapper.toEntity(
                request,
                exam,
                resolveExamPractice(request.examPracticeId(), exam),
                request.studentId() == null ? null : userRepository.findById(request.studentId())
                        .orElseThrow(() -> new NotFoundException("Student topilmadi: " + request.studentId())),
                LocalDateTime.now()
        );
        return assignmentRepository.save(assignment);
    }

    public PracticeAssignmentResponse createResponse(PracticeAssignmentRequest request) {
        var exam = validateExamTypeForPractice(request.examId());
        validatePracticeCapacityForCreate(exam.getId(), exam.getTaskLimit());
        PracticeSubmission assignment = practiceAssignmentMapper.toEntity(
                request,
                exam,
                resolveExamPractice(request.examPracticeId(), exam),
                request.studentId() == null ? null : userRepository.findById(request.studentId())
                        .orElseThrow(() -> new NotFoundException("Student topilmadi: " + request.studentId())),
                LocalDateTime.now()
        );
        return practiceAssignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public PracticeAssignmentResponse update(Long id, PracticeAssignmentRequest request) {
        var exam = validateExamTypeForPractice(request.examId());
        validatePracticeCapacityForUpdate(exam.getId(), exam.getTaskLimit(), id);
        PracticeSubmission assignment = findById(id);
        practiceAssignmentMapper.updateEntity(
                assignment,
                request,
                exam,
                resolveExamPractice(request.examPracticeId(), exam),
                request.studentId() == null ? null : userRepository.findById(request.studentId())
                        .orElseThrow(() -> new NotFoundException("Student topilmadi: " + request.studentId()))
        );
        if (request.status() == PracticeAssignmentStatus.SUBMITTED && assignment.getSubmittedAt() == null) {
            assignment.setSubmittedAt(LocalDateTime.now());
        }
        return practiceAssignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public void delete(Long id) {
        assignmentRepository.delete(findById(id));
    }

    private Exam validateExamTypeForPractice(Long examId) {
        var exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + examId));
        if (exam.getType() != ExamType.PRACTICE) {
            throw new ErrorMessageException("Faqat PRACTICE turidagi imtihonga amaliy topshiriq biriktirish mumkin", ErrorCodes.BadRequest);
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

    private ExamPractice resolveExamPractice(Long examPracticeId, Exam exam) {
        var examPractice = examPracticeRepository.findById(examPracticeId)
                .orElseThrow(() -> new NotFoundException("ExamPractice topilmadi: " + examPracticeId));

        if (!examPractice.getExam().getId().equals(exam.getId())) {
            throw new ErrorMessageException("Tanlangan examPractice ushbu examga tegishli emas", ErrorCodes.BadRequest);
        }
        return examPractice;
    }
}
