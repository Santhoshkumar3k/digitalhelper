package com.digitalhelper.dto;

import java.time.LocalDateTime;

public class FeedbackDto {
    private Long id;
    private String userEmail;
    private int rating;
    private boolean helpful;
    private String comments;
    private String feature;
    private LocalDateTime createdAt;

    public FeedbackDto() {}

    public FeedbackDto(Long id, String userEmail, int rating, boolean helpful, String comments, String feature, LocalDateTime createdAt) {
        this.id = id;
        this.userEmail = userEmail;
        this.rating = rating;
        this.helpful = helpful;
        this.comments = comments;
        this.feature = feature;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isHelpful() {
        return helpful;
    }

    public void setHelpful(boolean helpful) {
        this.helpful = helpful;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
