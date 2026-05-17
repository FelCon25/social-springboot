package com.social.backend.security.principal;

import com.social.backend.user.model.UserRole;

public record AuthenticatedUser(
        Long id,
        String email,
        UserRole role) {

}
