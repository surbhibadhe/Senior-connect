package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.PageResponse;
import com.project.seniorconnect.DTO.ResourceResponse;
import com.project.seniorconnect.Service.EngagementService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final EngagementService engagementService;

    public BookmarkController(EngagementService engagementService) {
        this.engagementService = engagementService;
    }

    @GetMapping
    public PageResponse<ResourceResponse> getBookmarkedResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return engagementService.getBookmarkedResources(googleUser.getSubject(), page, size, sort);
    }
}
