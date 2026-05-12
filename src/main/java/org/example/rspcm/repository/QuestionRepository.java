package org.example.rspcm.repository;

import org.example.rspcm.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySubjectId(Long subjectId);
    List<Question> findByCreatedByIdAndSubjectId(Long createdById, Long subjectId);
    long countBySubjectId(Long subjectId);
    boolean existsBySubjectIdAndText(Long subjectId, String text);
    Optional<Question> findBySubjectIdAndText(Long subjectId, String text);
}
