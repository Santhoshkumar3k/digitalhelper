package com.digitalhelper.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ScamCheckRequest {
    @NotBlank(message = "Message text or suspicious content is required")
    private String messageText;

    private String sourceUrl; // optional link to evaluate

    public ScamCheckRequest() {}

    public ScamCheckRequest(String messageText, String sourceUrl) {
        this.messageText = messageText;
        this.sourceUrl = sourceUrl;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}
