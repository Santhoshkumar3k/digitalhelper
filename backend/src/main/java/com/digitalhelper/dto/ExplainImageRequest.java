package com.digitalhelper.dto;

import jakarta.validation.constraints.NotBlank;

public class ExplainImageRequest {
    @NotBlank(message = "Image data is required (base64 encoded)")
    private String imageBase64;

    private String userNote;
    private String language = "en";

    public ExplainImageRequest() {}

    public ExplainImageRequest(String imageBase64, String userNote, String language) {
        this.imageBase64 = imageBase64;
        this.userNote = userNote;
        this.language = language;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getUserNote() {
        return userNote;
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
