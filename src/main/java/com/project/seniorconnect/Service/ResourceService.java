
package com.project.seniorconnect.Service;

import com.project.seniorconnect.DTO.ResourceCreateRequest;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.Entity.Resource;
import com.project.seniorconnect.Entity.ResourceCategory;
import com.project.seniorconnect.Entity.ResourceType;
import com.project.seniorconnect.Entity.Role;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Repository.ResourceRepository;
import com.project.seniorconnect.Repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ResourceService(
            ResourceRepository resourceRepository,
            UserRepository userRepository) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResourceResponse createResource(
            ResourceCreateRequest request,
            String googleId) {

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User profile not found"));

        if (user.getRole() != Role.SENIOR
                && user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only seniors and admins can create resources");
        }

        ResourceType type = request.type();

        if (type == ResourceType.TEXT) {
            if (request.textContent() == null
                    || request.textContent().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Text content is required for TEXT resources");
            }
        } else if (type == ResourceType.GOOGLE_DRIVE_LINK
                || type == ResourceType.EXTERNAL_LINK) {
            validateUrl(request.resourceUrl());
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "PDF, DOC, and DOCX uploads are not supported yet");
        }

        Resource resource = new Resource();
        resource.setTitle(request.title().trim());
        resource.setDescription(request.description());
        resource.setCategory(request.category());
        resource.setType(type);
        resource.setTextContent(
                type == ResourceType.TEXT ? request.textContent() : null);
        resource.setResourceUrl(
                type == ResourceType.TEXT ? null : request.resourceUrl());
        resource.setUploadedBy(user);

        Resource savedResource = resourceRepository.save(resource);

        return toResponse(savedResource);
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> getResources(
            ResourceCategory category) {

        List<Resource> resources = category == null
                ? resourceRepository.findAllByOrderByCreatedAtDesc()
                : resourceRepository.findByCategoryOrderByCreatedAtDesc(category);

        return resources.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resource not found"));

        return toResponse(resource);
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A resource URL is required");
        }

        try {
            URI uri = URI.create(url.trim());

            if (!uri.isAbsolute()
                    || uri.getHost() == null
                    || !("https".equalsIgnoreCase(uri.getScheme())
                    || "http".equalsIgnoreCase(uri.getScheme()))) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please provide a valid HTTP or HTTPS URL");
        }
    }

    private ResourceResponse toResponse(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getTitle(),
                resource.getDescription(),
                resource.getCategory(),
                resource.getType(),
                resource.getTextContent(),
                resource.getResourceUrl(),
                resource.getOriginalFileName(),
                resource.getUploadedBy().getName(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}
