package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.ContributorProfileResponse;
import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ProfileUpdateRequest;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.DTO.UserProfileResponse;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Service.ResourceService;
import com.project.seniorconnect.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ResourceService resourceService;

    public UserController(UserService userService, ResourceService resourceService) {
        this.userService = userService;
        this.resourceService = resourceService;
    }

    // 1. Get current authenticated student's profile
    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return userService.getCurrentUserProfile(googleUser.getSubject());
    }

    // 2. Update current authenticated student's profile
    @PutMapping("/me")
    public UserProfileResponse updateCurrentUser(
            @Valid @RequestBody ProfileUpdateRequest request,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return userService.updateProfile(googleUser.getSubject(), request);
    }

    // 3. Prevent one student from modifying another student's profile by ID
    @PutMapping("/{id}")
    public UserProfileResponse updateUserById(
            @PathVariable Long id,
            @Valid @RequestBody ProfileUpdateRequest request,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        User currentUser = userService.getUserEntityByGoogleId(googleUser.getSubject());
        if (!currentUser.getId().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot modify another student's profile");
        }

        return userService.updateProfile(googleUser.getSubject(), request);
    }

    // 4. Get public contributor profile by user ID
    @GetMapping("/{id}")
    public ContributorProfileResponse getPublicContributorProfile(
            @PathVariable Long id) {

        return userService.getPublicContributorProfile(id);
    }

    // 5. List a contributor's shared resources with pagination and sorting
    @GetMapping("/{id}/resources")
    public PageResponse<ResourceResponse> getContributorResources(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {

        return resourceService.getResourcesByContributor(id, page, size, sort);
    }
}
