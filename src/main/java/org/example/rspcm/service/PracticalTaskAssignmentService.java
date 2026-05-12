package org.example.rspcm.service;

import org.example.rspcm.dto.practice.PracticalTaskAssignmentRequest;
import org.example.rspcm.dto.practice.PracticalTaskAssignmentResponse;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.PracticalTaskAssignmentMapper;
import org.example.rspcm.model.entity.PracticalTaskAssignment;
import org.example.rspcm.model.enums.PracticalTaskAssignmentStatus;
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

    public List<PracticalTaskAssignmentResponse> findAll() {
        return assignmentRepository.findAll().stream().map(PracticalTaskAssignmentMapper::toResponse).toList();
    }

    public PracticalTaskAssignment findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PracticalTaskAssignment topilmadi: " + id));
    }

    public PracticalTaskAssignmentResponse findResponseById(Long id) {
        return PracticalTaskAssignmentMapper.toResponse(assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PracticalTaskAssignment topilmadi: " + id)));
    }

    @Transactional
    public PracticalTaskAssignment create(PracticalTaskAssignmentRequest request) {
        PracticalTaskAssignment assignment = PracticalTaskAssignmentMapper.toEntity(
                request,
                examRepository.findById(request.examId())
                        .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + request.examId())),
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
        PracticalTaskAssignment assignment = PracticalTaskAssignmentMapper.toEntity(
                request,
                examRepository.findById(request.examId())
                        .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + request.examId())),
                practicalTaskRepository.findById(request.practicalTaskId())
                        .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + request.practicalTaskId())),
                request.studentId() == null ? null : userRepository.findById(request.studentId())
                        .orElseThrow(() -> new NotFoundException("Student topilmadi: " + request.studentId())),
                request.teamId() == null ? null : practiceTeamRepository.findById(request.teamId())
                        .orElseThrow(() -> new NotFoundException("PracticeTeam topilmadi: " + request.teamId())),
                LocalDateTime.now()
        );
        return PracticalTaskAssignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public PracticalTaskAssignmentResponse update(Long id, PracticalTaskAssignmentRequest request) {
        PracticalTaskAssignment assignment = findById(id);
        PracticalTaskAssignmentMapper.updateEntity(
                assignment,
                request,
                examRepository.findById(request.examId())
                        .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + request.examId())),
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
        return PracticalTaskAssignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public void delete(Long id) {
        assignmentRepository.delete(findById(id));
    }
}
