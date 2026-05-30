package org.example.rspcm.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rspcm.model.entity.FCM;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.repository.FCMRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FCMRepository fcmRepository;

    @Transactional
    public void registerToken(User user, String token) {
        if (token == null || token.isBlank()) return;
        if (!fcmRepository.existsByUserIdAndFcmToken(user.getId(), token)) {
            FCM fcm = FCM.builder().user(user).fcmToken(token).build();
            fcmRepository.save(fcm);
        }
    }

    @Transactional
    public void unregisterToken(User user, String token) {
        fcmRepository.deleteByUserIdAndFcmToken(user.getId(), token);
    }

    public void sendToUser(User user, String title, String body) {
        if (!isFirebaseAvailable()) return;
        List<String> tokens = fcmRepository.findAllByUserId(user.getId())
                .stream().map(FCM::getFcmToken).toList();
        if (tokens.isEmpty()) return;
        sendMulticast(tokens, title, body);
    }

    public void sendToUsers(List<User> users, String title, String body) {
        if (!isFirebaseAvailable()) return;
        List<String> tokens = users.stream()
                .flatMap(u -> fcmRepository.findAllByUserId(u.getId()).stream())
                .map(FCM::getFcmToken)
                .distinct()
                .toList();
        if (tokens.isEmpty()) return;
        sendMulticast(tokens, title, body);
    }

    private void sendMulticast(List<String> tokens, String title, String body) {
        if (tokens.isEmpty()) return;
        // FCM multicast supports up to 500 tokens per call
        int batchSize = 500;
        for (int i = 0; i < tokens.size(); i += batchSize) {
            List<String> batch = tokens.subList(i, Math.min(i + batchSize, tokens.size()));
            try {
                MulticastMessage message = MulticastMessage.builder()
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .addAllTokens(batch)
                        .build();
                FirebaseMessaging.getInstance().sendEachForMulticast(message);
            } catch (FirebaseMessagingException e) {
                log.error("FCM multicast failed: {}", e.getMessage());
            }
        }
    }

    private boolean isFirebaseAvailable() {
        return !FirebaseApp.getApps().isEmpty();
    }
}
