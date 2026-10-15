package com.project.seniorconnect.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank(message = "Comment cannot be blank")
        @Size(max = 3000, message = "Comment must not exceed 3000 characters")
        String content,

        Long parentCommentId
) {
}
