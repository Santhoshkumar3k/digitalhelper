package com.digitalhelper.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class FeedbackRequest {
    private Long conversationId;
    private Long messageId;

    @Min(1)
    @Max(5)
    private int rating = 5;

    private boolean helpful = true;
    private String comments;
    private String feature = "chat";

    public FeedbackRequest() {}

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
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
}
