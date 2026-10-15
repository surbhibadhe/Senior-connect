package com.project.seniorconnect.DTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CommentResponse(
        Long id,
        Long resourceId,
        Long userId,
        String userName,
        String userEmail,
        String userProfilePicture,
        String userRole,
        String userDepartment,
        String content,
        Long parentCommentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long upvotes,
        boolean userUpvoted,
        boolean canDelete,
        boolean isAuthor,
        boolean deleted,
        List<CommentResponse> replies
) {
    public CommentResponse withReplies(List<CommentResponse> newReplies) {
        return new CommentResponse(
                id,
                resourceId,
                userId,
                userName,
                userEmail,
                userProfilePicture,
                userRole,
                userDepartment,
                content,
                parentCommentId,
                createdAt,
                updatedAt,
                upvotes,
                userUpvoted,
                canDelete,
                isAuthor,
                deleted,
                newReplies != null ? newReplies : new ArrayList<>()
        );
    }
}
