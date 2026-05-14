package org.example.rspcm.repository;

import org.example.rspcm.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndEnabledTrueAndDeletedFalse(String email);
    Optional<User> findByUniversityIdAndEnabledTrueAndDeletedFalse(String universityId);

    boolean existsByEmail(String email);
}
