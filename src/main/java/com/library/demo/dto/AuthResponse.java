package com.library.demo.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, UserResponse user) { }