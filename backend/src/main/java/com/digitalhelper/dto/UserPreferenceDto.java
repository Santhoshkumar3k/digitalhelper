package com.digitalhelper.dto;

public class UserPreferenceDto {
    private String preferredLanguage;
    private boolean highContrast;
    private boolean largeFont;
    private String theme;
    private boolean notifySecurityAlerts;

    public UserPreferenceDto() {}

    public UserPreferenceDto(String preferredLanguage, boolean highContrast, boolean largeFont, String theme, boolean notifySecurityAlerts) {
        this.preferredLanguage = preferredLanguage;
        this.highContrast = highContrast;
        this.largeFont = largeFont;
        this.theme = theme;
        this.notifySecurityAlerts = notifySecurityAlerts;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public boolean isHighContrast() {
        return highContrast;
    }

    public void setHighContrast(boolean highContrast) {
        this.highContrast = highContrast;
    }

    public boolean isLargeFont() {
        return largeFont;
    }

    public void setLargeFont(boolean largeFont) {
        this.largeFont = largeFont;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public boolean isNotifySecurityAlerts() {
        return notifySecurityAlerts;
    }

    public void setNotifySecurityAlerts(boolean notifySecurityAlerts) {
        this.notifySecurityAlerts = notifySecurityAlerts;
    }
}
