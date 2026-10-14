package com.project.seniorconnect.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "resource_helpful",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_resource_helpful_user_resource", columnNames = {"user_id", "resource_id"})
        },
        indexes = {
                @Index(name = "idx_helpful_user", columnList = "user_id"),
                @Index(name = "idx_helpful_resource", columnList = "resource_id")
        }
)
public class ResourceHelpful {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ResourceHelpful() {
    }

    public ResourceHelpful(User user, Resource resource) {
        this.user = user;
        this.resource = resource;
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

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
