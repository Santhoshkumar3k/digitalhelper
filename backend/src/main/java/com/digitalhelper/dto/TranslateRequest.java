package com.digitalhelper.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class TranslateRequest {
    @NotBlank(message = "Text cannot be empty")
    private String text;

    private String sourceLanguage = "auto";
    private String targetLanguage = "ta"; // default to Indian language e.g. Tamil or Hindi
    private String mode = "translate"; // "translate", "simplify", "explain_terms"
    private String depth = "simple"; // "concise", "detailed"

    public TranslateRequest() {}

    public TranslateRequest(String text, String targetLanguage, String mode) {
        this.text = text;
        this.targetLanguage = targetLanguage;
        this.mode = mode;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDepth() {
        return depth;
    }

    public void setDepth(String depth) {
        this.depth = depth;
    }
}
