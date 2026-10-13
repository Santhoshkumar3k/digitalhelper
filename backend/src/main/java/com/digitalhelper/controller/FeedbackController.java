package com.digitalhelper.controller;

import com.digitalhelper.dto.ApiResponse;
import com.digitalhelper.dto.FeedbackDto;
import com.digitalhelper.dto.FeedbackRequest;
import com.digitalhelper.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackDto>> submitFeedback(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FeedbackRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        FeedbackDto dto = feedbackService.submitFeedback(email, request);
        return ResponseEntity.ok(ApiResponse.success("Thank you! Your feedback helps improve DigitalHelper.", dto));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getMyFeedback(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<FeedbackDto> list = feedbackService.getUserFeedback(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
