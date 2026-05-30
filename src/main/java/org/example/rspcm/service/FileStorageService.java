package org.example.rspcm.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.rspcm.config.AppProperties;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final AppProperties appProperties;

    public StoredFile storeChatFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ErrorMessageException("Attachment file is empty", ErrorCodes.BadRequest);
        }

        String uploadDir = appProperties.getUploadDir();
        if (uploadDir == null || uploadDir.isBlank()) {
            throw new ErrorMessageException("Upload directory is not configured", ErrorCodes.InvalidParams);
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalName.substring(dot);
        }
        String storedName = UUID.randomUUID() + extension;
        Path base = Path.of(uploadDir);
        Path chatDir = base.resolve("chats");
        Path target = chatDir.resolve(storedName);

        try {
            Files.createDirectories(chatDir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ErrorMessageException("Failed to store file", ErrorCodes.InternalServerError);
        }

        StoredFile stored = new StoredFile();
        stored.setFileName(originalName);
        stored.setStoredName(storedName);
        stored.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        stored.setSize(file.getSize());
        stored.setAbsolutePath(target.toString());
        stored.setRelativePath("chats/" + storedName);
        return stored;
    }

    @Getter
    public static class StoredFile {
        private String fileName;
        private String storedName;
        private String contentType;
        private Long size;
        private String absolutePath;
        private String relativePath;

        public void setFileName(String fileName) { this.fileName = fileName; }
        public void setStoredName(String storedName) { this.storedName = storedName; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public void setSize(Long size) { this.size = size; }
        public void setAbsolutePath(String absolutePath) { this.absolutePath = absolutePath; }
        public void setRelativePath(String relativePath) { this.relativePath = relativePath; }
    }
}
