package com.digitalhelper.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 10)
    private String preferredLanguage = "en"; // en, hi, ta, te, kn, ml, bn

    @Column(nullable = false)
    private boolean highContrast = false;

    @Column(nullable = false)
    private boolean largeFont = false;

    @Column(nullable = false, length = 20)
    private String theme = "system"; // light, dark, system

    @Column(nullable = false)
    private boolean notifySecurityAlerts = true;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public UserPreference() {
        this.updatedAt = LocalDateTime.now();
    }

    public UserPreference(User user) {
        this.user = user;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
