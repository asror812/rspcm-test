package org.example.rspcm.repository;

import org.example.rspcm.model.entity.ExamPractice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamPracticeRepository extends JpaRepository<ExamPractice, Long> {
    Page<ExamPractice> findByExamId(Long examId, Pageable pageable);
    boolean existsByExamIdAndPracticeId(Long examId, Long practiceId);
    boolean existsByExamIdAndPracticeIdAndIdNot(Long examId, Long practiceId, Long id);

    Long countByExamId(Long id);
}
