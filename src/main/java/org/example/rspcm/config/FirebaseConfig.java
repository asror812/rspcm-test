package org.example.rspcm.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    private final AppProperties appProperties;

    @PostConstruct
    public void initialize() {
        String credentialsPath = appProperties.getFirebase().getCredentialsPath();
        if (credentialsPath == null || credentialsPath.isBlank()) {
            log.warn("Firebase credentials path not configured (app.firebase.credentials-path). Push notifications will be disabled.");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }
}
