package com.social.backend.auth.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.social.backend.auth.dto.SessionResponse;
import com.social.backend.auth.service.SessionService;
import com.social.backend.security.principal.AuthenticatedUser;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/")
    public List<SessionResponse> getActiveSessions(@AuthenticationPrincipal AuthenticatedUser principal) {
        return sessionService.getCurrentDeviceSessions(principal.id(), principal.sessionId());
    }
}
