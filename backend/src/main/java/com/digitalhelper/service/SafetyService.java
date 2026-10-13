package com.digitalhelper.service;

import com.digitalhelper.dto.ScamCheckRequest;
import com.digitalhelper.dto.ScamCheckResponse;
import com.digitalhelper.model.ScamAnalysisLog;
import com.digitalhelper.model.User;
import com.digitalhelper.repository.ScamAnalysisLogRepository;
import com.digitalhelper.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SafetyService {

    private final AiService aiService;
    private final ScamAnalysisLogRepository logRepository;
    private final UserRepository userRepository;

    public SafetyService(AiService aiService,
                         ScamAnalysisLogRepository logRepository,
                         UserRepository userRepository) {
        this.aiService = aiService;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
    }

    public ScamCheckResponse checkMessage(String userEmail, ScamCheckRequest request) {
        ScamCheckResponse response = aiService.checkScam(request);

        // Record log if user is logged in
        if (userEmail != null && !userEmail.isBlank()) {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                ScamAnalysisLog log = new ScamAnalysisLog();
                log.setUser(userOpt.get());
                String snippet = request.getMessageText();
                log.setInputSnippet(snippet.length() > 200 ? snippet.substring(0, 197) + "..." : snippet);
                log.setRiskScore(response.getRiskScore());
                log.setRiskLevel(response.getRiskLevel());
                log.setDetectedFlags(String.join("; ", response.getWarningFlags()));
                log.setSummary(response.getSummary());
                logRepository.save(log);
            }
        }

        return response;
    }
}
