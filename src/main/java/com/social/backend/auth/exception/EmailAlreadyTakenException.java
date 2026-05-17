package com.social.backend.auth.exception;

import com.social.backend.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyTakenException extends BusinessException {
    public EmailAlreadyTakenException(String email) {
        super(HttpStatus.CONFLICT, "EMAIL_ALREADY_TAKEN", "Email " + email + " is already taken");
    }

    public EmailAlreadyTakenException() {
        super(HttpStatus.CONFLICT, "EMAIL_ALREADY_TAKEN", "Email is already taken");
    }
}
