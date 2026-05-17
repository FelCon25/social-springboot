package com.social.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
                @NotBlank @Size(min = 3, max = 30) @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Username must contain only letters, numbers, and underscores") String username,

                @NotBlank @Size(min = 3, max = 255) @Email(message = "Email is not valid") String email,

                @NotBlank @Size(min = 8, max = 500) String password,

                @NotBlank @Size(min = 3, max = 50) String firstName,

                @NotBlank @Size(min = 3, max = 50) String lastName,

                @NotBlank @Size(min = 10, max = 15) String phoneNumber) {

}
