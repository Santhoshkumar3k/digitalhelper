package com.digitalhelper.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AskRequest {
    private Long conversationId;

    @NotBlank(message = "Question or prompt cannot be empty")
    private String prompt;

    private String language = "en";
    private String mode = "simple"; // "simple", "detailed", "step_by_step"
    private String previousContext;

    public AskRequest() {}

    public AskRequest(Long conversationId, String prompt, String language) {
        this.conversationId = conversationId;
        this.prompt = prompt;
        this.language = language;
    }

    public String getPreviousContext() {
        return previousContext;
    }

    public void setPreviousContext(String previousContext) {
        this.previousContext = previousContext;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
