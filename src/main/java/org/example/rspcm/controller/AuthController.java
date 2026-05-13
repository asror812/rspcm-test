package org.example.rspcm.controller;

import org.example.rspcm.dto.auth.AuthResponse;
import org.example.rspcm.dto.auth.LoginRequest;
import org.example.rspcm.dto.auth.RegisterRequest;
import org.example.rspcm.dto.auth.ResendOtpRequest;
import org.example.rspcm.dto.auth.VerifyOtpRequest;
import org.example.rspcm.dto.user.UserResponse;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(Map.of("message", authService.verifyOtp(request)));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return ResponseEntity.ok(Map.of("message", authService.resendOtp(request.email())));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
