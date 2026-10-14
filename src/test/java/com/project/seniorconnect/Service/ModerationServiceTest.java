package com.project.seniorconnect.Service;

import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ReportStatusUpdateRequest;
import com.project.seniorconnect.DTO.ResourceReportRequest;
import com.project.seniorconnect.DTO.ResourceReportResponse;
import com.project.seniorconnect.Entity.*;
import com.project.seniorconnect.Repository.ResourceReportRepository;
import com.project.seniorconnect.Repository.ResourceRepository;
import com.project.seniorconnect.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModerationServiceTest {

    @Mock
    private ResourceReportRepository resourceReportRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private ModerationService moderationService;

    private User studentReporter;
    private User adminUser;
    private Resource resource;
    private ResourceReport sampleReport;

    @BeforeEach
    void setUp() {
        studentReporter = new User();
        ReflectionTestUtils.setField(studentReporter, "id", 1L);
        studentReporter.setName("Reporter Student");
        studentReporter.setEmail("reporter@despu.edu.in");
        studentReporter.setGoogleId("google-reporter");
        studentReporter.setRole(Role.JUNIOR);

        adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", 99L);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@despu.edu.in");
        adminUser.setGoogleId("google-admin");
        adminUser.setRole(Role.ADMIN);

        resource = new Resource();
        ReflectionTestUtils.setField(resource, "id", 20L);
        resource.setTitle("Problematic Resource");
        resource.setUploadedBy(studentReporter);

        sampleReport = new ResourceReport(resource, studentReporter, ReportReason.SPAM, "Looks like spam link");
        ReflectionTestUtils.setField(sampleReport, "id", 100L);
    }

    @Test
    void reportResource_success() {
        when(userRepository.findByGoogleId("google-reporter")).thenReturn(Optional.of(studentReporter));
        when(resourceRepository.findById(20L)).thenReturn(Optional.of(resource));
        when(resourceReportRepository.existsByReporterIdAndResourceIdAndStatus(1L, 20L, ReportStatus.OPEN)).thenReturn(false);
        when(resourceReportRepository.save(any(ResourceReport.class))).thenAnswer(inv -> inv.getArgument(0));

        ResourceReportRequest request = new ResourceReportRequest(ReportReason.INAPPROPRIATE, "Offensive content");

        ResourceReportResponse response = moderationService.reportResource(20L, request, "google-reporter");

        assertNotNull(response);
        assertEquals(20L, response.resourceId());
        assertEquals("Problematic Resource", response.resourceTitle());
        assertEquals(1L, response.reporterId());
        assertEquals(ReportReason.INAPPROPRIATE, response.reason());
        assertEquals("Offensive content", response.explanation());
        assertEquals(ReportStatus.OPEN, response.status());
    }

    @Test
    void reportResource_missingResource_throws404() {
        when(userRepository.findByGoogleId("google-reporter")).thenReturn(Optional.of(studentReporter));
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceReportRequest request = new ResourceReportRequest(ReportReason.SPAM);

        assertThrows(ResponseStatusException.class, () ->
                moderationService.reportResource(999L, request, "google-reporter"));
    }

    @Test
    void reportResource_duplicateActiveReport_throws409Conflict() {
        when(userRepository.findByGoogleId("google-reporter")).thenReturn(Optional.of(studentReporter));
        when(resourceRepository.findById(20L)).thenReturn(Optional.of(resource));
        when(resourceReportRepository.existsByReporterIdAndResourceIdAndStatus(1L, 20L, ReportStatus.OPEN)).thenReturn(true);

        ResourceReportRequest request = new ResourceReportRequest(ReportReason.SPAM);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                moderationService.reportResource(20L, request, "google-reporter"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("You already have an active report for this resource"));
    }

    @Test
    void reportResource_nullReason_throws400() {
        when(userRepository.findByGoogleId("google-reporter")).thenReturn(Optional.of(studentReporter));
        when(resourceRepository.findById(20L)).thenReturn(Optional.of(resource));

        ResourceReportRequest request = new ResourceReportRequest(null);

        assertThrows(ResponseStatusException.class, () ->
                moderationService.reportResource(20L, request, "google-reporter"));
    }

    @Test
    void getReports_asAdmin_successWithPagination() {
        when(userRepository.findByGoogleId("google-admin")).thenReturn(Optional.of(adminUser));
        when(resourceReportRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleReport)));

        PageResponse<ResourceReportResponse> pageResponse =
                moderationService.getReports(null, 0, 10, "google-admin");

        assertNotNull(pageResponse);
        assertEquals(1, pageResponse.totalElements());
        assertEquals(ReportReason.SPAM, pageResponse.content().get(0).reason());
    }

    @Test
    void getReports_asAdmin_withStatusFilter() {
        when(userRepository.findByGoogleId("google-admin")).thenReturn(Optional.of(adminUser));
        when(resourceReportRepository.findByStatus(eq(ReportStatus.OPEN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleReport)));

        PageResponse<ResourceReportResponse> pageResponse =
                moderationService.getReports(ReportStatus.OPEN, 0, 10, "google-admin");

        assertEquals(1, pageResponse.totalElements());
    }

    @Test
    void getReports_asNonAdmin_throws403Forbidden() {
        when(userRepository.findByGoogleId("google-reporter")).thenReturn(Optional.of(studentReporter));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                moderationService.getReports(null, 0, 10, "google-reporter"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getReportById_asAdmin_success() {
        when(userRepository.findByGoogleId("google-admin")).thenReturn(Optional.of(adminUser));
        when(resourceReportRepository.findById(100L)).thenReturn(Optional.of(sampleReport));

        ResourceReportResponse response = moderationService.getReportById(100L, "google-admin");

        assertNotNull(response);
        assertEquals(100L, response.id());
    }

    @Test
    void getReportById_asNonAdmin_throws403Forbidden() {
        when(userRepository.findByGoogleId("google-reporter")).thenReturn(Optional.of(studentReporter));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                moderationService.getReportById(100L, "google-reporter"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getReportById_notFound_throws404() {
        when(userRepository.findByGoogleId("google-admin")).thenReturn(Optional.of(adminUser));
        when(resourceReportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                moderationService.getReportById(999L, "google-admin"));
    }

    @Test
    void updateReportStatus_asAdmin_toReviewed() {
        when(userRepository.findByGoogleId("google-admin")).thenReturn(Optional.of(adminUser));
        when(resourceReportRepository.findById(100L)).thenReturn(Optional.of(sampleReport));
        when(resourceReportRepository.save(any(ResourceReport.class))).thenAnswer(inv -> inv.getArgument(0));

        ReportStatusUpdateRequest request = new ReportStatusUpdateRequest(ReportStatus.REVIEWED, "Investigated, content revised");

        ResourceReportResponse response = moderationService.updateReportStatus(100L, request, "google-admin");

        assertNotNull(response);
        assertEquals(ReportStatus.REVIEWED, response.status());
        assertEquals("Investigated, content revised", response.moderatorNotes());
        assertEquals("Admin User", response.resolvedByName());
        assertNotNull(response.resolvedAt());
    }

    @Test
    void updateReportStatus_asAdmin_toDismissed() {
        when(userRepository.findByGoogleId("google-admin")).thenReturn(Optional.of(adminUser));
        when(resourceReportRepository.findById(100L)).thenReturn(Optional.of(sampleReport));
        when(resourceReportRepository.save(any(ResourceReport.class))).thenAnswer(inv -> inv.getArgument(0));

        ReportStatusUpdateRequest request = new ReportStatusUpdateRequest(ReportStatus.DISMISSED, "False report");

        ResourceReportResponse response = moderationService.updateReportStatus(100L, request, "google-admin");

        assertEquals(ReportStatus.DISMISSED, response.status());
    }

    @Test
    void updateReportStatus_asAdmin_toOpen_throws400() {
        when(userRepository.findByGoogleId("google-admin")).thenReturn(Optional.of(adminUser));

        ReportStatusUpdateRequest request = new ReportStatusUpdateRequest(ReportStatus.OPEN, "Reopen");

        assertThrows(ResponseStatusException.class, () ->
                moderationService.updateReportStatus(100L, request, "google-admin"));
    }

    @Test
    void removeResourceByReport_asAdmin_safelyDeletesResourceAndUpdatesReport() {
        when(userRepository.findByGoogleId("google-admin")).thenReturn(Optional.of(adminUser));
        when(resourceReportRepository.findById(100L)).thenReturn(Optional.of(sampleReport));
        when(resourceReportRepository.save(any(ResourceReport.class))).thenAnswer(inv -> inv.getArgument(0));

        ResourceReportResponse response = moderationService.removeResourceByReport(100L, "google-admin");

        assertNotNull(response);
        assertEquals(ReportStatus.REVIEWED, response.status());
        verify(resourceService).deleteResource(20L, "google-admin");
        verify(resourceReportRepository).save(sampleReport);
    }

    @Test
    void removeResourceByReport_asNonAdmin_throws403Forbidden() {
        when(userRepository.findByGoogleId("google-reporter")).thenReturn(Optional.of(studentReporter));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                moderationService.removeResourceByReport(100L, "google-reporter"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(resourceService, never()).deleteResource(any(), any());
    }
}
