package com.project.seniorconnect.DTO;

import com.project.seniorconnect.Entity.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResourceReportRequest(
        @NotNull(message = "Report reason is required")
        ReportReason reason,

        @Size(max = 1000, message = "Explanation must not exceed 1000 characters")
        String explanation
) {
    public ResourceReportRequest(ReportReason reason) {
        this(reason, null);
    }
}
