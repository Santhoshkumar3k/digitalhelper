package com.digitalhelper.controller;

import com.digitalhelper.dto.ApiResponse;
import com.digitalhelper.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final AiService aiService;

    public HealthController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> status = Map.of(
                "status", "UP",
                "app", "DigitalHelper",
                "version", "1.0.0",
                "aiProvider", aiService.getProviderName(),
                "aiConfigured", aiService.isConfigured(),
                "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
