package org.example.rspcm.dto.profile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record StudentProfileUpdateRequest(
        @Min(1) @Max(8) Integer course,
        String phoneNumber
) {
}
