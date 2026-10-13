package com.digitalhelper;

import com.digitalhelper.dto.ScamCheckRequest;
import com.digitalhelper.dto.ScamCheckResponse;
import com.digitalhelper.repository.ScamAnalysisLogRepository;
import com.digitalhelper.repository.UserRepository;
import com.digitalhelper.service.AiServiceImpl;
import com.digitalhelper.service.SafetyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class SafetyServiceTest {

    private SafetyService safetyService;

    @BeforeEach
    void setUp() {
        AiServiceImpl aiService = new AiServiceImpl("mock", "", "", "", new ObjectMapper());
        ScamAnalysisLogRepository logRepo = Mockito.mock(ScamAnalysisLogRepository.class);
        UserRepository userRepo = Mockito.mock(UserRepository.class);
        safetyService = new SafetyService(aiService, logRepo, userRepo);
    }

    @Test
    void testUrgentScamMessageDetection() {
        ScamCheckRequest req = new ScamCheckRequest(
                "URGENT: Your SBI Bank account is blocked. Click bit.ly/sbi-verify immediately and share OTP to unfreeze.",
                "http://bit.ly/sbi-verify"
        );

        ScamCheckResponse response = safetyService.checkMessage(null, req);

        assertNotNull(response);
        assertEquals("HIGH_RISK", response.getRiskLevel());
        assertTrue(response.getRiskScore() >= 60);
        assertFalse(response.getWarningFlags().isEmpty());
        assertFalse(response.getSafeNextSteps().isEmpty());
    }

    @Test
    void testNormalMessageEvaluation() {
        ScamCheckRequest req = new ScamCheckRequest(
                "Hi Mom, I will be home by 6 PM for dinner today.",
                null
        );

        ScamCheckResponse response = safetyService.checkMessage(null, req);

        assertNotNull(response);
        assertEquals("LOW_CONCERN", response.getRiskLevel());
        assertTrue(response.getRiskScore() < 30);
    }
}
