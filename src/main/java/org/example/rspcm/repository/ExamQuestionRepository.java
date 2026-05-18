package org.example.rspcm.repository;

import org.example.rspcm.model.entity.ExamQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    List<ExamQuestion> findByExamId(Long examId);

    @Query("""
            select e from ExamQuestion e
            where (:subjectId is null or e.question.subject.id = :subjectId)
                and (:createdById is null or e.createdBy.id = :createdById)
            """)
    Page<ExamQuestion> searchAll(Long subjectId, Long createdById, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(eq.score), 0)
        FROM ExamQuestion eq
        WHERE eq.exam.id = :examId
        """)
    Integer sumScoreByExamId(@Param("examId") Long examId);

    long countByExamId(Long examId);

    long countByExamIdAndIdNot(Long examId, Long id);
}
