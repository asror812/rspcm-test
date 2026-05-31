package org.example.rspcm.dto.profile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record StudentProfileUpdateRequest(
        @Min(value = 1, message = "Значение должно быть не меньше {value}") @Max(value = 8, message = "Значение должно быть не больше {value}") Integer course,
        Long groupId
) {
}
