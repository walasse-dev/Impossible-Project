package com.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;

public record RegisterRequestDTO(
        @NotBlank
        @Size(max = 255)
        String name,
        @NotBlank
        @Email
        @Size(max = 255)
        String email,
        @NotBlank
        @Size(min = 8)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {
    @AssertTrue(message = "Password must not exceed 72 bytes in UTF-8")
    @JsonIgnore
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
