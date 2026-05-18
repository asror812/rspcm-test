package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticalTaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticalTaskAssignmentRepository extends JpaRepository<PracticalTaskAssignment, Long> {
    List<PracticalTaskAssignment> findByExamId(Long examId);
    List<PracticalTaskAssignment> findByStudentId(Long studentId);
    long countByExamId(Long examId);
    long countByExamIdAndIdNot(Long examId, Long id);
}
