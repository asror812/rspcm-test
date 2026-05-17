package org.example.rspcm.repository;

import org.example.rspcm.model.entity.TeacherProfile;
import org.example.rspcm.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {
    Optional<TeacherProfile> findByUserId(Long userId);
    void deleteByUserId(Long userId);

    boolean existsByUserIdAndTeachingSubjectsId(Long userId, Long subjectId);

    Long user(User user);
}
