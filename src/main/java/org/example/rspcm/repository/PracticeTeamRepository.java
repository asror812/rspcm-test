package org.example.rspcm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeTeamRepository extends JpaRepository<PracticeTeam, Long> {
    List<PracticeTeam> findByPracticeId(Long practiceId);
}
