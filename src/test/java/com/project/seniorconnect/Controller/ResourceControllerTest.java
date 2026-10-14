package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ResourceCreateRequest;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.Entity.Resource;
import com.project.seniorconnect.Entity.ResourceCategory;
import com.project.seniorconnect.Entity.ResourceType;
import com.project.seniorconnect.Exception.GlobalExceptionHandler;
import com.project.seniorconnect.Service.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private ResourceController resourceController;

    private MockMvc mockMvc;
    private MockMvc mockMvcWithUser;

    private OidcUser testOidcUser;

    @BeforeEach
    void setUp() {
        // Unauthenticated MockMvc resolver that resolves @AuthenticationPrincipal to null
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

        mockMvc = MockMvcBuilders.standaloneSetup(resourceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(unauthResolver)
                .build();

        // OidcUser instance
        OidcIdToken idToken = new OidcIdToken(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("sub", "google-student", "email", "student@despu.edu.in", "email_verified", true)
        );
        testOidcUser = new DefaultOidcUser(Collections.emptyList(), idToken);

        // Authenticated MockMvc resolver that injects testOidcUser for @AuthenticationPrincipal
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

        mockMvcWithUser = MockMvcBuilders.standaloneSetup(resourceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authResolver)
                .build();
    }

    // --- 1. Unauthenticated Checks (Expect 401 Unauthorized) ---

    @Test
    void createResource_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "Unauthenticated Resource",
                            "category": "NOTES",
                            "type": "TEXT",
                            "textContent": "Sample notes content"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    @Test
    void uploadResource_unauthenticated_returnsUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/resources/upload")
                        .file(file)
                        .param("title", "Exam Paper")
                        .param("category", "QUESTION_PAPERS"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    @Test
    void updateResource_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/resources/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "Update title",
                            "category": "NOTES",
                            "type": "TEXT",
                            "textContent": "Update content"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteResource_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/resources/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void downloadResource_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/resources/1/download"))
                .andExpect(status().isUnauthorized());
    }

    // --- 2. Input Validation (Expect 400 Bad Request) ---

    @Test
    void createResource_blankTitle_returnsBadRequestWithValidationErrors() throws Exception {
        mockMvcWithUser.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "   ",
                            "category": "NOTES",
                            "type": "TEXT",
                            "textContent": "Some content"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void createResource_nullCategory_returnsBadRequest() throws Exception {
        mockMvcWithUser.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "Valid Title",
                            "category": null,
                            "type": "TEXT",
                            "textContent": "Some content"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.category").exists());
    }

    // --- 3. Authenticated Success Operations ---

    @Test
    void createResource_authenticated_returnsCreated() throws Exception {
        ResourceResponse mockResponse = new ResourceResponse(
                10L,
                "Operating Systems Notes",
                "OS Unit 1",
                ResourceCategory.NOTES,
                ResourceType.TEXT,
                "OS concepts",
                null,
                null,
                "Student",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(resourceService.createResource(any(ResourceCreateRequest.class), eq("google-student")))
                .thenReturn(mockResponse);

        mockMvcWithUser.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "Operating Systems Notes",
                            "description": "OS Unit 1",
                            "category": "NOTES",
                            "type": "TEXT",
                            "textContent": "OS concepts"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Operating Systems Notes"));
    }

    @Test
    void uploadResource_authenticated_returnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "lab_manual.pdf", "application/pdf", "pdf bytes".getBytes()
        );

        ResourceResponse mockResponse = new ResourceResponse(
                11L,
                "WT Lab Manual",
                "Web Technology Lab Manual",
                ResourceCategory.LAB_MANUALS,
                ResourceType.PDF,
                null,
                null,
                "lab_manual.pdf",
                "Student",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(resourceService.uploadResource(
                eq("WT Lab Manual"),
                eq("Web Technology Lab Manual"),
                eq(ResourceCategory.LAB_MANUALS),
                any(),
                eq("google-student")
        )).thenReturn(mockResponse);

        mockMvcWithUser.perform(multipart("/api/resources/upload")
                        .file(file)
                        .param("title", "WT Lab Manual")
                        .param("description", "Web Technology Lab Manual")
                        .param("category", "LAB_MANUALS"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.originalFileName").value("lab_manual.pdf"));
    }

    @Test
    void updateResource_forbidden_returnsForbidden() throws Exception {
        when(resourceService.updateResource(eq(20L), any(ResourceCreateRequest.class), eq("google-student")))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You can update only resources you uploaded"));

        mockMvcWithUser.perform(put("/api/resources/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "Attempt to update",
                            "category": "NOTES",
                            "type": "TEXT",
                            "textContent": "Attempt"
                        }
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You can update only resources you uploaded"));
    }

    @Test
    void deleteResource_authenticated_returnsNoContent() throws Exception {
        mockMvcWithUser.perform(delete("/api/resources/15"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteResource_forbidden_returnsForbidden() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You can delete only resources you uploaded"))
                .when(resourceService).deleteResource(15L, "google-student");

        mockMvcWithUser.perform(delete("/api/resources/15"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You can delete only resources you uploaded"));
    }

    @Test
    void getResourceById_notFound_returnsNotFound() throws Exception {
        when(resourceService.getResourceById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        mockMvc.perform(get("/api/resources/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void downloadResource_authenticated_returnsHeadersAndContent() throws Exception {
        Resource entity = new Resource();
        entity.setType(ResourceType.PDF);
        entity.setOriginalFileName("QuestionPaper.pdf");

        ByteArrayResource fileResource = new ByteArrayResource("sample pdf stream".getBytes());

        when(resourceService.getResourceEntityForDownload(5L)).thenReturn(entity);
        when(resourceService.getDownloadFile(5L)).thenReturn(fileResource);

        mockMvcWithUser.perform(get("/api/resources/5/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes("sample pdf stream".getBytes()));
    }

    // --- 4. Pagination, Search, Filter, and Sort Endpoints ---

    @Test
    void getResources_defaultPagination_returnsOkWithPageMetadata() throws Exception {
        ResourceResponse item = new ResourceResponse(
                1L, "Data Structures", "Arrays", ResourceCategory.NOTES,
                ResourceType.TEXT, "Content", null, null,
                "Student", LocalDateTime.now(), LocalDateTime.now()
        );
        PageResponse<ResourceResponse> mockPage = new PageResponse<>(
                List.of(item), 0, 10, 1, 1, true, true, false
        );

        when(resourceService.getResources(null, null, 0, 10, "newest"))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.content[0].title").value("Data Structures"));
    }

    @Test
    void getResources_invalidPage_returnsBadRequest() throws Exception {
        when(resourceService.getResources(null, null, -1, 10, "newest"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page index must not be less than zero"));

        mockMvc.perform(get("/api/resources?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page index must not be less than zero"));
    }

    @Test
    void getResources_invalidSize_returnsBadRequest() throws Exception {
        when(resourceService.getResources(null, null, 0, 0, "newest"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be greater than zero"));

        mockMvc.perform(get("/api/resources?size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page size must be greater than zero"));
    }

    @Test
    void getResources_excessiveSize_returnsBadRequest() throws Exception {
        when(resourceService.getResources(null, null, 0, 60, "newest"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must not exceed 50"));

        mockMvc.perform(get("/api/resources?size=60"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page size must not exceed 50"));
    }

    @Test
    void getResources_invalidSort_returnsBadRequest() throws Exception {
        when(resourceService.getResources(null, null, 0, 10, "unknown_col"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort option: unknown_col. Supported options: newest, oldest, title_asc, title_desc"));

        mockMvc.perform(get("/api/resources?sort=unknown_col"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid sort option: unknown_col. Supported options: newest, oldest, title_asc, title_desc"));
    }

    @Test
    void getResources_withSearchAndCategoryAndPagination() throws Exception {
        PageResponse<ResourceResponse> mockPage = new PageResponse<>(
                List.of(), 1, 5, 0, 0, false, true, true
        );

        when(resourceService.getResources(eq(ResourceCategory.ASSIGNMENTS), eq("Algorithms"), eq(1), eq(5), eq("title_asc")))
                .thenReturn(mockPage);

        mockMvc.perform(get("/api/resources?category=ASSIGNMENTS&search=Algorithms&page=1&size=5&sort=title_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.empty").value(true));
    }
}

