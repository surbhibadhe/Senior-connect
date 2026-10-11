
package com.project.seniorconnect.DTO;

import com.project.seniorconnect.Entity.Role;
import java.time.LocalDateTime;

public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String profilePicture;
    private Role role;
    private LocalDateTime createdAt;

    public UserProfileResponse(
            Long id,
            String name,
            String email,
            String profilePicture,
            Role role,
            LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profilePicture = profilePicture;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
