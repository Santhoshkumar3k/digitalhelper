package com.digitalhelper.dto;

import java.time.LocalDateTime;

public class SavedAnswerDto {
    private Long id;
    private String title;
    private String originalQuestion;
    private String content;
    private String category;
    private String tags;
    private boolean isFavorite;
    private LocalDateTime createdAt;

    public SavedAnswerDto() {}

    public SavedAnswerDto(Long id, String title, String originalQuestion, String content, String category, String tags, boolean isFavorite, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.originalQuestion = originalQuestion;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
