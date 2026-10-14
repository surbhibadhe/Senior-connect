package com.project.seniorconnect.DTO;

public record BookmarkStatusResponse(
        Long resourceId,
        boolean bookmarked,
        String message
) {
    public BookmarkStatusResponse(Long resourceId, boolean bookmarked) {
        this(resourceId, bookmarked, bookmarked ? "Resource bookmarked successfully" : "Bookmark removed successfully");
    }
}
