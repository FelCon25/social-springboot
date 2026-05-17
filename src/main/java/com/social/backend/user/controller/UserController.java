package com.social.backend.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.social.backend.security.principal.AuthenticatedUser;
import com.social.backend.user.dto.UserProfileResponse;
import com.social.backend.user.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/hello")
    public String hello() {

        return "Hello World";
    }

    @GetMapping("/me")
    public UserProfileResponse getCorrentUser(@AuthenticationPrincipal AuthenticatedUser principal) {
        return userService.getUserById(principal.id());
    }
}
