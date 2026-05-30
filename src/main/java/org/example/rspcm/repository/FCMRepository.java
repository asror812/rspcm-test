package org.example.rspcm.repository;

import org.example.rspcm.model.entity.FCM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FCMRepository extends JpaRepository<FCM, Long> {

    boolean existsByUserIdAndFcmToken(Long userId, String fcmToken);

    void deleteByUserIdAndFcmToken(Long userId, String fcmToken);

    List<FCM> findAllByUserId(Long userId);
}
