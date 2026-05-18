package org.example.rspcm.repository;

import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.enums.ExamStatus;
import org.example.rspcm.model.enums.ExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCreatedById(Long createdById);

    @Query(value = """
            select distinct e from Exam e
            left join e.targetStudents ts
            left join e.groups g
            left join g.students gs
            where (ts.id = :userId or gs.id = :userId)
            and (:query is null or :query = ''
                or lower(e.title) like lower(concat('%', :query, '%'))
                or lower(e.description) like lower(concat('%', :query, '%'))
            )
            and (:examType is null or e.type = :examType)
            and (:subjectId is null or e.subject.id = :subjectId)
            """,
            countQuery = """
                    select count(distinct e.id) from Exam e
                    left join e.targetStudents ts
                    left join e.groups g
                    left join g.students gs
                    where (ts.id = :userId or gs.id = :userId)
                    and (:query is null or :query = ''
                        or lower(e.title) like lower(concat('%', :query, '%'))
                        or lower(e.description) like lower(concat('%', :query, '%'))
                    )
                    and (:examType is null or e.type = :examType)
                    and (:subjectId is null or e.subject.id = :subjectId)
                    """)
    Page<Exam> findStudentExams(Long userId, ExamType examType, Long subjectId, String query, Pageable pageable);

    @Query(value = """
                    select e from Exam e
                    where (:own = false or e.createdBy.id = :userId)
                    and (:query is null or :query = ''
                        or lower(e.title) like lower(concat('%', :query, '%'))
                        or lower(e.description) like lower(concat('%', :query, '%'))
                    )
                    and (:examType is null or e.type = :examType)
                    and (:examStatus is null or e.status = :examStatus)
                    and (:subjectId is null or e.subject.id = :subjectId)
            """,
            countQuery = """
                    select count(e.id) from Exam e
                    where (:own = false or e.createdBy.id = :userId)
                    and (:query is null or :query = ''
                        or lower(e.title) like lower(concat('%', :query, '%'))
                        or lower(e.description) like lower(concat('%', :query, '%'))
                    )
                    and (:examType is null or e.type = :examType)
                    and (:examStatus is null or e.status = :examStatus)
                    and (:subjectId is null or e.subject.id = :subjectId)
                    """)
    Page<Exam> searchAll(Long userId, ExamType examType, ExamStatus examStatus, boolean own, Long subjectId, String query, Pageable pageable);
}
