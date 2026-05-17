package org.example.rspcm.dto.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record TeacherSelfProfileUpdateRequest(
        @Email String email,
        @Size(min = 6, message = "Yangi parol kamida 6 ta belgidan iborat bo'lishi kerak")
        String newPassword,
        String currentPassword,
        String phoneNumber
) {
}
