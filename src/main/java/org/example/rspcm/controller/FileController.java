package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.repository.ChatAttachmentRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final ChatAttachmentRepository chatAttachmentRepository;

    @GetMapping("/chats/{storedName}")
    public ResponseEntity<Resource> downloadChatAttachment(@PathVariable String storedName) {
        var attachment = chatAttachmentRepository.findByStoredName(storedName)
                .orElseThrow(() -> new NotFoundException("Attachment not found: " + storedName));

        File file = new File(attachment.getFilePath());
        if (!file.exists()) {
            throw new NotFoundException("Attachment file not found: " + storedName);
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }
}
