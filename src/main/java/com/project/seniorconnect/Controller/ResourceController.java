package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.BookmarkStatusResponse;
import com.project.seniorconnect.DTO.HelpfulStatusResponse;
import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ResourceCreateRequest;
import com.project.seniorconnect.DTO.ResourceReportRequest;
import com.project.seniorconnect.DTO.ResourceReportResponse;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.Entity.ResourceCategory;
import com.project.seniorconnect.Service.EngagementService;
import com.project.seniorconnect.Service.ModerationService;
import com.project.seniorconnect.Service.ResourceService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final EngagementService engagementService;
    private final ModerationService moderationService;

    public ResourceController(ResourceService resourceService) {
        this(resourceService, null, null);
    }

    @Autowired
    public ResourceController(
            ResourceService resourceService,
            @Autowired(required = false) EngagementService engagementService,
            @Autowired(required = false) ModerationService moderationService) {
        this.resourceService = resourceService;
        this.engagementService = engagementService;
        this.moderationService = moderationService;
    }

    // Get all resources with pagination, search, category filtering, and sorting
    @GetMapping
    public PageResponse<ResourceResponse> getResources(
            @RequestParam(required = false) ResourceCategory category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {

        return resourceService.getResources(category, search, page, size, sort);
    }

    // Get a single resource by ID
    @GetMapping("/{id}")
    public ResourceResponse getResourceById(@PathVariable Long id) {
        return resourceService.getResourceById(id);
    }

    // Create a text or link resource
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponse createResource(
            @Valid @RequestBody ResourceCreateRequest request,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

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

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

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
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) throws IOException {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

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

        String rawFilename = resource.getOriginalFileName();
        String safeFilename;
        if (rawFilename == null || rawFilename.isBlank()) {
            safeFilename = "download";
        } else {
            safeFilename = Paths.get(rawFilename).getFileName().toString();
        }

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(safeFilename, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(file);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        resourceService.deleteResource(id, googleUser.getSubject());
    }

    @PutMapping("/{id}")
    public ResourceResponse updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceCreateRequest request,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return resourceService.updateResource(
                id, request, googleUser.getSubject());
    }

    // Bookmark a resource
    @PostMapping("/{id}/bookmark")
    public BookmarkStatusResponse bookmarkResource(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return engagementService.bookmarkResource(id, googleUser.getSubject());
    }

    // Remove a bookmark
    @DeleteMapping("/{id}/bookmark")
    public BookmarkStatusResponse removeBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return engagementService.removeBookmark(id, googleUser.getSubject());
    }

    // Check whether resource is bookmarked by current user
    @GetMapping("/{id}/bookmark")
    public BookmarkStatusResponse getBookmarkStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return engagementService.getBookmarkStatus(id, googleUser.getSubject());
    }

    // Mark resource as helpful
    @PostMapping("/{id}/helpful")
    public HelpfulStatusResponse markHelpful(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return engagementService.markHelpful(id, googleUser.getSubject());
    }

    // Remove helpful mark
    @DeleteMapping("/{id}/helpful")
    public HelpfulStatusResponse removeHelpful(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return engagementService.removeHelpful(id, googleUser.getSubject());
    }

    // Retrieve helpfulness count and user's helpful status
    @GetMapping("/{id}/helpful")
    public HelpfulStatusResponse getHelpfulStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return engagementService.getHelpfulStatus(id, googleUser.getSubject());
    }

    // Report a resource
    @PostMapping("/{id}/report")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceReportResponse reportResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceReportRequest request,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return moderationService.reportResource(id, request, googleUser.getSubject());
    }
}
