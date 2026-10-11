
package com.project.seniorconnect.DTO;

import com.project.seniorconnect.Entity.ResourceCategory;
import com.project.seniorconnect.Entity.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResourceCreateRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        @Size(max = 2000)
        String description,

        @NotNull
        ResourceCategory category,

        @NotNull
        ResourceType type,

        String textContent,

        @Size(max = 2048)
        String resourceUrl

) {
}
