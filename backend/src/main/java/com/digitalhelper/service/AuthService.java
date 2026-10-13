package com.digitalhelper.service;

import com.digitalhelper.config.JwtTokenProvider;
import com.digitalhelper.dto.AuthRequest;
import com.digitalhelper.dto.AuthResponse;
import com.digitalhelper.dto.RegisterRequest;
import com.digitalhelper.dto.UserDto;
import com.digitalhelper.exception.BadRequestException;
import com.digitalhelper.exception.UnauthorizedException;
import com.digitalhelper.model.User;
import com.digitalhelper.model.UserPreference;
import com.digitalhelper.repository.UserPreferenceRepository;
import com.digitalhelper.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       UserPreferenceRepository preferenceRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User(
                email,
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                "ROLE_USER"
        );
        user = userRepository.save(user);

        UserPreference preference = new UserPreference(user);
        preferenceRepository.save(preference);

        String token = tokenProvider.createToken(user.getEmail(), user.getRole(), user.getId());
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getCreatedAt());

        return new AuthResponse(token, userDto);
    }

    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = tokenProvider.createToken(user.getEmail(), user.getRole(), user.getId());
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getCreatedAt());

        return new AuthResponse(token, userDto);
    }
}
