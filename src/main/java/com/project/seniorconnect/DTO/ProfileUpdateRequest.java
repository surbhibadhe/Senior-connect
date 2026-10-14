package com.project.seniorconnect.DTO;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio,

        @Size(max = 100, message = "Department must not exceed 100 characters")
        String department,

        Long id,
        String email,
        String role,
        String googleId
) {
    public ProfileUpdateRequest(String bio, String department) {
        this(bio, department, null, null, null, null);
    }
}
