package com.social.backend.auth.dto;

public record AuthTokensResponse(
        String accessToken,
        String refreshToken,
        String tokenType) {

    public static AuthTokensResponse bearer(String accessToken, String refreshToken) {
        return new AuthTokensResponse(accessToken, refreshToken, "Bearer");
    }

}
