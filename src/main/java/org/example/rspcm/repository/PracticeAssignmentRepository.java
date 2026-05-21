package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeAssignmentRepository extends JpaRepository<PracticeSubmission, Long> {
    List<PracticeSubmission> findByExamId(Long examId);
    List<PracticeSubmission> findByStudentId(Long studentId);
    long countByExamId(Long examId);
    long countByExamIdAndIdNot(Long examId, Long id);
}
