package com.social.backend.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.social.backend.security.config.JwtProperties;
import com.social.backend.security.principal.AuthenticatedUser;
import com.social.backend.user.entity.User;
import com.social.backend.user.model.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_EMAIL = "email";

    private final JwtProperties jwtProperties;

    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] byteKey = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        if (byteKey.length < 32) {
            throw new IllegalArgumentException("secret key must be at least 256 bits long for HS256");
        }
        this.secretKey = Keys.hmacShaKeyFor(byteKey);
    }

    public AccessToken generateAccessToken(User user) {

        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.accessTokenTtl());
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(user.getId()))
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getUserRole().name())
                .signWith(secretKey)
                .compact();

        return new AccessToken(
                token,
                expiresAt,
                jwtProperties.accessTokenTtl().toSeconds());
    }

    public Jws<Claims> parseAccessToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token);
    }

    public AuthenticatedUser toPrincipal(Claims claims) {
        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get(CLAIM_EMAIL, String.class);
        UserRole role = UserRole.valueOf(claims.get(CLAIM_ROLE, String.class));

        return new AuthenticatedUser(userId, email, role);
    }

    public record AccessToken(
            String accessToken,
            Instant expiresAt,
            long expiresInSeconds) {
    }
}
