package com.social.backend.auth.exception;

import org.springframework.http.HttpStatus;

import com.social.backend.common.exception.BusinessException;

public class GenericEmailOrUsernameAlreadyTaken extends BusinessException {
    public GenericEmailOrUsernameAlreadyTaken() {
        super(HttpStatus.CONFLICT, "EMAIL_OR_USERNAME_ALREADY_TAKEN", "Email or username is already taken");
    }
}
