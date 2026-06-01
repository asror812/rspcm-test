package org.example.rspcm.repository;

import org.example.rspcm.model.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    Page<StudyGroup> findAllBy(Pageable pageable);
    Optional<StudyGroup> findByName(String name);

    @Query("SELECT g FROM StudyGroup g JOIN g.teachers t WHERE t.id = :teacherId")
    List<StudyGroup> findByTeachersId(Long teacherId);

    @Query("SELECT g FROM StudyGroup g JOIN g.students s WHERE s.id = :studentId")
    List<StudyGroup> findByStudentsId(Long studentId);

    @Query("SELECT g FROM StudyGroup g JOIN g.teachers t WHERE g.id = :groupId AND t.id = :teacherId")
    Optional<StudyGroup> findByIdAndTeacherId(Long groupId, Long teacherId);

    @Query("SELECT g FROM StudyGroup g JOIN g.students s WHERE g.id = :groupId AND s.id = :studentId")
    Optional<StudyGroup> findByIdAndStudentId(Long groupId, Long studentId);
}
