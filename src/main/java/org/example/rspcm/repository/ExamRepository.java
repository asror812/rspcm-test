package org.example.rspcm.repository;

import org.example.rspcm.model.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findDistinctByGroupsStudentsIdOrTargetStudentsId(Long groupStudentId, Long targetStudentId);
    List<Exam> findByCreatedById(Long createdById);
}
