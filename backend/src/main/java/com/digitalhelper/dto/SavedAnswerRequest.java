package com.digitalhelper.dto;

import jakarta.validation.constraints.NotBlank;

public class SavedAnswerRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String originalQuestion;

    @NotBlank(message = "Content is required")
    private String content;

    private String category = "General";
    private String tags;

    public SavedAnswerRequest() {}

    public SavedAnswerRequest(String title, String originalQuestion, String content, String category, String tags) {
        this.title = title;
        this.originalQuestion = originalQuestion;
        this.content = content;
        this.category = category;
        this.tags = tags;
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
}
