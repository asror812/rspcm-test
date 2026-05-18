package org.example.rspcm.repository;

import org.example.rspcm.model.entity.Practice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticeRepository extends JpaRepository<Practice, Long> {

    @Query(value = """
            select p from Practice p
                    where (:own = false or p.createdBy.id = :userId)
                    and (:subjectId is null or p.subject.id = :subjectId)
                    and (:query is null or :query = ''
                    or lower(p.name) like lower(concat('%', :query, '%'))
                    or lower(p.description) like lower(concat('%', :query, '%'))
                    )
            """)
    Page<Practice> searchAll(String query, boolean own, Long subjectId, Long userId, Pageable pageable);
}
