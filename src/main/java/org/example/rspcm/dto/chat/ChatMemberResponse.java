package org.example.rspcm.dto.chat;

public record ChatMemberResponse(
        Long userId,
        String firstName,
        String lastName,
        String role
) {}
