package com.digitalhelper.dto;

public class AdminStatsDto {
    private long totalUsers;
    private long totalConversations;
    private long totalMessages;
    private long totalScamChecks;
    private long totalSavedAnswers;
    private long totalFeedbacks;
    private double positiveFeedbackPercent;
    private double averageRating;
    private long highRiskScamsDetected;
    private String aiProvider;
    private String systemHealth;

    public AdminStatsDto() {}

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalConversations() {
        return totalConversations;
    }

    public void setTotalConversations(long totalConversations) {
        this.totalConversations = totalConversations;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public long getTotalScamChecks() {
        return totalScamChecks;
    }

    public void setTotalScamChecks(long totalScamChecks) {
        this.totalScamChecks = totalScamChecks;
    }

    public long getTotalSavedAnswers() {
        return totalSavedAnswers;
    }

    public void setTotalSavedAnswers(long totalSavedAnswers) {
        this.totalSavedAnswers = totalSavedAnswers;
    }

    public long getTotalFeedbacks() {
        return totalFeedbacks;
    }

    public void setTotalFeedbacks(long totalFeedbacks) {
        this.totalFeedbacks = totalFeedbacks;
    }

    public double getPositiveFeedbackPercent() {
        return positiveFeedbackPercent;
    }

    public void setPositiveFeedbackPercent(double positiveFeedbackPercent) {
        this.positiveFeedbackPercent = positiveFeedbackPercent;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getHighRiskScamsDetected() {
        return highRiskScamsDetected;
    }

    public void setHighRiskScamsDetected(long highRiskScamsDetected) {
        this.highRiskScamsDetected = highRiskScamsDetected;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public void setAiProvider(String aiProvider) {
        this.aiProvider = aiProvider;
    }

    public String getSystemHealth() {
        return systemHealth;
    }

    public void setSystemHealth(String systemHealth) {
        this.systemHealth = systemHealth;
    }
}
