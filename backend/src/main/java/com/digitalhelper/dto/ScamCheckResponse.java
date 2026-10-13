package com.digitalhelper.dto;

import java.util.List;

public class ScamCheckResponse {
    private String riskLevel; // "LOW_CONCERN", "SUSPICIOUS", "HIGH_RISK"
    private int riskScore; // 0 to 100
    private String summary;
    private List<String> warningFlags; // e.g. "False sense of urgency", "Impersonates official bank", "Suspicious short URL"
    private List<String> safeNextSteps; // e.g. "Do not click link", "Never provide OTP", "Verify via official helpline"
    private String officialAlternative; // Where to verify safely
    private String safetyDisclaimer = "Note: DigitalHelper analyzes patterns to alert you. No tool can guarantee 100% detection. When in doubt, never send money, OTPs, or passwords.";

    public ScamCheckResponse() {}

    public ScamCheckResponse(String riskLevel, int riskScore, String summary, List<String> warningFlags, List<String> safeNextSteps, String officialAlternative) {
        this.riskLevel = riskLevel;
        this.riskScore = riskScore;
        this.summary = summary;
        this.warningFlags = warningFlags;
        this.safeNextSteps = safeNextSteps;
        this.officialAlternative = officialAlternative;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getWarningFlags() {
        return warningFlags;
    }

    public void setWarningFlags(List<String> warningFlags) {
        this.warningFlags = warningFlags;
    }

    public List<String> getSafeNextSteps() {
        return safeNextSteps;
    }

    public void setSafeNextSteps(List<String> safeNextSteps) {
        this.safeNextSteps = safeNextSteps;
    }

    public String getOfficialAlternative() {
        return officialAlternative;
    }

    public void setOfficialAlternative(String officialAlternative) {
        this.officialAlternative = officialAlternative;
    }

    public String getSafetyDisclaimer() {
        return safetyDisclaimer;
    }

    public void setSafetyDisclaimer(String safetyDisclaimer) {
        this.safetyDisclaimer = safetyDisclaimer;
    }
}
