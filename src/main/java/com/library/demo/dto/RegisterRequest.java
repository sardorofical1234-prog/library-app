package com.library.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "To'liq ism kiritilishi shart") String fullName,
        @NotBlank(message = "Username kiritilishi shart")
        @Size(min = 3, max = 30, message = "Username 3 dan 30 gacha belgi bo'lishi kerak") String username,
        @NotBlank(message = "Parol kiritilishi shart")
        @Size(min = 6, max = 100, message = "Parol kamida 6 belgi bo'lishi kerak") String password
) {
}
