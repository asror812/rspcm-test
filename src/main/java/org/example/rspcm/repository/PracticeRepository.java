package org.example.rspcm.repository;

import org.example.rspcm.model.entity.PracticalTask;
import org.example.rspcm.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PracticeRepository extends JpaRepository<PracticalTask, Long> {

    @Query("select p from PracticalTask p join p.exams e where e.id = :examId")
    List<PracticalTask> findPracticesByExamId(Long examId);

    @Query(value = """
            select p from PracticalTask p
                    where (:own = false or p.createdBy.id = :userId)
                    and (:subjectId is null or p.exams = :subjectId)
                    and (lower(p.name) like lower(concat('%', :query, '%')) or lower(p.description) like lower(concat('%', :query, '%')))
            """)
    Page<PracticalTask> searchAll(String query, boolean own, Long subjectId, Long userId, Pageable pageable);
}
