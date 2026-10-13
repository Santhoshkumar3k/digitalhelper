package com.digitalhelper.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_answers")
public class SavedAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String originalQuestion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 60)
    private String category = "General"; // e.g., "Banking & Security", "Device Help", "WhatsApp & Social", "Forms"

    @Column(length = 150)
    private String tags;

    @Column(nullable = false)
    private boolean isFavorite = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SavedAnswer() {
        this.createdAt = LocalDateTime.now();
    }

    public SavedAnswer(User user, String title, String originalQuestion, String content, String category, String tags) {
        this.user = user;
        this.title = title;
        this.originalQuestion = originalQuestion;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalQuestion() {
        return originalQuestion;
    }

    public void setOriginalQuestion(String originalQuestion) {
        this.originalQuestion = originalQuestion;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
