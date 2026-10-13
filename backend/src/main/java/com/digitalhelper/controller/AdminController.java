package com.digitalhelper.controller;

import com.digitalhelper.dto.AdminStatsDto;
import com.digitalhelper.dto.ApiResponse;
import com.digitalhelper.dto.FeedbackDto;
import com.digitalhelper.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final FeedbackService feedbackService;

    public AdminController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getStats() {
        AdminStatsDto stats = feedbackService.getAdminStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getAllFeedback() {
        List<FeedbackDto> feedbackList = feedbackService.getAllFeedbackForAdmin();
        return ResponseEntity.ok(ApiResponse.success(feedbackList));
    }
}
