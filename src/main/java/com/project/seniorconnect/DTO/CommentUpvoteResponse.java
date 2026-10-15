package com.project.seniorconnect.DTO;

public record CommentUpvoteResponse(
        Long commentId,
        long upvoteCount,
        boolean upvoted
) {
}
