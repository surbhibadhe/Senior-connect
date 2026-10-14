package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ReportStatusUpdateRequest;
import com.project.seniorconnect.DTO.ResourceReportResponse;
import com.project.seniorconnect.Entity.ReportStatus;
import com.project.seniorconnect.Service.ModerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminModerationController {

    private final ModerationService moderationService;

    public AdminModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    // List reports with pagination and optional status filter
    @GetMapping
    public PageResponse<ResourceReportResponse> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return moderationService.getReports(status, page, size, googleUser.getSubject());
    }

    // Get report details
    @GetMapping("/{id}")
    public ResourceReportResponse getReportById(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return moderationService.getReportById(id, googleUser.getSubject());
    }

    // Update report status (REVIEWED, DISMISSED) with optional notes
    @PutMapping("/{id}/status")
    public ResourceReportResponse updateReportStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReportStatusUpdateRequest request,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return moderationService.updateReportStatus(id, request, googleUser.getSubject());
    }

    // Remove resource following administrator review of report
    @DeleteMapping("/{id}/resource")
    public ResourceReportResponse removeResourceByReport(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return moderationService.removeResourceByReport(id, googleUser.getSubject());
    }
}
