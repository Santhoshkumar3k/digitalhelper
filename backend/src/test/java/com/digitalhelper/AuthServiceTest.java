package com.digitalhelper;

import com.digitalhelper.config.JwtTokenProvider;
import com.digitalhelper.dto.AuthRequest;
import com.digitalhelper.dto.AuthResponse;
import com.digitalhelper.dto.RegisterRequest;
import com.digitalhelper.exception.BadRequestException;
import com.digitalhelper.exception.UnauthorizedException;
import com.digitalhelper.model.User;
import com.digitalhelper.model.UserPreference;
import com.digitalhelper.repository.UserPreferenceRepository;
import com.digitalhelper.repository.UserRepository;
import com.digitalhelper.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AuthServiceTest {

    private UserRepository userRepository;
    private UserPreferenceRepository preferenceRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        preferenceRepository = Mockito.mock(UserPreferenceRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        tokenProvider = Mockito.mock(JwtTokenProvider.class);

        authService = new AuthService(userRepository, preferenceRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest("Test User", "test@example.com", "Secret@123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("hashedPassword");

        User savedUser = new User("test@example.com", "hashedPassword", "Test User", "ROLE_USER");
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(preferenceRepository.save(any(UserPreference.class))).thenReturn(new UserPreference(savedUser));
        when(tokenProvider.createToken("test@example.com", "ROLE_USER", 10L)).thenReturn("mockJwtToken");

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("mockJwtToken", resp.getToken());
        assertEquals("test@example.com", resp.getUser().getEmail());
    }

    @Test
    void testRegisterDuplicateEmailThrows() {
        RegisterRequest req = new RegisterRequest("Test User", "duplicate@example.com", "Secret@123");
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void testLoginInvalidPasswordThrows() {
        AuthRequest req = new AuthRequest("user@example.com", "wrongPass");
        User user = new User("user@example.com", "realHash", "User", "ROLE_USER");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "realHash")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }
}
