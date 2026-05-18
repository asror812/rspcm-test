package org.example.rspcm.repository;

import jakarta.validation.constraints.NotNull;
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
            where (:examId      is null or e.exam.id = :examId)
                and (:subjectId is null or e.exam.subject.id = :subjectId)
                and (:own  =      false or e.createdBy.id = :userId)
            """)
    Page<ExamQuestion> searchAll(Long examId, Long subjectId, boolean own, Long userId, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(eq.score), 0)
        FROM ExamQuestion eq
        WHERE eq.exam.id = :examId
        """)
    Integer sumScoreByExamId(@Param("examId") Long examId);

    long countByExamId(Long examId);

    long countByExamIdAndIdNot(Long examId, Long id);

    boolean existsByExamIdAndQuestionId(Long id, @NotNull Long aLong);
}
