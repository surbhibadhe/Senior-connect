package com.project.seniorconnect.DTO;

public record HelpfulStatusResponse(
        Long resourceId,
        long helpfulCount,
        boolean markedHelpful,
        String message
) {
    public HelpfulStatusResponse(Long resourceId, long helpfulCount, boolean markedHelpful) {
        this(resourceId, helpfulCount, markedHelpful, markedHelpful ? "Marked as helpful" : "Helpful mark removed");
    }
}
