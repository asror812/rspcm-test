package org.example.rspcm.repository;

import org.example.rspcm.model.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Page<Subject> findAllBy(Pageable pageable);
    List<Subject> findDistinctByTeachersId(Long teacherId);
    Optional<Subject> findByName(String name);
}
