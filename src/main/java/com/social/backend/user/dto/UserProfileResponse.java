package com.social.backend.user.dto;

import java.time.Instant;

import com.social.backend.user.entity.User;
import com.social.backend.user.model.UserRole;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        String phoneNumber,
        String firstName,
        String lastName,
        Instant createdAt) {

    public static UserProfileResponse fromUser(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUserRole(),
                user.getPhoneNumber(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt());
    }
}
