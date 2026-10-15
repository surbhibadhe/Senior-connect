package com.project.seniorconnect.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 150)
    private String authorName;

    @Column(length = 200)
    private String authorSubtitle;

    @Column(length = 100)
    private String perspectiveTag;

    @Column(nullable = false, length = 2000, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int rating = 5;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Review() {
    }

    public Review(User user, String authorName, String authorSubtitle, String perspectiveTag, String content, int rating) {
        this.user = user;
        this.authorName = authorName;
        this.authorSubtitle = authorSubtitle;
        this.perspectiveTag = perspectiveTag;
        this.content = content;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorSubtitle() {
        return authorSubtitle;
    }

    public void setAuthorSubtitle(String authorSubtitle) {
        this.authorSubtitle = authorSubtitle;
    }

    public String getPerspectiveTag() {
        return perspectiveTag;
    }

    public void setPerspectiveTag(String perspectiveTag) {
        this.perspectiveTag = perspectiveTag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
