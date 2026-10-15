package com.project.seniorconnect.DTO;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long userId,
        String authorName,
        String authorPicture,
        String authorSubtitle,
        String perspectiveTag,
        String content,
        int rating,
        LocalDateTime createdAt,
        boolean canDelete
) {
}
