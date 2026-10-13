package com.digitalhelper.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scam_analysis_logs")
public class ScamAnalysisLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 500)
    private String inputSnippet;

    @Column(nullable = false)
    private int riskScore; // 0 - 100

    @Column(nullable = false, length = 30)
    private String riskLevel; // LOW_CONCERN, SUSPICIOUS, HIGH_RISK

    @Column(columnDefinition = "TEXT")
    private String detectedFlags;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ScamAnalysisLog() {
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
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

    public String getInputSnippet() {
        return inputSnippet;
    }

    public void setInputSnippet(String inputSnippet) {
        this.inputSnippet = inputSnippet;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDetectedFlags() {
        return detectedFlags;
    }

    public void setDetectedFlags(String detectedFlags) {
        this.detectedFlags = detectedFlags;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
