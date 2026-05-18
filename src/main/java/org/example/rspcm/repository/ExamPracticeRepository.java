package org.example.rspcm.repository;

import org.example.rspcm.model.entity.ExamPractice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamPracticeRepository extends JpaRepository<ExamPractice, Long> {
}
