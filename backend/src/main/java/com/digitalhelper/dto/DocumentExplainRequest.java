package com.digitalhelper.dto;

import java.util.List;

public class DocumentExplainRequest {
    private String formType; // e.g. "Passport Application", "Aadhaar Card Update", "Bank KYC", "PAN Card Correction", "Custom"
    private String instructionsOrTextFields;
    private String query;

    public DocumentExplainRequest() {}

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public String getInstructionsOrTextFields() {
        return instructionsOrTextFields;
    }

    public void setInstructionsOrTextFields(String instructionsOrTextFields) {
        this.instructionsOrTextFields = instructionsOrTextFields;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
