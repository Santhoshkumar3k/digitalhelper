package com.digitalhelper.service;

import com.digitalhelper.dto.UserDto;
import com.digitalhelper.dto.UserPreferenceDto;
import com.digitalhelper.exception.ResourceNotFoundException;
import com.digitalhelper.model.User;
import com.digitalhelper.model.UserPreference;
import com.digitalhelper.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final ConversationRepository conversationRepository;
    private final SavedAnswerRepository savedAnswerRepository;
    private final FeedbackRepository feedbackRepository;
    private final ScamAnalysisLogRepository scamAnalysisLogRepository;

    public UserService(UserRepository userRepository,
                       UserPreferenceRepository preferenceRepository,
                       ConversationRepository conversationRepository,
                       SavedAnswerRepository savedAnswerRepository,
                       FeedbackRepository feedbackRepository,
                       ScamAnalysisLogRepository scamAnalysisLogRepository) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.conversationRepository = conversationRepository;
        this.savedAnswerRepository = savedAnswerRepository;
        this.feedbackRepository = feedbackRepository;
        this.scamAnalysisLogRepository = scamAnalysisLogRepository;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public UserDto getUserDto(String email) {
        User user = getUserByEmail(email);
        return new UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getCreatedAt());
    }

    public UserPreferenceDto getPreferences(String email) {
        User user = getUserByEmail(email);
        UserPreference pref = preferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> preferenceRepository.save(new UserPreference(user)));

        return new UserPreferenceDto(
                pref.getPreferredLanguage(),
                pref.isHighContrast(),
                pref.isLargeFont(),
                pref.getTheme(),
                pref.isNotifySecurityAlerts()
        );
    }

    @Transactional
    public UserPreferenceDto updatePreferences(String email, UserPreferenceDto dto) {
        User user = getUserByEmail(email);
        UserPreference pref = preferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> new UserPreference(user));

        if (dto.getPreferredLanguage() != null) pref.setPreferredLanguage(dto.getPreferredLanguage());
        pref.setHighContrast(dto.isHighContrast());
        pref.setLargeFont(dto.isLargeFont());
        if (dto.getTheme() != null) pref.setTheme(dto.getTheme());
        pref.setNotifySecurityAlerts(dto.isNotifySecurityAlerts());

        pref = preferenceRepository.save(pref);

        return new UserPreferenceDto(
                pref.getPreferredLanguage(),
                pref.isHighContrast(),
                pref.isLargeFont(),
                pref.getTheme(),
                pref.isNotifySecurityAlerts()
        );
    }

    @Transactional
    public void deleteUserData(String email) {
        User user = getUserByEmail(email);
        Long userId = user.getId();

        savedAnswerRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(savedAnswerRepository::delete);

        conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .forEach(conversationRepository::delete);

        scamAnalysisLogRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(scamAnalysisLogRepository::delete);

        feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(feedbackRepository::delete);

        preferenceRepository.findByUserId(userId)
                .ifPresent(preferenceRepository::delete);

        userRepository.delete(user);
    }
}
