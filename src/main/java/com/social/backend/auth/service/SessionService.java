package com.social.backend.auth.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.social.backend.auth.config.AuthProperties;
import com.social.backend.auth.dto.SessionResponse;
import com.social.backend.auth.entity.Session;
import com.social.backend.auth.repository.SessionRepository;
import com.social.backend.user.entity.User;

import jakarta.transaction.Transactional;

@Service
public class SessionService {
    private final AuthProperties authProperties;
    private final SessionRepository sessionRepository;

    public SessionService(AuthProperties authProperties, SessionRepository sessionRepository) {
        this.authProperties = authProperties;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public Session createSession(User user, String userAgent, String clientIp) {
        Instant now = Instant.now();
        Session session = Session.builder()
                .user(user)
                .userAgent(userAgent)
                .ipAddress(clientIp)
                .isActive(true)
                .expiresAt(now.plus(authProperties.refreshTokenTtl()))
                .build();

        return sessionRepository.save(session);
    }

    public List<SessionResponse> getCurrentDeviceSessions(Long userId, Long currentSessionId) {
        List<Session> sessions = sessionRepository.findActiveSessions(userId);

        return sessions.stream()
                .map(s -> SessionResponse.toSessionResponse(s, currentSessionId))
                .toList();
    }
}
