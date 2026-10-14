package com.project.seniorconnect.DTO;

import com.project.seniorconnect.Entity.ReportReason;
import com.project.seniorconnect.Entity.ReportStatus;
import java.time.LocalDateTime;

public record ResourceReportResponse(
        Long id,
        Long resourceId,
        String resourceTitle,
        Long reporterId,
        String reporterName,
        ReportReason reason,
        String explanation,
        ReportStatus status,
        String moderatorNotes,
        String resolvedByName,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}
