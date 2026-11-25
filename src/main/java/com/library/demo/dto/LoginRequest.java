package com.library.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username kiritilishi shart")
        @Size(min = 3, max = 30, message = "Username 3 dan 30 gacha belgi bo'lishi kerak")
        String username,
        @NotBlank(message = "Parol kiritilishi shart")
        @Size(min = 8, max = 100, message = "Parol 8 dan 100 gacha belgi bo'lishi kerak")
        String password
) { }