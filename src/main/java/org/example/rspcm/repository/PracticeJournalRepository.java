package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeLogbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeJournalRepository extends JpaRepository<PracticeLogbook, Long> {
    List<PracticeLogbook> findByStudentId(Long studentId);
    List<PracticeLogbook> findByPracticalTaskId(Long practiceId);
    Optional<PracticeLogbook> findFirstByPracticalTaskIdAndStudentId(Long practiceId, Long studentId);
}
