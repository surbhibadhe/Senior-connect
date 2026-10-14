package com.project.seniorconnect.Service;

import com.project.seniorconnect.DTO.ContributorProfileResponse;
import com.project.seniorconnect.DTO.ProfileUpdateRequest;
import com.project.seniorconnect.DTO.UserProfileResponse;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Repository.ResourceRepository;
import com.project.seniorconnect.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public UserService(UserRepository userRepository, ResourceRepository resourceRepository) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String googleId) {
        User user = getUserEntityByGoogleId(googleId);
        return toUserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String googleId, ProfileUpdateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile update request body cannot be null");
        }

        // Reject attempts to modify identity, email, role, or Google subject
        if (request.id() != null
                || request.email() != null
                || request.role() != null
                || request.googleId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Modifying id, email, role, or Google identity is not permitted");
        }

        User user = getUserEntityByGoogleId(googleId);

        if (request.bio() != null) {
            String trimmedBio = request.bio().trim();
            user.setBio(trimmedBio.isEmpty() ? null : trimmedBio);
        }

        if (request.department() != null) {
            String trimmedDept = request.department().trim();
            user.setDepartment(trimmedDept.isEmpty() ? null : trimmedDept);
        }

        User savedUser = userRepository.save(user);
        return toUserProfileResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public ContributorProfileResponse getPublicContributorProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Contributor not found with id: " + userId));

        long totalResources = resourceRepository.countByUploadedById(userId);

        return new ContributorProfileResponse(
                user.getId(),
                user.getName(),
                user.getProfilePicture(),
                user.getBio(),
                user.getDepartment(),
                totalResources,
                user.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public User getUserEntityByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User profile not found in database"));
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfilePicture(),
                user.getRole(),
                user.getBio(),
                user.getDepartment(),
                user.isProfileComplete(),
                user.getCreatedAt()
        );
    }
}
