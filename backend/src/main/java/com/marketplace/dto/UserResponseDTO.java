package com.marketplace.dto;

import com.marketplace.entities.Role;
import com.marketplace.entities.User;

public record UserResponseDTO(Long id, String name, String email, Role role) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
