package com.project.seniorconnect.DTO;

import com.project.seniorconnect.Entity.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportStatusUpdateRequest(
        @NotNull(message = "Status is required")
        ReportStatus status,

        @Size(max = 1000, message = "Moderator notes must not exceed 1000 characters")
        String moderatorNotes
) {
    public ReportStatusUpdateRequest(ReportStatus status) {
        this(status, null);
    }
}
