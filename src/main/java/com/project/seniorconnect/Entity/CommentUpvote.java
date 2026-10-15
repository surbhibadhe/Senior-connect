package com.project.seniorconnect.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "comment_upvotes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_comment_user_upvote",
                        columnNames = {"comment_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_comment_upvotes_comment", columnList = "comment_id"),
                @Index(name = "idx_comment_upvotes_user", columnList = "user_id")
        }
)
public class CommentUpvote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CommentUpvote() {
    }

    public CommentUpvote(Comment comment, User user) {
        this.comment = comment;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
