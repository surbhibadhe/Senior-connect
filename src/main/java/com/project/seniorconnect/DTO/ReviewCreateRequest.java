package com.project.seniorconnect.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
        @NotBlank(message = "Review content cannot be blank")
        @Size(max = 2000, message = "Review must not exceed 2000 characters")
        String content,

        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        Integer rating,

        @Size(max = 100)
        String perspectiveTag,

        @Size(max = 200)
        String authorSubtitle
) {
}
