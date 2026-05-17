package org.example.rspcm.repository;

import org.example.rspcm.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllBy(Pageable pageable);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndEnabledTrueAndDeletedFalse(String email);
    Optional<User> findByUniversityIdAndEnabledTrueAndDeletedFalse(String universityId);

    boolean existsByEmail(String email);
}
