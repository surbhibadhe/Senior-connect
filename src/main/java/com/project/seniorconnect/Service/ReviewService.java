package com.project.seniorconnect.Service;

import com.project.seniorconnect.DTO.ReviewCreateRequest;
import com.project.seniorconnect.DTO.ReviewResponse;
import com.project.seniorconnect.Entity.Review;
import com.project.seniorconnect.Entity.Role;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Repository.ReviewRepository;
import com.project.seniorconnect.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<ReviewResponse> getAllReviews(String googleId) {
        User currentUser = googleId != null ? userRepository.findByGoogleId(googleId).orElse(null) : null;

        List<Review> list = reviewRepository.findAllWithUserOrderByCreatedAtDesc();

        List<ReviewResponse> result = new ArrayList<>();
        for (Review r : list) {
            boolean canDelete = currentUser != null && (
                    currentUser.getRole() == Role.ADMIN ||
                    (r.getUser() != null && currentUser.getId().equals(r.getUser().getId()))
            );

            String authorPic = r.getUser() != null ? r.getUser().getProfilePicture() : null;

            result.add(new ReviewResponse(
                    r.getId(),
                    r.getUser() != null ? r.getUser().getId() : null,
                    r.getAuthorName(),
                    authorPic,
                    r.getAuthorSubtitle(),
                    r.getPerspectiveTag(),
                    r.getContent(),
                    r.getRating(),
                    r.getCreatedAt(),
                    canDelete
            ));
        }

        return result;
    }

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, String googleId) {
        if (googleId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in to write a review");
        }

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        int rating = (request.rating() != null && request.rating() >= 1 && request.rating() <= 5)
                ? request.rating()
                : 5;

        String subtitle = request.authorSubtitle() != null && !request.authorSubtitle().isBlank()
                ? request.authorSubtitle().trim()
                : (user.getDepartment() != null ? user.getDepartment() : user.getRole().name());

        String tag = request.perspectiveTag() != null && !request.perspectiveTag().isBlank()
                ? request.perspectiveTag().trim()
                : "Student";

        Review review = new Review(
                user,
                user.getName(),
                subtitle,
                tag,
                request.content().trim(),
                rating
        );

        Review saved = reviewRepository.save(review);

        return new ReviewResponse(
                saved.getId(),
                user.getId(),
                saved.getAuthorName(),
                user.getProfilePicture(),
                saved.getAuthorSubtitle(),
                saved.getPerspectiveTag(),
                saved.getContent(),
                saved.getRating(),
                saved.getCreatedAt(),
                true
        );
    }

    @Transactional
    public void deleteReview(Long id, String googleId) {
        if (googleId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        boolean canDelete = user.getRole() == Role.ADMIN ||
                (review.getUser() != null && review.getUser().getId().equals(user.getId()));

        if (!canDelete) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this review");
        }

        reviewRepository.delete(review);
    }
}
