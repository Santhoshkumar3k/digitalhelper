package com.digitalhelper.service;

import com.digitalhelper.dto.AdminStatsDto;
import com.digitalhelper.dto.FeedbackDto;
import com.digitalhelper.dto.FeedbackRequest;
import com.digitalhelper.model.Feedback;
import com.digitalhelper.model.User;
import com.digitalhelper.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ScamAnalysisLogRepository scamLogRepository;
    private final SavedAnswerRepository savedAnswerRepository;
    private final AiService aiService;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           UserRepository userRepository,
                           ConversationRepository conversationRepository,
                           MessageRepository messageRepository,
                           ScamAnalysisLogRepository scamLogRepository,
                           SavedAnswerRepository savedAnswerRepository,
                           AiService aiService) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.scamLogRepository = scamLogRepository;
        this.savedAnswerRepository = savedAnswerRepository;
        this.aiService = aiService;
    }

    @Transactional
    public FeedbackDto submitFeedback(String userEmail, FeedbackRequest request) {
        Feedback feedback = new Feedback();
        if (userEmail != null && !userEmail.isBlank()) {
            Optional<User> u = userRepository.findByEmail(userEmail);
            u.ifPresent(feedback::setUser);
        }

        feedback.setConversationId(request.getConversationId());
        feedback.setMessageId(request.getMessageId());
        feedback.setRating(request.getRating());
        feedback.setHelpful(request.isHelpful());
        feedback.setComments(request.getComments());
        feedback.setFeature(request.getFeature() != null ? request.getFeature() : "chat");

        feedback = feedbackRepository.save(feedback);
        return toDto(feedback);
    }

    public List<FeedbackDto> getUserFeedback(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return List.of();
        return feedbackRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<FeedbackDto> getAllFeedbackForAdmin() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public AdminStatsDto getAdminStats() {
        AdminStatsDto stats = new AdminStatsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalConversations(conversationRepository.count());
        stats.setTotalMessages(messageRepository.count());
        stats.setTotalScamChecks(scamLogRepository.count());
        stats.setTotalSavedAnswers(savedAnswerRepository.count());

        long helpfulCount = feedbackRepository.countByHelpfulTrue();
        long unhelpfulCount = feedbackRepository.countByHelpfulFalse();
        long totalFeedback = helpfulCount + unhelpfulCount;

        stats.setTotalFeedbacks(totalFeedback);
        stats.setPositiveFeedbackPercent(totalFeedback > 0 ? (helpfulCount * 100.0 / totalFeedback) : 100.0);

        Double avg = feedbackRepository.getAverageRating();
        stats.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 5.0);

        stats.setHighRiskScamsDetected(scamLogRepository.countByRiskLevel("HIGH_RISK"));
        stats.setAiProvider(aiService.getProviderName());
        stats.setSystemHealth("OPERATIONAL — All services normal");

        return stats;
    }

    private FeedbackDto toDto(Feedback f) {
        return new FeedbackDto(
                f.getId(),
                f.getUser() != null ? f.getUser().getEmail() : "Anonymous User",
                f.getRating(),
                f.isHelpful(),
                f.getComments(),
                f.getFeature(),
                f.getCreatedAt()
        );
    }
}
