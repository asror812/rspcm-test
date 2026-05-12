package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticeLogbookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PracticeLogbookEntryRepository extends JpaRepository<PracticeLogbookEntry, Long> {
    Optional<PracticeLogbookEntry> findFirstByLogbookIdAndEntryDate(Long logbookId, LocalDate entryDate);
}
