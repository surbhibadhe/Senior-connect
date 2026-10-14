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

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.IOException;

import com.project.seniorconnect.DTO.ResourceCreateRequest;
import com.project.seniorconnect.DTO.ResourceResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
    
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
            @RequestParam(required = false) ResourceCategory category,
            @RequestParam(required = false) String search) {

        return resourceService.getResources(category, search);
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


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse uploadResource(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("category") ResourceCategory category,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal OidcUser googleUser) {

        return resourceService.uploadResource(
                title,
                description,
                category,
                file,
                googleUser.getSubject()
        );
    }


    @GetMapping("/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(
            @PathVariable Long id) throws IOException {

        com.project.seniorconnect.Entity.Resource resource =
                resourceService.getResourceEntityForDownload(id);

        org.springframework.core.io.Resource file =
                resourceService.getDownloadFile(id);

        String contentType = switch (resource.getType()) {
            case PDF -> "application/pdf";
            case DOC -> "application/msword";
            case DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/octet-stream";
        };

        String filename = resource.getOriginalFileName() == null
                ? "download"
                : resource.getOriginalFileName().replace("\"", "");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(file);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) {

        resourceService.deleteResource(id, googleUser.getSubject());
    }



    @PutMapping("/{id}")
    public ResourceResponse updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceCreateRequest request,
            @AuthenticationPrincipal OidcUser googleUser) {

        return resourceService.updateResource(
                id, request, googleUser.getSubject());
    }




}
