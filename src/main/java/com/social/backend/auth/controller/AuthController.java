package com.social.backend.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.social.backend.auth.dto.AuthTokensResponse;
import com.social.backend.auth.dto.LoginRequest;
import com.social.backend.auth.dto.RefreshTokenRequest;
import com.social.backend.auth.dto.RegisterRequest;
import com.social.backend.auth.service.AuthService;
import com.social.backend.auth.util.RequestMetadata;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthTokensResponse register(@Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest httpRequest) {
        return authService.register(registerRequest, RequestMetadata.userAgent(httpRequest),
                RequestMetadata.clientIp(httpRequest));
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthTokensResponse login(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpRequest) {
        return authService.login(loginRequest, RequestMetadata.userAgent(httpRequest),
                RequestMetadata.clientIp(httpRequest));
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AuthTokensResponse refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authService.refresh(refreshTokenRequest.refreshToken());
    }

}
