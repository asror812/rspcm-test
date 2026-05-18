package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeAssignmentRepository extends JpaRepository<PracticeAssignment, Long> {
    List<PracticeAssignment> findByExamId(Long examId);
    List<PracticeAssignment> findByStudentId(Long studentId);
    long countByExamId(Long examId);
    long countByExamIdAndIdNot(Long examId, Long id);
}
