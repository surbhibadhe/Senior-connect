
package com.project.seniorconnect.Service;

import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ResourceCreateRequest;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.Entity.Resource;
import com.project.seniorconnect.Entity.ResourceCategory;
import com.project.seniorconnect.Entity.ResourceType;
import com.project.seniorconnect.Entity.Role;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Repository.BookmarkRepository;
import com.project.seniorconnect.Repository.CommentRepository;
import com.project.seniorconnect.Repository.CommentUpvoteRepository;
import com.project.seniorconnect.Repository.ResourceHelpfulRepository;
import com.project.seniorconnect.Repository.ResourceReportRepository;
import com.project.seniorconnect.Repository.ResourceRepository;
import com.project.seniorconnect.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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


@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ResourceHelpfulRepository resourceHelpfulRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ResourceReportRepository resourceReportRepository;
    private final CommentRepository commentRepository;
    private final CommentUpvoteRepository commentUpvoteRepository;

    public ResourceService(
            ResourceRepository resourceRepository,
            UserRepository userRepository) {
        this(resourceRepository, userRepository, null, null, null, null, null);
    }

    @Autowired
    public ResourceService(
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            @Autowired(required = false) ResourceHelpfulRepository resourceHelpfulRepository,
            @Autowired(required = false) BookmarkRepository bookmarkRepository,
            @Autowired(required = false) ResourceReportRepository resourceReportRepository,
            @Autowired(required = false) CommentRepository commentRepository,
            @Autowired(required = false) CommentUpvoteRepository commentUpvoteRepository) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.resourceHelpfulRepository = resourceHelpfulRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.resourceReportRepository = resourceReportRepository;
        this.commentRepository = commentRepository;
        this.commentUpvoteRepository = commentUpvoteRepository;
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
        resource.setDescription(request.description() != null ? request.description().trim() : null);
        resource.setCategory(request.category());
        resource.setType(type);
        resource.setTextContent(
                type == ResourceType.TEXT ? request.textContent() : null);
        resource.setResourceUrl(
                type == ResourceType.TEXT ? null : request.resourceUrl().trim());
        resource.setUploadedBy(user);

        Resource savedResource = resourceRepository.save(resource);

        return toResponse(savedResource);
    }


    @Transactional(readOnly = true)
    public PageResponse<ResourceResponse> getResources(
            ResourceCategory category,
            String search,
            int page,
            int size,
            String sortOption) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page index must not be less than zero");
        }

        if (size <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be greater than zero");
        }

        if (size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must not exceed 50");
        }

        Sort sort = resolveSort(sortOption);
        Pageable pageable = PageRequest.of(page, size, sort);

        String cleanSearch = (search != null && !search.trim().isBlank())
                ? search.trim()
                : null;

        Page<Resource> resourcePage;

        if (cleanSearch != null && category != null) {
            resourcePage = resourceRepository.findByCategoryAndSearch(category, cleanSearch, pageable);
        } else if (cleanSearch != null) {
            resourcePage = resourceRepository.findBySearch(cleanSearch, pageable);
        } else if (category != null) {
            resourcePage = resourceRepository.findByCategory(category, pageable);
        } else {
            resourcePage = resourceRepository.findAll(pageable);
        }

        Page<ResourceResponse> responsePage = resourcePage.map(this::toResponse);
        return PageResponse.from(responsePage);
    }


    @Transactional(readOnly = true)
    public List<ResourceResponse> getResources(
            ResourceCategory category,
            String search) {
        return getResources(category, search, 0, 50, "newest").content();
    }

    @Transactional(readOnly = true)
    public PageResponse<ResourceResponse> getResourcesByContributor(
            Long userId,
            int page,
            int size,
            String sortOption) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Contributor not found with id: " + userId);
        }

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page index must not be less than zero");
        }

        if (size < 1 || size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 50");
        }

        Sort sort = resolveSort(sortOption);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Resource> resourcePage = resourceRepository.findByUploadedById(userId, pageable);
        return PageResponse.from(resourcePage.map(this::toResponse));
    }

    public Sort resolveSort(String sortOption) {
        if (sortOption == null || sortOption.isBlank() || "newest".equalsIgnoreCase(sortOption.trim())) {
            return Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        }

        String normalized = sortOption.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "oldest" -> Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
            case "title_asc", "title" -> Sort.by(Sort.Order.asc("title").ignoreCase(), Sort.Order.desc("id"));
            case "title_desc" -> Sort.by(Sort.Order.desc("title").ignoreCase(), Sort.Order.desc("id"));
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort option: " + sortOption + ". Supported options: newest, oldest, title_asc, title_desc");
        };
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

        String safeStoredFileName = Paths.get(resource.getStoredFileName()).getFileName().toString();
        Path uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        Path filePath = uploadPath
                .resolve(safeStoredFileName)
                .normalize();

        if (!filePath.startsWith(uploadPath) || filePath.equals(uploadPath)) {
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

    public ResourceResponse toResponse(Resource resource) {
        long helpfulCount = (resourceHelpfulRepository != null && resource.getId() != null)
                ? resourceHelpfulRepository.countByResourceId(resource.getId())
                : 0L;
        long commentCount = (commentRepository != null && resource.getId() != null)
                ? commentRepository.countByResourceIdAndDeletedFalse(resource.getId())
                : 0L;

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
                resource.getUpdatedAt(),
                helpfulCount,
                commentCount
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

        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title is required");
        }

        if (title.trim().length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title must not exceed 200 characters");
        }

        if (description != null && description.length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Description must not exceed 2000 characters");
        }

        if (category == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category is required");
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

        String cleanOriginalName = Paths.get(originalName).getFileName().toString().trim();
        if (cleanOriginalName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid filename");
        }

        int dotIndex = cleanOriginalName.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == cleanOriginalName.length() - 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File must have a valid extension");
        }

        String extension = cleanOriginalName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);

        ResourceType type = switch (extension) {
            case "pdf" -> ResourceType.PDF;
            case "doc" -> ResourceType.DOC;
            case "docx" -> ResourceType.DOCX;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF, DOC, and DOCX files are allowed");
        };

        String rawContentType = file.getContentType();
        String contentType = rawContentType;
        if (contentType != null) {
            int semicolon = contentType.indexOf(';');
            if (semicolon != -1) {
                contentType = contentType.substring(0, semicolon).trim();
            }
        }

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

        if (!destination.startsWith(uploadPath) || destination.equals(uploadPath)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid upload path");
        }

        try {
            Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save uploaded file");
        }

        Resource resource = new Resource();
        resource.setTitle(title.trim());
        resource.setDescription(description != null ? description.trim() : null);
        resource.setCategory(category);
        resource.setType(type);
        resource.setOriginalFileName(cleanOriginalName);
        resource.setStoredFileName(storedName);
        resource.setUploadedBy(user);

        try {
            Resource saved = resourceRepository.saveAndFlush(resource);
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

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Resource not found"));

        if (user.getRole() != Role.ADMIN
                && !resource.getUploadedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can delete only resources you uploaded");
        }

        String storedFileName = resource.getStoredFileName();

        if (commentUpvoteRepository != null) {
            commentUpvoteRepository.deleteByResourceId(id);
        }
        if (commentRepository != null) {
            commentRepository.deleteByResourceId(id);
        }
        if (bookmarkRepository != null) {
            bookmarkRepository.deleteByResourceId(id);
        }
        if (resourceHelpfulRepository != null) {
            resourceHelpfulRepository.deleteByResourceId(id);
        }
        if (resourceReportRepository != null) {
            resourceReportRepository.disassociateResource(id);
        }

        resourceRepository.delete(resource);
        resourceRepository.flush();

        if (storedFileName != null && !storedFileName.isBlank()) {
            String safeFileName = Paths.get(storedFileName).getFileName().toString();
            Path uploadPath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Path filePath = uploadPath
                    .resolve(safeFileName)
                    .normalize();

            if (!filePath.startsWith(uploadPath) || filePath.equals(uploadPath)) {
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
    }


    @Transactional
    public ResourceResponse updateResource(
            Long id,
            ResourceCreateRequest request,
            String googleId) {

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User profile not found"));

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Resource not found"));

        // Students can update only their own resources. Admins can update any resource.
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

        if (request.type() == ResourceType.TEXT) {
            if (request.textContent() == null || request.textContent().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Text content is required for TEXT resources");
            }
        } else if (request.type() == ResourceType.GOOGLE_DRIVE_LINK
                || request.type() == ResourceType.EXTERNAL_LINK) {
            validateUrl(request.resourceUrl());
        }

        resource.setTitle(request.title().trim());
        resource.setDescription(request.description() != null ? request.description().trim() : null);
        resource.setCategory(request.category());
        resource.setType(request.type());
        resource.setTextContent(request.type() == ResourceType.TEXT ? request.textContent() : null);
        resource.setResourceUrl(request.type() == ResourceType.TEXT ? null : request.resourceUrl().trim());

        return toResponse(resourceRepository.save(resource));
    }
}
