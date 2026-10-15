package com.project.seniorconnect.Controller;

import com.project.seniorconnect.DTO.CommentCreateRequest;
import com.project.seniorconnect.DTO.CommentResponse;
import com.project.seniorconnect.DTO.CommentUpvoteResponse;
import com.project.seniorconnect.Service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 1. Get Reddit-style threaded comments for a resource
    @GetMapping("/resources/{resourceId}/comments")
    public List<CommentResponse> getComments(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal OidcUser googleUser) {

        String googleId = googleUser != null ? googleUser.getSubject() : null;
        return commentService.getCommentsByResource(resourceId, googleId);
    }

    // 2. Post a comment or reply to a comment on a resource
    @PostMapping("/resources/{resourceId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
            @PathVariable Long resourceId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        return commentService.createComment(resourceId, request, googleUser.getSubject());
    }

    // 3. Upvote or toggle upvote on a comment
    @PostMapping("/comments/{commentId}/upvote")
    public CommentUpvoteResponse toggleUpvote(
            @PathVariable Long commentId,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        return commentService.toggleUpvote(commentId, googleUser.getSubject());
    }

    // 4. Delete a comment
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal OidcUser googleUser) {

        if (googleUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        commentService.deleteComment(commentId, googleUser.getSubject());
        return ResponseEntity.noContent().build();
    }
}
