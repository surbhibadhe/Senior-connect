
package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.UserProfileResponse;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        String googleId = googleUser.getSubject();

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User profile not found in database"));

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfilePicture(),
                user.getRole(),
                user.getCreatedAt());
    }
}
