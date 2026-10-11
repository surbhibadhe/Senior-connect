package com.project.seniorconnect.DTO;


import com.project.seniorconnect.Entity.ResourceCategory;
import com.project.seniorconnect.Entity.ResourceType;

import java.time.LocalDateTime;

public record ResourceResponse(
        Long id,
        String title,
        String description,
        ResourceCategory category,
        ResourceType type,
        String textContent,
        String resourceUrl,
        String originalFileName,
        String uploadedByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}