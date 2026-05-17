package com.social.backend.auth.exception;

import org.springframework.http.HttpStatus;

import com.social.backend.common.exception.BusinessException;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials");
    }

}
