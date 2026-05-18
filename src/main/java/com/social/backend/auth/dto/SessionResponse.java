package com.social.backend.auth.dto;

import java.time.Instant;

import com.social.backend.auth.entity.Session;

public record SessionResponse(
                Long id,
                String ipAddress,
                String userAgent,
                Instant createdAt,
                Instant expiresAt,
                boolean isCurrent) {

        public static SessionResponse toSessionResponse(Session session, Long currentSessionId) {
                return new SessionResponse(
                                session.getId(),
                                session.getIpAddress(),
                                session.getUserAgent(),
                                session.getCreatedAt(),
                                session.getExpiresAt(),
                                session.getId().equals(currentSessionId));
        }

}
