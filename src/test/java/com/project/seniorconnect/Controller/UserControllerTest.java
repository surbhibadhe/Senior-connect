package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.ContributorProfileResponse;
import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ProfileUpdateRequest;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.DTO.UserProfileResponse;
import com.project.seniorconnect.Entity.ResourceCategory;
import com.project.seniorconnect.Entity.ResourceType;
import com.project.seniorconnect.Entity.Role;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Exception.GlobalExceptionHandler;
import com.project.seniorconnect.Service.ResourceService;
import com.project.seniorconnect.Service.UserService;
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
import org.springframework.test.util.ReflectionTestUtils;
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
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvcUnauth;
    private MockMvc mockMvcAuth;

    private MockMvc mockMvcProfileUnauth;
    private MockMvc mockMvcProfileAuth;

    private OidcUser testOidcUser;

    @BeforeEach
    void setUp() {
        // Resolver that returns null for @AuthenticationPrincipal (unauthenticated)
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

        // Authenticated user token
        OidcIdToken idToken = new OidcIdToken(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("sub", "google-student-1", "email", "student1@despu.edu.in", "email_verified", true)
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

        mockMvcUnauth = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(unauthResolver)
                .build();

        mockMvcAuth = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authResolver)
                .build();

        ProfileController profileController = new ProfileController(userService);
        mockMvcProfileUnauth = MockMvcBuilders.standaloneSetup(profileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(unauthResolver)
                .build();

        mockMvcProfileAuth = MockMvcBuilders.standaloneSetup(profileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authResolver)
                .build();
    }

    @Test
    void getCurrentUser_unauthenticated_returns401() throws Exception {
        mockMvcUnauth.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    @Test
    void getCurrentUser_authenticated_returns200() throws Exception {
        UserProfileResponse mockProfile = new UserProfileResponse(
                1L,
                "Student One",
                "student1@despu.edu.in",
                "https://lh3.googleusercontent.com/photo.jpg",
                Role.JUNIOR,
                "Passionate about Web Technologies",
                "Computer Science",
                true,
                LocalDateTime.now()
        );
        when(userService.getCurrentUserProfile("google-student-1")).thenReturn(mockProfile);

        mockMvcAuth.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Student One"))
                .andExpect(jsonPath("$.email").value("student1@despu.edu.in"))
                .andExpect(jsonPath("$.profilePicture").value("https://lh3.googleusercontent.com/photo.jpg"))
                .andExpect(jsonPath("$.role").value("JUNIOR"))
                .andExpect(jsonPath("$.bio").value("Passionate about Web Technologies"))
                .andExpect(jsonPath("$.department").value("Computer Science"))
                .andExpect(jsonPath("$.profileComplete").value(true));
    }

    @Test
    void updateCurrentUser_unauthenticated_returns401() throws Exception {
        mockMvcUnauth.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"Hello\",\"department\":\"CS\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateCurrentUser_permittedFields_returns200() throws Exception {
        UserProfileResponse updatedProfile = new UserProfileResponse(
                1L,
                "Student One",
                "student1@despu.edu.in",
                "https://lh3.googleusercontent.com/photo.jpg",
                Role.JUNIOR,
                "Updated Bio",
                "Information Technology",
                true,
                LocalDateTime.now()
        );
        when(userService.updateProfile(eq("google-student-1"), any(ProfileUpdateRequest.class)))
                .thenReturn(updatedProfile);

        mockMvcAuth.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"Updated Bio\",\"department\":\"Information Technology\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Updated Bio"))
                .andExpect(jsonPath("$.department").value("Information Technology"))
                .andExpect(jsonPath("$.profileComplete").value(true));
    }

    @Test
    void updateCurrentUser_rejectsEmailModification_returns400() throws Exception {
        when(userService.updateProfile(eq("google-student-1"), any(ProfileUpdateRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modifying id, email, role, or Google identity is not permitted"));

        mockMvcAuth.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"New Bio\",\"email\":\"hacker@despu.edu.in\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Modifying id, email, role, or Google identity is not permitted"));
    }

    @Test
    void updateCurrentUser_rejectsRoleModification_returns400() throws Exception {
        when(userService.updateProfile(eq("google-student-1"), any(ProfileUpdateRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modifying id, email, role, or Google identity is not permitted"));

        mockMvcAuth.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"New Bio\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Modifying id, email, role, or Google identity is not permitted"));
    }

    @Test
    void updateCurrentUser_rejectsGoogleIdModification_returns400() throws Exception {
        when(userService.updateProfile(eq("google-student-1"), any(ProfileUpdateRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modifying id, email, role, or Google identity is not permitted"));

        mockMvcAuth.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"New Bio\",\"googleId\":\"hacker-sub\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCurrentUser_bioExceedsLength_returns400ValidationFailed() throws Exception {
        String longBio = "A".repeat(501);
        mockMvcAuth.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"" + longBio + "\",\"department\":\"CS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.bio").value("Bio must not exceed 500 characters"));
    }

    @Test
    void updateCurrentUser_departmentExceedsLength_returns400ValidationFailed() throws Exception {
        String longDept = "D".repeat(101);
        mockMvcAuth.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"Bio\",\"department\":\"" + longDept + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.department").value("Department must not exceed 100 characters"));
    }

    @Test
    void updateUserById_sameStudent_succeeds() throws Exception {
        User studentUser = new User();
        ReflectionTestUtils.setField(studentUser, "id", 1L);
        when(userService.getUserEntityByGoogleId("google-student-1")).thenReturn(studentUser);

        UserProfileResponse updatedProfile = new UserProfileResponse(
                1L, "Student One", "student1@despu.edu.in", null, Role.JUNIOR, "Self Bio", "CS", true, LocalDateTime.now());
        when(userService.updateProfile(eq("google-student-1"), any(ProfileUpdateRequest.class)))
                .thenReturn(updatedProfile);

        mockMvcAuth.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"Self Bio\",\"department\":\"CS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Self Bio"));
    }

    @Test
    void updateUserById_differentStudent_returns403Forbidden() throws Exception {
        User studentUser = new User();
        ReflectionTestUtils.setField(studentUser, "id", 1L);
        when(userService.getUserEntityByGoogleId("google-student-1")).thenReturn(studentUser);

        mockMvcAuth.perform(put("/api/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"Hacked Bio\",\"department\":\"CS\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You cannot modify another student's profile"));
    }

    @Test
    void getPublicContributorProfile_privacyCheck_returns200() throws Exception {
        ContributorProfileResponse contributor = new ContributorProfileResponse(
                5L,
                "Senior Contributor",
                "https://lh3.googleusercontent.com/senior.jpg",
                "Algorithms and Data Structures Mentor",
                "Computer Science",
                12L,
                LocalDateTime.now()
        );
        when(userService.getPublicContributorProfile(5L)).thenReturn(contributor);

        mockMvcAuth.perform(get("/api/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Senior Contributor"))
                .andExpect(jsonPath("$.profilePicture").value("https://lh3.googleusercontent.com/senior.jpg"))
                .andExpect(jsonPath("$.bio").value("Algorithms and Data Structures Mentor"))
                .andExpect(jsonPath("$.department").value("Computer Science"))
                .andExpect(jsonPath("$.totalResources").value(12))
                .andExpect(jsonPath("$.createdAt").exists())
                // Assert sensitive and identity fields are NEVER exposed in public contributor profiles
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.googleId").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.credentials").doesNotExist());
    }

    @Test
    void getPublicContributorProfile_notFound_returns404() throws Exception {
        when(userService.getPublicContributorProfile(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Contributor not found with id: 999"));

        mockMvcAuth.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contributor not found with id: 999"));
    }

    @Test
    void getContributorResources_returns200WithPagination() throws Exception {
        ResourceResponse item = new ResourceResponse(
                10L, "DSA Lab Manual", "Full solutions",
                ResourceCategory.LAB_MANUALS, ResourceType.PDF, null, null, "dsa_lab.pdf",
                "Senior Contributor", LocalDateTime.now(), LocalDateTime.now()
        );
        PageResponse<ResourceResponse> mockPage = new PageResponse<>(
                List.of(item), 0, 10, 1L, 1, true, true, false
        );
        when(resourceService.getResourcesByContributor(5L, 0, 10, "newest")).thenReturn(mockPage);

        mockMvcAuth.perform(get("/api/users/5/resources?page=0&size=10&sort=newest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("DSA Lab Manual"))
                .andExpect(jsonPath("$.content[0].uploadedByName").value("Senior Contributor"));
    }

    @Test
    void getContributorResources_unknownUser_returns404() throws Exception {
        when(resourceService.getResourcesByContributor(999L, 0, 10, "newest"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Contributor not found with id: 999"));

        mockMvcAuth.perform(get("/api/users/999/resources"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contributor not found with id: 999"));
    }

    @Test
    void getContributorResources_invalidSort_returns400() throws Exception {
        when(resourceService.getResourcesByContributor(5L, 0, 10, "invalid_sort"))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort option: invalid_sort"));

        mockMvcAuth.perform(get("/api/users/5/resources?sort=invalid_sort"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid sort option: invalid_sort"));
    }

    @Test
    void getProfileEndpoint_authenticated_returns200() throws Exception {
        UserProfileResponse mockProfile = new UserProfileResponse(
                1L, "Student One", "student1@despu.edu.in", null, Role.JUNIOR, "Bio", "CS", true, LocalDateTime.now());
        when(userService.getCurrentUserProfile("google-student-1")).thenReturn(mockProfile);

        mockMvcProfileAuth.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Student One"))
                .andExpect(jsonPath("$.department").value("CS"));
    }

    @Test
    void getProfileEndpoint_unauthenticated_returns401() throws Exception {
        mockMvcProfileUnauth.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfileEndpoint_authenticated_returns200() throws Exception {
        UserProfileResponse updatedProfile = new UserProfileResponse(
                1L, "Student One", "student1@despu.edu.in", null, Role.JUNIOR, "New Bio", "CS", true, LocalDateTime.now());
        when(userService.updateProfile(eq("google-student-1"), any(ProfileUpdateRequest.class)))
                .thenReturn(updatedProfile);

        mockMvcProfileAuth.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"New Bio\",\"department\":\"CS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("New Bio"));
    }
}
