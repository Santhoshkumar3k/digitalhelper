package com.digitalhelper.dto;

import java.util.List;
import java.util.Map;

public class TranslateResponse {
    private String originalText;
    private String translatedText;
    private String simplifiedText;
    private String targetLanguage;
    private String targetLanguageName;
    private Map<String, String> difficultTermsExplained;
    private String disclaimer = "Important: Translations and explanations are for informational assistance only and not legally certified.";

    public TranslateResponse() {}

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getSimplifiedText() {
        return simplifiedText;
    }

    public void setSimplifiedText(String simplifiedText) {
        this.simplifiedText = simplifiedText;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getTargetLanguageName() {
        return targetLanguageName;
    }

    public void setTargetLanguageName(String targetLanguageName) {
        this.targetLanguageName = targetLanguageName;
    }

    public Map<String, String> getDifficultTermsExplained() {
        return difficultTermsExplained;
    }

    public void setDifficultTermsExplained(Map<String, String> difficultTermsExplained) {
        this.difficultTermsExplained = difficultTermsExplained;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
