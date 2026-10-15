package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.ReviewCreateRequest;
import com.project.seniorconnect.DTO.ReviewResponse;
import com.project.seniorconnect.Service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 1. Get all student reviews (Public - anyone can view without sign-in)
    @GetMapping
    public List<ReviewResponse> getAllReviews(
            @AuthenticationPrincipal OidcUser googleUser) {
        String googleId = googleUser != null ? googleUser.getSubject() : null;
        return reviewService.getAllReviews(googleId);
    }

    // 2. Submit a review (Sign-in required)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal OidcUser googleUser) {
        if (googleUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in to write a review");
        }
        return reviewService.createReview(request, googleUser.getSubject());
    }

    // 3. Delete a review (Author or Admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser googleUser) {
        if (googleUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        reviewService.deleteReview(id, googleUser.getSubject());
        return ResponseEntity.noContent().build();
    }
}
