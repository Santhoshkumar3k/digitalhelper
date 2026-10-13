package com.digitalhelper.dto;

import java.util.List;

public class ExplainImageResponse {
    private String errorTitle;
    private String plainExplanation;
    private List<String> possibleCauses;
    private List<String> stepByStepFix;
    private String safetyWarning;
    private boolean isSensitiveDataDetected;

    public ExplainImageResponse() {}

    public ExplainImageResponse(String errorTitle, String plainExplanation, List<String> possibleCauses, List<String> stepByStepFix, String safetyWarning, boolean isSensitiveDataDetected) {
        this.errorTitle = errorTitle;
        this.plainExplanation = plainExplanation;
        this.possibleCauses = possibleCauses;
        this.stepByStepFix = stepByStepFix;
        this.safetyWarning = safetyWarning;
        this.isSensitiveDataDetected = isSensitiveDataDetected;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }

    public String getPlainExplanation() {
        return plainExplanation;
    }

    public void setPlainExplanation(String plainExplanation) {
        this.plainExplanation = plainExplanation;
    }

    public List<String> getPossibleCauses() {
        return possibleCauses;
    }

    public void setPossibleCauses(List<String> possibleCauses) {
        this.possibleCauses = possibleCauses;
    }

    public List<String> getStepByStepFix() {
        return stepByStepFix;
    }

    public void setStepByStepFix(List<String> stepByStepFix) {
        this.stepByStepFix = stepByStepFix;
    }

    public String getSafetyWarning() {
        return safetyWarning;
    }

    public void setSafetyWarning(String safetyWarning) {
        this.safetyWarning = safetyWarning;
    }

    public boolean isSensitiveDataDetected() {
        return isSensitiveDataDetected;
    }

    public void setSensitiveDataDetected(boolean sensitiveDataDetected) {
        isSensitiveDataDetected = sensitiveDataDetected;
    }
}
