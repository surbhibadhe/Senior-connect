package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.*;
import com.project.seniorconnect.Entity.ReportReason;
import com.project.seniorconnect.Entity.ReportStatus;
import com.project.seniorconnect.Exception.GlobalExceptionHandler;
import com.project.seniorconnect.Service.EngagementService;
import com.project.seniorconnect.Service.ModerationService;
import com.project.seniorconnect.Service.ResourceService;
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
class EngagementControllerTest {

    @Mock
    private ResourceService resourceService;

    @Mock
    private EngagementService engagementService;

    @Mock
    private ModerationService moderationService;

    @InjectMocks
    private ResourceController resourceController;

    private BookmarkController bookmarkController;

    private MockMvc mockMvcUnauth;
    private MockMvc mockMvcAuth;

    private MockMvc mockMvcBookmarksUnauth;
    private MockMvc mockMvcBookmarksAuth;

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
                Map.of("sub", "google-student-test", "email", "student@despu.edu.in", "email_verified", true)
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

        mockMvcUnauth = MockMvcBuilders.standaloneSetup(resourceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(unauthResolver)
                .build();

        mockMvcAuth = MockMvcBuilders.standaloneSetup(resourceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authResolver)
                .build();

        bookmarkController = new BookmarkController(engagementService);

        mockMvcBookmarksUnauth = MockMvcBuilders.standaloneSetup(bookmarkController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(unauthResolver)
                .build();

        mockMvcBookmarksAuth = MockMvcBuilders.standaloneSetup(bookmarkController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authResolver)
                .build();
    }

    @Test
    void bookmarkResource_unauthenticated_returns401() throws Exception {
        mockMvcUnauth.perform(post("/api/resources/10/bookmark"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bookmarkResource_authenticated_returns200() throws Exception {
        when(engagementService.bookmarkResource(10L, "google-student-test"))
                .thenReturn(new BookmarkStatusResponse(10L, true, "Resource bookmarked successfully"));

        mockMvcAuth.perform(post("/api/resources/10/bookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId").value(10))
                .andExpect(jsonPath("$.bookmarked").value(true));
    }

    @Test
    void removeBookmark_authenticated_returns200() throws Exception {
        when(engagementService.removeBookmark(10L, "google-student-test"))
                .thenReturn(new BookmarkStatusResponse(10L, false, "Bookmark removed successfully"));

        mockMvcAuth.perform(delete("/api/resources/10/bookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked").value(false));
    }

    @Test
    void getBookmarkStatus_authenticated_returns200() throws Exception {
        when(engagementService.getBookmarkStatus(10L, "google-student-test"))
                .thenReturn(new BookmarkStatusResponse(10L, true, "Resource is bookmarked"));

        mockMvcAuth.perform(get("/api/resources/10/bookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked").value(true));
    }

    @Test
    void getBookmarkedResources_authenticated_returns200() throws Exception {
        ResourceResponse item = new ResourceResponse(
                10L, "Data Structures", "Notes", null, null, null, null, null, "Uploader", null, null, 3L
        );
        PageResponse<ResourceResponse> mockPage = new PageResponse<>(
                List.of(item), 0, 10, 1L, 1, true, true, false
        );

        when(engagementService.getBookmarkedResources("google-student-test", 0, 10, "newest"))
                .thenReturn(mockPage);

        mockMvcBookmarksAuth.perform(get("/api/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Data Structures"));
    }

    @Test
    void getBookmarkedResources_unauthenticated_returns401() throws Exception {
        mockMvcBookmarksUnauth.perform(get("/api/bookmarks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void markHelpful_authenticated_returns200() throws Exception {
        when(engagementService.markHelpful(10L, "google-student-test"))
                .thenReturn(new HelpfulStatusResponse(10L, 5L, true, "Marked as helpful"));

        mockMvcAuth.perform(post("/api/resources/10/helpful"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.helpfulCount").value(5))
                .andExpect(jsonPath("$.markedHelpful").value(true));
    }

    @Test
    void removeHelpful_authenticated_returns200() throws Exception {
        when(engagementService.removeHelpful(10L, "google-student-test"))
                .thenReturn(new HelpfulStatusResponse(10L, 4L, false, "Helpful mark removed"));

        mockMvcAuth.perform(delete("/api/resources/10/helpful"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.helpfulCount").value(4))
                .andExpect(jsonPath("$.markedHelpful").value(false));
    }

    @Test
    void getHelpfulStatus_authenticated_returns200() throws Exception {
        when(engagementService.getHelpfulStatus(10L, "google-student-test"))
                .thenReturn(new HelpfulStatusResponse(10L, 10L, true));

        mockMvcAuth.perform(get("/api/resources/10/helpful"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.helpfulCount").value(10))
                .andExpect(jsonPath("$.markedHelpful").value(true));
    }

    @Test
    void reportResource_authenticated_returns201() throws Exception {
        ResourceReportResponse mockReport = new ResourceReportResponse(
                1L, 10L, "Resource Title", 2L, "Reporter", ReportReason.SPAM, "Spamming links",
                ReportStatus.OPEN, null, null, LocalDateTime.now(), null
        );

        when(moderationService.reportResource(eq(10L), any(ResourceReportRequest.class), eq("google-student-test")))
                .thenReturn(mockReport);

        mockMvcAuth.perform(post("/api/resources/10/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"SPAM\",\"explanation\":\"Spamming links\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reason").value("SPAM"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void reportResource_duplicateReport_returns409Conflict() throws Exception {
        when(moderationService.reportResource(eq(10L), any(ResourceReportRequest.class), eq("google-student-test")))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "You already have an active report for this resource"));

        mockMvcAuth.perform(post("/api/resources/10/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"SPAM\",\"explanation\":\"Duplicate report\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You already have an active report for this resource"));
    }

    @Test
    void reportResource_unauthenticated_returns401() throws Exception {
        mockMvcUnauth.perform(post("/api/resources/10/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"SPAM\"}"))
                .andExpect(status().isUnauthorized());
    }
}
