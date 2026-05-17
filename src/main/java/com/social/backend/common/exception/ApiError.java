package com.social.backend.common.exception;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        List<FieldError> fieldErrors,
        String path) {

    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(Instant.now(), status, code, message, null, path);
    }

    public static ApiError of(int status, String code, String message, List<FieldError> fieldErrors, String path) {
        return new ApiError(Instant.now(), status, code, message, fieldErrors, path);
    }

    public record FieldError(
            String field,
            String message) {
    }

}
