package com.digitalhelper.controller;

import com.digitalhelper.dto.*;
import com.digitalhelper.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<AskResponse>> ask(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AskRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        AskResponse response = assistantService.ask(email, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Map<String, Object>> conversations = assistantService.getConversations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Map<String, Object> conversation = assistantService.getConversation(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        assistantService.deleteConversation(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted", null));
    }

    @PostMapping("/explain-image")
    public ResponseEntity<ApiResponse<ExplainImageResponse>> explainImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ExplainImageRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        ExplainImageResponse response = assistantService.explainImage(email, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/translate")
    public ResponseEntity<ApiResponse<TranslateResponse>> translate(
            @Valid @RequestBody TranslateRequest request) {
        TranslateResponse response = assistantService.translate(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ai-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAiStatus() {
        return ResponseEntity.ok(ApiResponse.success(assistantService.getAiStatus()));
    }

    @PostMapping("/ai-configure")
    public ResponseEntity<ApiResponse<Map<String, Object>>> configureAi(
            @RequestBody Map<String, String> config) {
        String provider = config.get("provider");
        String apiKey = config.get("apiKey");
        String model = config.get("model");
        String apiUrl = config.get("apiUrl");
        boolean ok = assistantService.configureAi(provider, apiKey, model, apiUrl);
        return ResponseEntity.ok(ApiResponse.success(ok ? "AI Provider configured and activated successfully" : "Reverted to Local Intelligent Engine", assistantService.getAiStatus()));
    }
}
