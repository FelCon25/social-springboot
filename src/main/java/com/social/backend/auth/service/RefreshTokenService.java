package com.social.backend.auth.service;

import com.social.backend.auth.repository.RefreshTokenRepository;
import com.social.backend.auth.repository.SessionRepository;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.social.backend.auth.config.AuthProperties;
import com.social.backend.auth.entity.RefreshToken;
import com.social.backend.auth.entity.Session;
import com.social.backend.auth.exception.InvalidRefreshTokenException;
import com.social.backend.auth.util.TokenHasher;
import com.social.backend.user.entity.User;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SessionRepository sessionRepository;
    private final AuthProperties authProperties;

    @Transactional
    public IssuedRefreshToken issueRefreshToken(Session session, User user) {
        String rawToken = TokenHasher.generateOpaqueToken(authProperties.refreshTokenBytes());
        String hashedToken = TokenHasher.sha256(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .session(session)
                .hashedToken(hashedToken)
                .expiresAt(session.getExpiresAt())
                .isRevoked(false)
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);

        return new IssuedRefreshToken(refreshToken, rawToken);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public IssuedRefreshToken rotate(String rawRefreshToken) {
        String hashed = TokenHasher.sha256(rawRefreshToken);
        RefreshToken corrent = refreshTokenRepository.findByHashedToken(hashed)
                .orElseThrow(() -> new InvalidRefreshTokenException());

        Instant now = Instant.now();

        Session session = corrent.getSession();

        if (corrent.getUsedAt() != null) {
            log.warn("Sessionid: {} already used. Revoking all refresh tokens", session.getId());
            refreshTokenRepository.revokeAllBySessionId(session.getId(), now);
            sessionRepository.deactivateById(session.getId(), now);
            throw new InvalidRefreshTokenException();
        }

        if (corrent.getIsRevoked() || corrent.getExpiresAt().isBefore(now)) {
            throw new InvalidRefreshTokenException();
        }

        if (!session.getIsActive() || session.getExpiresAt().isBefore(now)) {
            throw new InvalidRefreshTokenException();
        }

        corrent.setUsedAt(now);
        corrent.setIsRevoked(true);

        IssuedRefreshToken next = issueRefreshToken(session, corrent.getUser());

        corrent.setReplacedBy(next.refreshToken());

        return next;

    }

    @Transactional
    public void revokeByRawToken(String rawRefreshToken) {
        String hashed = TokenHasher.sha256(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByHashedToken(hashed)
                .orElseThrow(() -> new InvalidRefreshTokenException());

        Session session = token.getSession();
        Instant now = Instant.now();
        token.setIsRevoked(true);

        refreshTokenRepository.revokeAllBySessionId(session.getId(), now);
        sessionRepository.deactivateById(session.getId(), now);

    }

    public record IssuedRefreshToken(
            RefreshToken refreshToken, String rawRefreshToken) {
    }
}
