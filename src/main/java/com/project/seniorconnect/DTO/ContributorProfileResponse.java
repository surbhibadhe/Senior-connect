package com.project.seniorconnect.DTO;

import java.time.LocalDateTime;

public record ContributorProfileResponse(
        Long id,
        String name,
        String profilePicture,
        String bio,
        String department,
        long totalResources,
        LocalDateTime createdAt
) {
}
