package com.social.backend.auth.service;

import com.social.backend.user.entity.User;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.backend.auth.dto.AuthTokensResponse;
import com.social.backend.auth.dto.LoginRequest;
import com.social.backend.auth.dto.RegisterRequest;
import com.social.backend.auth.entity.Session;
import com.social.backend.auth.exception.EmailAlreadyTakenException;
import com.social.backend.auth.exception.InvalidRefreshTokenException;
import com.social.backend.auth.exception.InvalidCredentialsException;
import com.social.backend.auth.exception.UsernameAlreadyTakenException;
import com.social.backend.auth.service.RefreshTokenService.IssuedRefreshToken;
import com.social.backend.security.jwt.JwtService;
import com.social.backend.security.jwt.JwtService.AccessToken;
import com.social.backend.user.model.UserRole;
import com.social.backend.user.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            SessionService sessionService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthTokensResponse register(RegisterRequest registerRequest, String userAgent, String clientIp) {
        // normalize email and username to lower case
        String email = registerRequest.email().toLowerCase().trim();
        String username = registerRequest.username().toLowerCase().trim();

        // check if email or username already exists
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyTakenException();
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new UsernameAlreadyTakenException();
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(registerRequest.password()))
                .userRole(UserRole.USER)
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .phoneNumber(registerRequest.phoneNumber())
                .build();

        userRepository.save(user);

        return issueTokens(user, userAgent, clientIp);
    }

    @Transactional
    public AuthTokensResponse login(LoginRequest loginRequest, String userAgent, String clientIp) {
        User user = findByIdentifier(loginRequest.identifier())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return issueTokens(user, userAgent, clientIp);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthTokensResponse refresh(String rawRefreshToken) {
        IssuedRefreshToken newRefreshToken = refreshTokenService.rotate(rawRefreshToken);
        AccessToken newAccessToken = jwtService
                .generateAccessToken(newRefreshToken.refreshToken().getSession().getUser(),
                        newRefreshToken.refreshToken().getSession());

        return AuthTokensResponse.bearer(
                newAccessToken.accessToken(),
                newRefreshToken.rawRefreshToken());
    }

    private AuthTokensResponse issueTokens(User user, String userAgent, String clientIp) {
        Session session = sessionService.createSession(user, userAgent, clientIp);

        IssuedRefreshToken refresh = refreshTokenService.issueRefreshToken(session, user);

        AccessToken access = jwtService.generateAccessToken(user, session);

        return AuthTokensResponse.bearer(
                access.accessToken(),
                refresh.rawRefreshToken());
    }

    @Transactional
    public void logout(String rawToken) {
        refreshTokenService.revokeByRawToken(rawToken);
    }

    private Optional<User> findByIdentifier(String identifier) {
        String value = identifier.trim().toLowerCase();

        if (value.contains("@")) {
            return userRepository.findByEmail(value)
                    .or(() -> userRepository.findByUsernameIgnoreCase(value));
        }

        return userRepository.findByUsernameIgnoreCase(value)
                .or(() -> userRepository.findByEmail(value));
    }
}
