package com.digitalhelper.dto;

import java.util.List;

public class AskResponse {
    private Long conversationId;
    private Long messageId;
    private String answer;
    private List<String> followUpSuggestions;
    private String category;
    private boolean isAiGenerated;

    public AskResponse() {}

    public AskResponse(Long conversationId, Long messageId, String answer, List<String> followUpSuggestions, String category, boolean isAiGenerated) {
        this.conversationId = conversationId;
        this.messageId = messageId;
        this.answer = answer;
        this.followUpSuggestions = followUpSuggestions;
        this.category = category;
        this.isAiGenerated = isAiGenerated;
    }

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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getFollowUpSuggestions() {
        return followUpSuggestions;
    }

    public void setFollowUpSuggestions(List<String> followUpSuggestions) {
        this.followUpSuggestions = followUpSuggestions;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAiGenerated() {
        return isAiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        isAiGenerated = aiGenerated;
    }
}
