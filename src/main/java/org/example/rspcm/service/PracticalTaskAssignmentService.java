package org.example.rspcm.service;

import org.example.rspcm.dto.practice.PracticalTaskAssignmentRequest;
import org.example.rspcm.dto.practice.PracticalTaskAssignmentResponse;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.PracticalTaskAssignmentMapper;
import org.example.rspcm.model.entity.PracticalTaskAssignment;
import org.example.rspcm.model.entity.PracticalTaskAssignmentStatus;
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

    public List<PracticalTaskAssignment> findAll() {
        return assignmentRepository.findAll();
    }

    public List<PracticalTaskAssignmentResponse> findAllResponse() {
        return findAll().stream().map(PracticalTaskAssignmentMapper::toResponse).toList();
    }

    public PracticalTaskAssignment findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PracticalTaskAssignment topilmadi: " + id));
    }

    public PracticalTaskAssignmentResponse findResponseById(Long id) {
        return PracticalTaskAssignmentMapper.toResponse(findById(id));
    }

    @Transactional
    public PracticalTaskAssignment create(PracticalTaskAssignmentRequest request) {
        PracticalTaskAssignment assignment = PracticalTaskAssignment.builder()
                .exam(examRepository.findById(request.examId())
                        .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + request.examId())))
                .practicalTask(practicalTaskRepository.findById(request.practicalTaskId())
                        .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + request.practicalTaskId())))
                .student(request.studentId() == null ? null : userRepository.findById(request.studentId())
                        .orElseThrow(() -> new NotFoundException("Student topilmadi: " + request.studentId())))
                .team(request.teamId() == null ? null : practiceTeamRepository.findById(request.teamId())
                        .orElseThrow(() -> new NotFoundException("PracticeTeam topilmadi: " + request.teamId())))
                .status(request.status() == null ? PracticalTaskAssignmentStatus.CHOSEN : request.status())
                .chosenAt(LocalDateTime.now())
                .score(request.score())
                .teacherComment(request.teacherComment())
                .build();
        return assignmentRepository.save(assignment);
    }

    public PracticalTaskAssignmentResponse createResponse(PracticalTaskAssignmentRequest request) {
        return PracticalTaskAssignmentMapper.toResponse(create(request));
    }

    @Transactional
    public PracticalTaskAssignment update(Long id, PracticalTaskAssignmentRequest request) {
        PracticalTaskAssignment assignment = findById(id);
        assignment.setExam(examRepository.findById(request.examId())
                .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + request.examId())));
        assignment.setPracticalTask(practicalTaskRepository.findById(request.practicalTaskId())
                .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + request.practicalTaskId())));
        assignment.setStudent(request.studentId() == null ? null : userRepository.findById(request.studentId())
                .orElseThrow(() -> new NotFoundException("Student topilmadi: " + request.studentId())));
        assignment.setTeam(request.teamId() == null ? null : practiceTeamRepository.findById(request.teamId())
                .orElseThrow(() -> new NotFoundException("PracticeTeam topilmadi: " + request.teamId())));
        assignment.setStatus(request.status() == null ? assignment.getStatus() : request.status());
        assignment.setScore(request.score());
        assignment.setTeacherComment(request.teacherComment());
        if (request.status() == PracticalTaskAssignmentStatus.SUBMITTED && assignment.getSubmittedAt() == null) {
            assignment.setSubmittedAt(LocalDateTime.now());
        }
        return assignmentRepository.save(assignment);
    }

    public PracticalTaskAssignmentResponse updateResponse(Long id, PracticalTaskAssignmentRequest request) {
        return PracticalTaskAssignmentMapper.toResponse(update(id, request));
    }

    @Transactional
    public void delete(Long id) {
        assignmentRepository.delete(findById(id));
    }
}
