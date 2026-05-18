package com.social.backend.security.principal;

import com.social.backend.user.model.UserRole;

public record AuthenticatedUser(
                Long id,
                String email,
                Long sessionId,
                UserRole role) {

}
