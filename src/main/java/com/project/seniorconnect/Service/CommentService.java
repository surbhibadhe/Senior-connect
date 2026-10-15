package com.project.seniorconnect.Service;

import com.project.seniorconnect.DTO.CommentCreateRequest;
import com.project.seniorconnect.DTO.CommentResponse;
import com.project.seniorconnect.DTO.CommentUpvoteResponse;
import com.project.seniorconnect.Entity.Comment;
import com.project.seniorconnect.Entity.CommentUpvote;
import com.project.seniorconnect.Entity.Resource;
import com.project.seniorconnect.Entity.Role;
import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Repository.CommentRepository;
import com.project.seniorconnect.Repository.CommentUpvoteRepository;
import com.project.seniorconnect.Repository.ResourceRepository;
import com.project.seniorconnect.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentUpvoteRepository commentUpvoteRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            CommentUpvoteRepository commentUpvoteRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.commentUpvoteRepository = commentUpvoteRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByResource(Long resourceId, String googleId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        User currentUser = googleId != null ? userRepository.findByGoogleId(googleId).orElse(null) : null;

        List<Comment> allComments = commentRepository.findByResourceIdWithUserAndParent(resourceId);

        // Fetch aggregated upvote counts
        List<Object[]> rawUpvoteCounts = commentUpvoteRepository.countUpvotesByResourceId(resourceId);
        Map<Long, Long> upvoteCounts = new HashMap<>();
        for (Object[] row : rawUpvoteCounts) {
            Long cId = (Long) row[0];
            Long count = (Long) row[1];
            upvoteCounts.put(cId, count);
        }

        // Fetch current user's upvoted comment IDs
        Set<Long> userUpvotedIds = (currentUser != null)
                ? commentUpvoteRepository.findUpvotedCommentIdsByUserAndResource(currentUser.getId(), resourceId)
                : Collections.emptySet();

        Long authorUserId = resource.getUploadedBy() != null ? resource.getUploadedBy().getId() : null;

        // Group into a tree structure
        Map<Long, List<CommentResponse>> childrenMap = new HashMap<>();
        List<CommentResponse> rootComments = new ArrayList<>();

        for (Comment c : allComments) {
            boolean canDelete = currentUser != null && !c.isDeleted() &&
                    (currentUser.getRole() == Role.ADMIN || currentUser.getId().equals(c.getUser().getId()));
            boolean isAuthor = authorUserId != null && authorUserId.equals(c.getUser().getId());
            boolean isUserUpvoted = userUpvotedIds.contains(c.getId());
            long upvotes = upvoteCounts.getOrDefault(c.getId(), 0L);

            String displayedContent = c.isDeleted() ? "[deleted]" : c.getContent();
            String userName = c.isDeleted() ? "[deleted]" : c.getUser().getName();
            String userEmail = c.isDeleted() ? "" : c.getUser().getEmail();
            String profilePic = c.isDeleted() ? null : c.getUser().getProfilePicture();
            String userRole = c.isDeleted() ? "" : (c.getUser().getRole() != null ? c.getUser().getRole().name() : "");
            String userDept = c.isDeleted() ? null : c.getUser().getDepartment();

            Long parentId = c.getParentComment() != null ? c.getParentComment().getId() : null;

            CommentResponse resp = new CommentResponse(
                    c.getId(),
                    resourceId,
                    c.isDeleted() ? null : c.getUser().getId(),
                    userName,
                    userEmail,
                    profilePic,
                    userRole,
                    userDept,
                    displayedContent,
                    parentId,
                    c.getCreatedAt(),
                    c.getUpdatedAt(),
                    upvotes,
                    isUserUpvoted,
                    canDelete,
                    isAuthor,
                    c.isDeleted(),
                    new ArrayList<>()
            );

            if (parentId == null) {
                rootComments.add(resp);
            } else {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(resp);
            }
        }

        // Attach nested replies recursively
        return rootComments.stream()
                .map(root -> attachReplies(root, childrenMap))
                .toList();
    }

    private CommentResponse attachReplies(CommentResponse parent, Map<Long, List<CommentResponse>> childrenMap) {
        List<CommentResponse> directChildren = childrenMap.getOrDefault(parent.id(), Collections.emptyList());
        List<CommentResponse> nestedChildren = directChildren.stream()
                .map(child -> attachReplies(child, childrenMap))
                .toList();
        return parent.withReplies(nestedChildren);
    }

    @Transactional
    public CommentResponse createComment(Long resourceId, CommentCreateRequest request, String googleId) {
        if (googleId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        if (request.content() == null || request.content().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment content cannot be blank");
        }

        Comment parent = null;
        if (request.parentCommentId() != null) {
            parent = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found"));

            if (!parent.getResource().getId().equals(resourceId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment does not belong to this resource");
            }
        }

        Comment comment = new Comment(resource, user, parent, request.content().trim());
        Comment saved = commentRepository.save(comment);

        boolean isAuthor = resource.getUploadedBy() != null && resource.getUploadedBy().getId().equals(user.getId());

        return new CommentResponse(
                saved.getId(),
                resourceId,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfilePicture(),
                user.getRole() != null ? user.getRole().name() : "",
                user.getDepartment(),
                saved.getContent(),
                parent != null ? parent.getId() : null,
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                0L,
                false,
                true,
                isAuthor,
                false,
                new ArrayList<>()
        );
    }

    @Transactional
    public CommentUpvoteResponse toggleUpvote(Long commentId, String googleId) {
        if (googleId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (comment.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot upvote a deleted comment");
        }

        Optional<CommentUpvote> existing = commentUpvoteRepository.findByCommentIdAndUserId(commentId, user.getId());
        boolean nowUpvoted;

        if (existing.isPresent()) {
            commentUpvoteRepository.delete(existing.get());
            nowUpvoted = false;
        } else {
            commentUpvoteRepository.save(new CommentUpvote(comment, user));
            nowUpvoted = true;
        }

        long count = commentUpvoteRepository.countByCommentId(commentId);
        return new CommentUpvoteResponse(commentId, count, nowUpvoted);
    }

    @Transactional
    public void deleteComment(Long commentId, String googleId) {
        if (googleId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        boolean canDelete = user.getRole() == Role.ADMIN || comment.getUser().getId().equals(user.getId());
        if (!canDelete) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this comment");
        }

        boolean hasReplies = commentRepository.existsByParentCommentId(commentId);
        if (hasReplies) {
            // Soft delete to preserve child conversation hierarchy like Reddit
            comment.setDeleted(true);
            comment.setContent("[deleted]");
            commentRepository.save(comment);
        } else {
            // Hard delete if no child replies
            commentUpvoteRepository.deleteByCommentId(commentId);
            commentRepository.delete(comment);
        }
    }

    @Transactional(readOnly = true)
    public long getCommentCount(Long resourceId) {
        return commentRepository.countByResourceIdAndDeletedFalse(resourceId);
    }
}
