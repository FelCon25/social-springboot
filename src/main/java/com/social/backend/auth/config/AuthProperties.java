package com.social.backend.auth.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "auth")
@Validated
public record AuthProperties(
        @NotNull Duration refreshTokenTtl,
        @Min(32) int refreshTokenBytes) {

}
