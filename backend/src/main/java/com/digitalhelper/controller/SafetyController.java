package com.digitalhelper.controller;

import com.digitalhelper.dto.ApiResponse;
import com.digitalhelper.dto.ScamCheckRequest;
import com.digitalhelper.dto.ScamCheckResponse;
import com.digitalhelper.service.SafetyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/safety")
public class SafetyController {

    private final SafetyService safetyService;

    public SafetyController(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    @PostMapping("/check-message")
    public ResponseEntity<ApiResponse<ScamCheckResponse>> checkMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ScamCheckRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        ScamCheckResponse response = safetyService.checkMessage(email, request);
        return ResponseEntity.ok(ApiResponse.success("Safety analysis completed", response));
    }
}
