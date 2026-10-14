package com.project.seniorconnect.Service;

import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ReportStatusUpdateRequest;
import com.project.seniorconnect.DTO.ResourceReportRequest;
import com.project.seniorconnect.DTO.ResourceReportResponse;
import com.project.seniorconnect.Entity.ReportStatus;
import com.project.seniorconnect.Entity.Resource;
import com.project.seniorconnect.Entity.ResourceReport;
import com.project.seniorconnect.Entity.Role;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Repository.ResourceReportRepository;
import com.project.seniorconnect.Repository.ResourceRepository;
import com.project.seniorconnect.Repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ModerationService {

    private final ResourceReportRepository resourceReportRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ResourceService resourceService;

    public ModerationService(
            ResourceReportRepository resourceReportRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            ResourceService resourceService) {
        this.resourceReportRepository = resourceReportRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.resourceService = resourceService;
    }

    @Transactional
    public ResourceReportResponse reportResource(
            Long resourceId,
            ResourceReportRequest request,
            String googleId) {

        User reporter = getUserByGoogleId(googleId);
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resource not found with id: " + resourceId));

        if (request == null || request.reason() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Report reason is required");
        }

        // Prevent duplicate reports while an existing report is active (OPEN)
        if (resourceReportRepository.existsByReporterIdAndResourceIdAndStatus(
                reporter.getId(), resource.getId(), ReportStatus.OPEN)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have an active report for this resource");
        }

        String explanation = (request.explanation() != null && !request.explanation().trim().isEmpty())
                ? request.explanation().trim()
                : null;

        ResourceReport report = new ResourceReport(resource, reporter, request.reason(), explanation);
        ResourceReport saved = resourceReportRepository.save(report);

        return toReportResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ResourceReportResponse> getReports(
            ReportStatus status,
            int page,
            int size,
            String googleId) {

        verifyAdmin(googleId);

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page index must not be less than zero");
        }

        if (size < 1 || size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 50");
        }

        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ResourceReport> reportPage = (status != null)
                ? resourceReportRepository.findByStatus(status, pageable)
                : resourceReportRepository.findAll(pageable);

        Page<ResourceReportResponse> responsePage = reportPage.map(this::toReportResponse);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public ResourceReportResponse getReportById(Long reportId, String googleId) {
        verifyAdmin(googleId);

        ResourceReport report = resourceReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Report not found with id: " + reportId));

        return toReportResponse(report);
    }

    @Transactional
    public ResourceReportResponse updateReportStatus(
            Long reportId,
            ReportStatusUpdateRequest request,
            String googleId) {

        User admin = verifyAdmin(googleId);

        if (request == null || request.status() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Report status is required");
        }

        if (request.status() == ReportStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Report status can only be updated to REVIEWED or DISMISSED");
        }

        ResourceReport report = resourceReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Report not found with id: " + reportId));

        report.setStatus(request.status());
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(admin);

        if (request.moderatorNotes() != null) {
            report.setModeratorNotes(request.moderatorNotes().trim());
        }

        ResourceReport updated = resourceReportRepository.save(report);
        return toReportResponse(updated);
    }

    @Transactional
    public ResourceReportResponse removeResourceByReport(Long reportId, String googleId) {
        User admin = verifyAdmin(googleId);

        ResourceReport report = resourceReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Report not found with id: " + reportId));

        if (report.getResource() != null) {
            Long resourceId = report.getResource().getId();
            // Call safe resource deletion logic (validates paths, removes file, cleans relations)
            resourceService.deleteResource(resourceId, googleId);
        }

        report.setStatus(ReportStatus.REVIEWED);
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(admin);
        report.setModeratorNotes("Resource removed by administrator upon review");

        ResourceReport updated = resourceReportRepository.save(report);
        return toReportResponse(updated);
    }

    private User verifyAdmin(String googleId) {
        User user = getUserByGoogleId(googleId);
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin privileges required");
        }
        return user;
    }

    private User getUserByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User profile not found in database"));
    }

    public ResourceReportResponse toReportResponse(ResourceReport report) {
        Long resourceId = report.getResource() != null ? report.getResource().getId() : null;
        String resourceTitle = (report.getResource() != null)
                ? report.getResource().getTitle()
                : report.getResourceTitleSnapshot();
        String reporterName = (report.getReporter() != null)
                ? report.getReporter().getName()
                : "Unknown Reporter";
        Long reporterId = (report.getReporter() != null)
                ? report.getReporter().getId()
                : null;
        String resolvedByName = (report.getResolvedBy() != null)
                ? report.getResolvedBy().getName()
                : null;

        return new ResourceReportResponse(
                report.getId(),
                resourceId,
                resourceTitle,
                reporterId,
                reporterName,
                report.getReason(),
                report.getExplanation(),
                report.getStatus(),
                report.getModeratorNotes(),
                resolvedByName,
                report.getCreatedAt(),
                report.getResolvedAt()
        );
    }
}
