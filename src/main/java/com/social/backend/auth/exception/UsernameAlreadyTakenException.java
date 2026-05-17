package com.social.backend.auth.exception;

import org.springframework.http.HttpStatus;

import com.social.backend.common.exception.BusinessException;

public class UsernameAlreadyTakenException extends BusinessException {
    public UsernameAlreadyTakenException(String username) {
        super(HttpStatus.CONFLICT, "USERNAME_ALREADY_TAKEN", "Username " + username + " is already taken");
    }

    public UsernameAlreadyTakenException() {
        super(HttpStatus.CONFLICT, "USERNAME_ALREADY_TAKEN", "Username is already taken");
    }
}
