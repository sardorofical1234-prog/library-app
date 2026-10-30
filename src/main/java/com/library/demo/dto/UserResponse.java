package com.library.demo.dto;

import com.library.demo.entity.AppUser;

public record UserResponse(Long id, String fullName, String username, String role) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getUsername(), user.getRole().name());
    }
}
