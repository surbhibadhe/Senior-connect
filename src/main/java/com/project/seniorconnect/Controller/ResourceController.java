package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.ResourceCreateRequest;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.Entity.ResourceCategory;
import com.project.seniorconnect.Service.ResourceService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    // Get all resources or filter by category
    @GetMapping
    public List<ResourceResponse> getResources(
            @RequestParam(required = false) ResourceCategory category) {

        return resourceService.getResources(category);
    }

    // Get a single resource by ID
    @GetMapping("/{id}")
    public ResourceResponse getResourceById(@PathVariable Long id) {
        return resourceService.getResourceById(id);
    }

    // Create a resource (SENIOR or ADMIN only)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse createResource(
            @Valid @RequestBody ResourceCreateRequest request,
            @AuthenticationPrincipal OidcUser googleUser) {

        return resourceService.createResource(
                request,
                googleUser.getSubject()
        );
    }
}
