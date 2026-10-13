package com.digitalhelper.dto;

import java.util.List;
import java.util.Map;

public class DocumentExplainResponse {
    private String formTitle;
    private String purpose;
    private List<String> requiredDocumentsChecklist;
    private Map<String, String> fieldExplanations;
    private List<String> commonRejectionReasons;
    private List<String> stepByStepGuide;
    private String disclaimer = "Notice: This provides simplified educational guidance. Official government and institutional requirements may change; always review the portal instructions.";

    public DocumentExplainResponse() {}

    public String getFormTitle() {
        return formTitle;
    }

    public void setFormTitle(String formTitle) {
        this.formTitle = formTitle;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public List<String> getRequiredDocumentsChecklist() {
        return requiredDocumentsChecklist;
    }

    public void setRequiredDocumentsChecklist(List<String> requiredDocumentsChecklist) {
        this.requiredDocumentsChecklist = requiredDocumentsChecklist;
    }

    public Map<String, String> getFieldExplanations() {
        return fieldExplanations;
    }

    public void setFieldExplanations(Map<String, String> fieldExplanations) {
        this.fieldExplanations = fieldExplanations;
    }

    public List<String> getCommonRejectionReasons() {
        return commonRejectionReasons;
    }

    public void setCommonRejectionReasons(List<String> commonRejectionReasons) {
        this.commonRejectionReasons = commonRejectionReasons;
    }

    public List<String> getStepByStepGuide() {
        return stepByStepGuide;
    }

    public void setStepByStepGuide(List<String> stepByStepGuide) {
        this.stepByStepGuide = stepByStepGuide;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
