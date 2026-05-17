package com.social.backend.auth.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.social.backend.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Modifying
    @Query("update RefreshToken rt set rt.isRevoked = true, rt.updatedAt = :now" +
            " where rt.session.id = :sessionId and rt.isRevoked = false")
    int revokeAllBySessionId(@Param("sessionId") Long sessionId,
            @Param("now") Instant now);

    Optional<RefreshToken> findByHashedToken(String hashedToken);
}
