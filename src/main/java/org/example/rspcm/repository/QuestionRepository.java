package org.example.rspcm.repository;

import org.example.rspcm.model.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySubjectIdAndDeletedFalse(Long subjectId);
    List<Question> findByCreatedByIdAndSubjectIdAndDeletedFalse(Long createdById, Long subjectId);
    long countBySubjectId(Long subjectId);
    boolean existsBySubjectIdAndText(Long subjectId, String text);
    Optional<Question> findBySubjectIdAndText(Long subjectId, String text);
    Optional<Question> findByIdAndDeletedFalse(Long id);

    @Query("""
            SELECT q FROM Question q
            WHERE (:subjectId IS NULL OR q.subject.id = :subjectId)
                AND (:createdById IS NULL OR q.createdBy.id = :createdById)
                AND q.deleted = false
    """)
    Page<Question> searchAll(Long subjectId, Long createdById, Pageable pageable);


    @Query("""
        SELECT q FROM Question q
        WHERE (:subjectId IS NULL OR q.subject.id = :subjectId)
            AND (:createdBy IS NULL OR q.createdBy.id = :createdBy)
            AND q.deleted = false
    """)
    Page<Question> findAllOwnQuestionsBySubjectId(Long subjectId, Long createdBy, Pageable pageable);
}
