package com.project.seniorconnect.Service;

import com.project.seniorconnect.DTO.ContributorProfileResponse;
import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ProfileUpdateRequest;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.DTO.UserProfileResponse;
import com.project.seniorconnect.Entity.Resource;
import com.project.seniorconnect.Entity.ResourceCategory;
import com.project.seniorconnect.Entity.ResourceType;
import com.project.seniorconnect.Entity.Role;
import com.project.seniorconnect.Entity.User;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        ReflectionTestUtils.setField(sampleUser, "id", 1L);
        sampleUser.setName("Sachin Badhe");
        sampleUser.setEmail("sachin.badhe@despu.edu.in");
        sampleUser.setGoogleId("google-12345");
        sampleUser.setProfilePicture("https://lh3.googleusercontent.com/photo.jpg");
        sampleUser.setRole(Role.JUNIOR);
    }

    @Test
    void getCurrentUserProfile_success_incompleteProfile() {
        when(userRepository.findByGoogleId("google-12345")).thenReturn(Optional.of(sampleUser));

        UserProfileResponse response = userService.getCurrentUserProfile("google-12345");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Sachin Badhe", response.getName());
        assertEquals("sachin.badhe@despu.edu.in", response.getEmail());
        assertEquals("https://lh3.googleusercontent.com/photo.jpg", response.getProfilePicture());
        assertEquals(Role.JUNIOR, response.getRole());
        assertNull(response.getBio());
        assertNull(response.getDepartment());
        assertFalse(response.isProfileComplete(), "Profile should be incomplete when department is missing");
    }

    @Test
    void getCurrentUserProfile_success_completeProfile() {
        sampleUser.setBio("Interested in Systems Programming");
        sampleUser.setDepartment("Computer Science");
        when(userRepository.findByGoogleId("google-12345")).thenReturn(Optional.of(sampleUser));

        UserProfileResponse response = userService.getCurrentUserProfile("google-12345");

        assertNotNull(response);
        assertEquals("Interested in Systems Programming", response.getBio());
        assertEquals("Computer Science", response.getDepartment());
        assertTrue(response.isProfileComplete(), "Profile should be complete when department is set");
    }

    @Test
    void getCurrentUserProfile_userNotFound_throwsNotFound() {
        when(userRepository.findByGoogleId("google-unknown")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.getCurrentUserProfile("google-unknown"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User profile not found in database", ex.getReason());
    }

    @Test
    void updateProfile_permittedFields_updatesBioAndDepartment() {
        when(userRepository.findByGoogleId("google-12345")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "  Software engineer enthusiast  ",
                "  Information Technology  "
        );

        UserProfileResponse response = userService.updateProfile("google-12345", request);

        assertNotNull(response);
        assertEquals("Software engineer enthusiast", response.getBio());
        assertEquals("Information Technology", response.getDepartment());
        assertTrue(response.isProfileComplete());
        verify(userRepository).save(sampleUser);
    }

    @Test
    void updateProfile_clearFields_withEmptyStrings() {
        sampleUser.setBio("Old Bio");
        sampleUser.setDepartment("Old Dept");
        when(userRepository.findByGoogleId("google-12345")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileUpdateRequest request = new ProfileUpdateRequest("   ", "   ");

        UserProfileResponse response = userService.updateProfile("google-12345", request);

        assertNotNull(response);
        assertNull(response.getBio());
        assertNull(response.getDepartment());
        assertFalse(response.isProfileComplete());
    }

    @Test
    void updateProfile_rejectEmailModification() {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "Bio",
                "Dept",
                null,
                "attacker@despu.edu.in",
                null,
                null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateProfile("google-12345", request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Modifying id, email, role, or Google identity is not permitted"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_rejectRoleModification() {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "Bio",
                "Dept",
                null,
                null,
                "ADMIN",
                null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateProfile("google-12345", request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_rejectGoogleIdModification() {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "Bio",
                "Dept",
                null,
                null,
                null,
                "hacker-google-id"
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateProfile("google-12345", request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_rejectIdModification() {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "Bio",
                "Dept",
                999L,
                null,
                null,
                null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateProfile("google-12345", request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_nullRequest_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateProfile("google-12345", null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getPublicContributorProfile_success_privacyGuaranteed() {
        sampleUser.setBio("Senior tutor in Mathematics");
        sampleUser.setDepartment("Mathematics");
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(resourceRepository.countByUploadedById(1L)).thenReturn(7L);

        ContributorProfileResponse response = userService.getPublicContributorProfile(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Sachin Badhe", response.name());
        assertEquals("https://lh3.googleusercontent.com/photo.jpg", response.profilePicture());
        assertEquals("Senior tutor in Mathematics", response.bio());
        assertEquals("Mathematics", response.department());
        assertEquals(7L, response.totalResources());
        assertNotNull(response.createdAt());
    }

    @Test
    void getPublicContributorProfile_notFound_throwsNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.getPublicContributorProfile(999L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Contributor not found with id: 999", ex.getReason());
    }

    @Test
    void getUserEntityByGoogleId_found_returnsEntity() {
        when(userRepository.findByGoogleId("google-12345")).thenReturn(Optional.of(sampleUser));

        User user = userService.getUserEntityByGoogleId("google-12345");

        assertNotNull(user);
        assertEquals(1L, user.getId());
    }

    @Test
    void getUserEntityByGoogleId_notFound_throwsNotFound() {
        when(userRepository.findByGoogleId("google-missing")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                userService.getUserEntityByGoogleId("google-missing"));
    }
}
