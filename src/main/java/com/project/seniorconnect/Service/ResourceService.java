
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


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Locale;

import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;


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


    @Value("${app.upload.dir:uploads}")
    private String uploadDir;


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
            ResourceCategory category,
            String search) {

        List<Resource> resources;

        if (search != null && !search.isBlank()) {
            resources =
                    resourceRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                            search.trim());

            if (category != null) {
                resources = resources.stream()
                        .filter(resource -> resource.getCategory() == category)
                        .toList();
            }
        } else if (category != null) {
            resources =
                    resourceRepository.findByCategoryOrderByCreatedAtDesc(category);
        } else {
            resources = resourceRepository.findAllByOrderByCreatedAtDesc();
        }

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


    public org.springframework.core.io.Resource getDownloadFile(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resource not found"));

        if (resource.getStoredFileName() == null
                || resource.getStoredFileName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No uploaded file exists for this resource");
        }

        Path uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        Path filePath = uploadPath
                .resolve(resource.getStoredFileName())
                .normalize();

        if (!filePath.startsWith(uploadPath)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file path");
        }

        try {
            org.springframework.core.io.Resource file =
                    new UrlResource(filePath.toUri());

            if (!file.exists() || !file.isReadable()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Uploaded file not found on server");
            }

            return file;

        } catch (MalformedURLException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not read uploaded file");
        }
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


    @Transactional
    public ResourceResponse uploadResource(
            String title,
            String description,
            ResourceCategory category,
            MultipartFile file,
            String googleId) {

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User profile not found"));

        if (user.getRole() != Role.SENIOR
                && user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only seniors and admins can upload resources");
        }

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a file to upload");
        }

        if (file.getSize() > 10L * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File size must not exceed 10 MB");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid filename");
        }

        String extension = originalName.substring(
                        originalName.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);

        ResourceType type;

        switch (extension) {
            case "pdf" -> type = ResourceType.PDF;
            case "doc" -> type = ResourceType.DOC;
            case "docx" -> type = ResourceType.DOCX;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF, DOC, and DOCX files are allowed");
        }

        String contentType = file.getContentType();

        boolean validContentType = switch (type) {
            case PDF -> "application/pdf".equalsIgnoreCase(contentType);
            case DOC -> "application/msword".equalsIgnoreCase(contentType);
            case DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    .equalsIgnoreCase(contentType);
            default -> false;
        };

        if (!validContentType) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File content type does not match the file extension");
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path destination = uploadPath.resolve(storedName).normalize();

        if (!destination.startsWith(uploadPath)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid upload path");
        }

        try {
            Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), destination);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save uploaded file");
        }

        Resource resource = new Resource();
        resource.setTitle(title.trim());
        resource.setDescription(description);
        resource.setCategory(category);
        resource.setType(type);
        resource.setOriginalFileName(originalName);
        resource.setStoredFileName(storedName);
        resource.setUploadedBy(user);

        try {
            Resource saved = resourceRepository.save(resource);
            return toResponse(saved);
        } catch (RuntimeException exception) {
            try {
                Files.deleteIfExists(destination);
            } catch (IOException ignored) {
                // Keep the original database error.
            }
            throw exception;
        }
    }


    @Transactional(readOnly = true)
    public Resource getResourceEntityForDownload(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resource not found"));
    }



    @Transactional
    public void deleteResource(Long id, String googleId) {
        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User profile not found"));

        if (user.getRole() != Role.SENIOR
                && user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only seniors and admins can delete resources");
        }

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Resource not found"));

        if (resource.getStoredFileName() != null
                && !resource.getStoredFileName().isBlank()) {
            Path filePath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize()
                    .resolve(resource.getStoredFileName())
                    .normalize();

            Path uploadPath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            if (!filePath.startsWith(uploadPath)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid file path");
            }

            try {
                Files.deleteIfExists(filePath);
            } catch (IOException exception) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Could not delete uploaded file");
            }
        }

        resourceRepository.delete(resource);
    }


    @Transactional
    public ResourceResponse updateResource(
            Long id,
            ResourceCreateRequest request,
            String googleId) {

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != Role.SENIOR
                && user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only seniors and admins can update resources");
        }

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Resource not found"));

        // Seniors can update only their own resources.
        if (user.getRole() != Role.ADMIN
                && !resource.getUploadedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can update only resources you uploaded");
        }

        // This endpoint updates resource details, not uploaded files.
        if (request.type() == ResourceType.PDF
                || request.type() == ResourceType.DOC
                || request.type() == ResourceType.DOCX) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use the upload endpoint to add a file");
        }

        resource.setTitle(request.title());
        resource.setDescription(request.description());
        resource.setCategory(request.category());
        resource.setType(request.type());
        resource.setTextContent(request.textContent());
        resource.setResourceUrl(request.resourceUrl());

        return toResponse(resourceRepository.save(resource));
    }




}
