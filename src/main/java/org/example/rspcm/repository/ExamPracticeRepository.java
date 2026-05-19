package org.example.rspcm.repository;

import org.example.rspcm.model.entity.ExamPractice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamPracticeRepository extends JpaRepository<ExamPractice, Long> {
    List<ExamPractice> findByExamId(Long examId);
    Page<ExamPractice> findByExamId(Long examId, Pageable pageable);
    boolean existsByExamIdAndPracticeId(Long examId, Long practiceId);
    boolean existsByExamIdAndOrderIndex(Long examId, Integer orderIndex);
    boolean existsByExamIdAndOrderIndexAndIdNot(Long examId, Integer orderIndex, Long id);

    Long countByExamId(Long id);
}
