package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ReportStatusUpdateRequest;
import com.project.seniorconnect.DTO.ResourceReportResponse;
import com.project.seniorconnect.Entity.ReportReason;
import com.project.seniorconnect.Entity.ReportStatus;
import com.project.seniorconnect.Exception.GlobalExceptionHandler;
import com.project.seniorconnect.Service.ModerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminModerationControllerTest {

    @Mock
    private ModerationService moderationService;

    @InjectMocks
    private AdminModerationController adminModerationController;

    private MockMvc mockMvcUnauth;
    private MockMvc mockMvcAuth;

    private OidcUser testOidcUser;

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver unauthResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return null;
            }
        };

        OidcIdToken idToken = new OidcIdToken(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("sub", "google-admin-1", "email", "admin@despu.edu.in", "email_verified", true)
        );
        testOidcUser = new DefaultOidcUser(Collections.emptyList(), idToken);

        HandlerMethodArgumentResolver authResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return testOidcUser;
            }
        };

        mockMvcUnauth = MockMvcBuilders.standaloneSetup(adminModerationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(unauthResolver)
                .build();

        mockMvcAuth = MockMvcBuilders.standaloneSetup(adminModerationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authResolver)
                .build();
    }

    @Test
    void getReports_unauthenticated_returns401() throws Exception {
        mockMvcUnauth.perform(get("/api/admin/reports"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getReports_asAdmin_returns200() throws Exception {
        ResourceReportResponse item = new ResourceReportResponse(
                1L, 10L, "Sample Title", 2L, "Reporter", ReportReason.SPAM, "Spam link",
                ReportStatus.OPEN, null, null, LocalDateTime.now(), null
        );
        PageResponse<ResourceReportResponse> pageResponse = new PageResponse<>(
                List.of(item), 0, 10, 1L, 1, true, true, false
        );

        when(moderationService.getReports(null, 0, 10, "google-admin-1")).thenReturn(pageResponse);

        mockMvcAuth.perform(get("/api/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].reason").value("SPAM"));
    }

    @Test
    void getReports_asStudent_returns403Forbidden() throws Exception {
        when(moderationService.getReports(null, 0, 10, "google-admin-1"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required"));

        mockMvcAuth.perform(get("/api/admin/reports"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin privileges required"));
    }

    @Test
    void getReportById_asAdmin_returns200() throws Exception {
        ResourceReportResponse report = new ResourceReportResponse(
                5L, 10L, "Title", 2L, "Reporter", ReportReason.INAPPROPRIATE, "Explanation",
                ReportStatus.OPEN, null, null, LocalDateTime.now(), null
        );
        when(moderationService.getReportById(5L, "google-admin-1")).thenReturn(report);

        mockMvcAuth.perform(get("/api/admin/reports/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.reason").value("INAPPROPRIATE"));
    }

    @Test
    void getReportById_asStudent_returns403() throws Exception {
        when(moderationService.getReportById(5L, "google-admin-1"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required"));

        mockMvcAuth.perform(get("/api/admin/reports/5"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReportById_notFound_returns404() throws Exception {
        when(moderationService.getReportById(999L, "google-admin-1"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found with id: 999"));

        mockMvcAuth.perform(get("/api/admin/reports/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReportStatus_asAdmin_returns200() throws Exception {
        ResourceReportResponse updated = new ResourceReportResponse(
                5L, 10L, "Title", 2L, "Reporter", ReportReason.SPAM, "Exp",
                ReportStatus.REVIEWED, "Checked and resolved", "Admin", LocalDateTime.now(), LocalDateTime.now()
        );
        when(moderationService.updateReportStatus(eq(5L), any(ReportStatusUpdateRequest.class), eq("google-admin-1")))
                .thenReturn(updated);

        mockMvcAuth.perform(put("/api/admin/reports/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REVIEWED\",\"moderatorNotes\":\"Checked and resolved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEWED"))
                .andExpect(jsonPath("$.moderatorNotes").value("Checked and resolved"));
    }

    @Test
    void updateReportStatus_asStudent_returns403() throws Exception {
        when(moderationService.updateReportStatus(eq(5L), any(ReportStatusUpdateRequest.class), eq("google-admin-1")))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required"));

        mockMvcAuth.perform(put("/api/admin/reports/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REVIEWED\",\"moderatorNotes\":\"Notes\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeResourceByReport_asAdmin_returns200() throws Exception {
        ResourceReportResponse updated = new ResourceReportResponse(
                5L, null, "Removed Title", 2L, "Reporter", ReportReason.COPYRIGHT, "Exp",
                ReportStatus.REVIEWED, "Resource removed by administrator upon review", "Admin", LocalDateTime.now(), LocalDateTime.now()
        );
        when(moderationService.removeResourceByReport(5L, "google-admin-1")).thenReturn(updated);

        mockMvcAuth.perform(delete("/api/admin/reports/5/resource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEWED"))
                .andExpect(jsonPath("$.resourceTitle").value("Removed Title"));
    }

    @Test
    void removeResourceByReport_asStudent_returns403() throws Exception {
        when(moderationService.removeResourceByReport(5L, "google-admin-1"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required"));

        mockMvcAuth.perform(delete("/api/admin/reports/5/resource"))
                .andExpect(status().isForbidden());
    }
}
