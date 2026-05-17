package org.example.rspcm.repository;

import org.example.rspcm.model.entity.ExamQuestion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    List<ExamQuestion> findByExamId(Long examId);

    @Query("""
            select e from ExamQuestion e
            where (:subjectId is null or e.question.subject.id = :subjectId)
                and (:createdById is null or e.createdBy.id = :createdById)
                and e.createdBy.id = :userId
            """)
    List<ExamQuestion> searchAll(Long subjectId, boolean own, Long createdById, Pageable pageable);

    long countByExamId(Long examId);

    long countByExamIdAndIdNot(Long examId, Long id);
}
