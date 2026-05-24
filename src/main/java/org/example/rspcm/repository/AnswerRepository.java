package org.example.rspcm.repository;

import org.example.rspcm.model.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findByExamQuestionId(Long examQuestionId);
    List<StudentAnswer> findByStudentId(Long studentId);
}
