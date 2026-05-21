package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeParticipation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticeParticipationRepository extends JpaRepository<PracticeParticipation, Long> {
    Page<PracticeParticipation> findByExamPracticeExamId(Long examId, Pageable pageable);
}
