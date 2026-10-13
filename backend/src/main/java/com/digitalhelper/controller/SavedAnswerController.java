package com.digitalhelper.controller;

import com.digitalhelper.dto.ApiResponse;
import com.digitalhelper.dto.SavedAnswerDto;
import com.digitalhelper.dto.SavedAnswerRequest;
import com.digitalhelper.service.SavedAnswerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/saved-answers")
public class SavedAnswerController {

    private final SavedAnswerService savedAnswerService;

    public SavedAnswerController(SavedAnswerService savedAnswerService) {
        this.savedAnswerService = savedAnswerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavedAnswerDto>>> getSavedAnswers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String query) {
        List<SavedAnswerDto> answers = savedAnswerService.getSavedAnswers(userDetails.getUsername(), category, query);
        return ResponseEntity.ok(ApiResponse.success(answers));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SavedAnswerDto>> saveAnswer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SavedAnswerRequest request) {
        SavedAnswerDto saved = savedAnswerService.saveAnswer(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Answer saved to your bookmarks", saved));
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<SavedAnswerDto>> toggleFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        SavedAnswerDto updated = savedAnswerService.toggleFavorite(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSavedAnswer(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        savedAnswerService.deleteSavedAnswer(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Bookmark removed", null));
    }
}
