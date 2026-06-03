package org.example.rspcm.dto.practice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PracticeAttemptCommentRequest(
        @NotBlank
        @Size(max = 2000)
        String comment
) {
}
