package org.example.rspcm.controller;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.service.FcmService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> registerToken(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body
    ) {
        String token = body.get("token");
        fcmService.registerToken(user, token);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unregisterToken(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body
    ) {
        String token = body.get("token");
        fcmService.unregisterToken(user, token);
        return ResponseEntity.noContent().build();
    }
}
