package com.project.seniorconnect.Service;

import com.project.seniorconnect.DTO.BookmarkStatusResponse;
import com.project.seniorconnect.DTO.HelpfulStatusResponse;
import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.Entity.Bookmark;
import com.project.seniorconnect.Entity.Resource;
import com.project.seniorconnect.Entity.ResourceHelpful;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Repository.BookmarkRepository;
import com.project.seniorconnect.Repository.ResourceHelpfulRepository;
import com.project.seniorconnect.Repository.ResourceRepository;
import com.project.seniorconnect.Repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EngagementService {

    private final BookmarkRepository bookmarkRepository;
    private final ResourceHelpfulRepository resourceHelpfulRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ResourceService resourceService;

    public EngagementService(
            BookmarkRepository bookmarkRepository,
            ResourceHelpfulRepository resourceHelpfulRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            ResourceService resourceService) {
        this.bookmarkRepository = bookmarkRepository;
        this.resourceHelpfulRepository = resourceHelpfulRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.resourceService = resourceService;
    }

    @Transactional
    public BookmarkStatusResponse bookmarkResource(Long resourceId, String googleId) {
        User user = getUserByGoogleId(googleId);
        Resource resource = getResourceById(resourceId);

        if (bookmarkRepository.existsByUserIdAndResourceId(user.getId(), resource.getId())) {
            return new BookmarkStatusResponse(resourceId, true, "Resource is already bookmarked");
        }

        bookmarkRepository.save(new Bookmark(user, resource));
        return new BookmarkStatusResponse(resourceId, true, "Resource bookmarked successfully");
    }

    @Transactional
    public BookmarkStatusResponse removeBookmark(Long resourceId, String googleId) {
        User user = getUserByGoogleId(googleId);
        // Verify resource existence
        getResourceById(resourceId);

        bookmarkRepository.deleteByUserIdAndResourceId(user.getId(), resourceId);
        return new BookmarkStatusResponse(resourceId, false, "Bookmark removed successfully");
    }

    @Transactional(readOnly = true)
    public BookmarkStatusResponse getBookmarkStatus(Long resourceId, String googleId) {
        User user = getUserByGoogleId(googleId);
        getResourceById(resourceId);

        boolean bookmarked = bookmarkRepository.existsByUserIdAndResourceId(user.getId(), resourceId);
        return new BookmarkStatusResponse(resourceId, bookmarked);
    }

    @Transactional(readOnly = true)
    public PageResponse<ResourceResponse> getBookmarkedResources(
            String googleId,
            int page,
            int size,
            String sortOption) {

        User user = getUserByGoogleId(googleId);

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

        // Whitelist sort validation
        Sort sort = resourceService.resolveSort(sortOption);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUserId(user.getId(), pageable);
        Page<ResourceResponse> responsePage = bookmarkPage.map(b -> resourceService.toResponse(b.getResource()));

        return PageResponse.from(responsePage);
    }

    @Transactional
    public HelpfulStatusResponse markHelpful(Long resourceId, String googleId) {
        User user = getUserByGoogleId(googleId);
        Resource resource = getResourceById(resourceId);

        if (!resourceHelpfulRepository.existsByUserIdAndResourceId(user.getId(), resource.getId())) {
            resourceHelpfulRepository.save(new ResourceHelpful(user, resource));
        }

        long count = resourceHelpfulRepository.countByResourceId(resource.getId());
        return new HelpfulStatusResponse(resourceId, count, true, "Marked as helpful");
    }

    @Transactional
    public HelpfulStatusResponse removeHelpful(Long resourceId, String googleId) {
        User user = getUserByGoogleId(googleId);
        Resource resource = getResourceById(resourceId);

        resourceHelpfulRepository.deleteByUserIdAndResourceId(user.getId(), resource.getId());

        long count = resourceHelpfulRepository.countByResourceId(resource.getId());
        return new HelpfulStatusResponse(resourceId, count, false, "Helpful mark removed");
    }

    @Transactional(readOnly = true)
    public HelpfulStatusResponse getHelpfulStatus(Long resourceId, String googleId) {
        User user = getUserByGoogleId(googleId);
        Resource resource = getResourceById(resourceId);

        boolean marked = resourceHelpfulRepository.existsByUserIdAndResourceId(user.getId(), resource.getId());
        long count = resourceHelpfulRepository.countByResourceId(resource.getId());

        return new HelpfulStatusResponse(resourceId, count, marked);
    }

    private User getUserByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User profile not found in database"));
    }

    private Resource getResourceById(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resource not found with id: " + resourceId));
    }
}
