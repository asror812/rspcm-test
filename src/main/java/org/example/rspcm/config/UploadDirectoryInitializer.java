package org.example.rspcm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadDirectoryInitializer {

    private final AppProperties appProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        String baseDir = appProperties.getUploadDir();
        if (baseDir == null || baseDir.isBlank()) {
            return;
        }

        try {
            Path base = Path.of(baseDir);
            Files.createDirectories(base.resolve("chats"));
            Files.createDirectories(base.resolve("groups"));
            Files.createDirectories(base.resolve("users"));
            Files.createDirectories(base.resolve("images"));
            log.info("Upload directories ensured under {}", base.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create upload directories at {}", baseDir, e);
        }
    }
}
