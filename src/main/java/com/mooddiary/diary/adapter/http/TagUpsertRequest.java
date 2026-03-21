package com.mooddiary.diary.adapter.http;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagUpsertRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 64) String color
) {
}

