package com.digitalhelper.controller;

import com.digitalhelper.dto.ApiResponse;
import com.digitalhelper.dto.UserDto;
import com.digitalhelper.dto.UserPreferenceDto;
import com.digitalhelper.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDto userDto = userService.getUserDto(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }

    @GetMapping("/me/preferences")
    public ResponseEntity<ApiResponse<UserPreferenceDto>> getPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        UserPreferenceDto prefs = userService.getPreferences(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(prefs));
    }

    @PatchMapping("/me/preferences")
    public ResponseEntity<ApiResponse<UserPreferenceDto>> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserPreferenceDto request) {
        UserPreferenceDto updated = userService.updatePreferences(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Preferences updated", updated));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUserData(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Your account and associated data have been permanently removed", null));
    }
}
