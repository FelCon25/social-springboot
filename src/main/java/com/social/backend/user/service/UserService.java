package com.social.backend.user.service;

import org.springframework.stereotype.Service;

import com.social.backend.common.exception.NotFoundException;
import com.social.backend.user.dto.UserProfileResponse;
import com.social.backend.user.entity.User;
import com.social.backend.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return UserProfileResponse.fromUser(user);
    }

}
