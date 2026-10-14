package com.project.seniorconnect.Config;

import com.project.seniorconnect.Entity.Role;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OidcUser oidcUser;

    @InjectMocks
    private OAuth2LoginSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getSubject()).thenReturn("google-sub-999");
        when(oidcUser.getEmail()).thenReturn("sachin.badhe@despu.edu.in");
        when(oidcUser.getFullName()).thenReturn("Sachin Badhe");
        when(oidcUser.getPicture()).thenReturn("https://lh3.googleusercontent.com/photo.jpg");
    }

    @Test
    void onAuthenticationSuccess_existingUser_preservesCustomBioDepartmentAndRole() throws Exception {
        User existingUser = new User();
        ReflectionTestUtils.setField(existingUser, "id", 10L);
        existingUser.setGoogleId("google-sub-999");
        existingUser.setEmail("sachin.badhe@despu.edu.in");
        existingUser.setName("Sachin");
        existingUser.setRole(Role.SENIOR);
        existingUser.setBio("My Custom Bio");
        existingUser.setDepartment("Computer Science");

        when(userRepository.findByGoogleId("google-sub-999")).thenReturn(Optional.of(existingUser));

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("google-sub-999", saved.getGoogleId());
        assertEquals("sachin.badhe@despu.edu.in", saved.getEmail());
        assertEquals("Sachin Badhe", saved.getName());
        assertEquals("https://lh3.googleusercontent.com/photo.jpg", saved.getProfilePicture());
        // Custom fields must be preserved on subsequent logins
        assertEquals("My Custom Bio", saved.getBio());
        assertEquals("Computer Science", saved.getDepartment());
        assertEquals(Role.SENIOR, saved.getRole());
        assertTrue(saved.isProfileComplete());

        verify(response).sendRedirect("/");
    }

    @Test
    void onAuthenticationSuccess_newUser_createsUserWithDefaultRoleAndIncompleteProfile() throws Exception {
        when(userRepository.findByGoogleId("google-sub-999")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("sachin.badhe@despu.edu.in")).thenReturn(Optional.empty());

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("google-sub-999", saved.getGoogleId());
        assertEquals("sachin.badhe@despu.edu.in", saved.getEmail());
        assertEquals("Sachin Badhe", saved.getName());
        assertEquals(Role.JUNIOR, saved.getRole());
        assertNull(saved.getBio());
        assertNull(saved.getDepartment());
        assertFalse(saved.isProfileComplete());

        verify(response).sendRedirect("/");
    }
}
