package com.library.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookRequest(
        @NotBlank(message = "Sarlavha kiritilishi shart") String title,
        @NotBlank(message = "Muallif kiritilishi shart") String author,
        @NotBlank(message = "ISBN kiritilishi shart") String isbn,
        @NotNull(message = "Nashr yili kiritilishi shart")
        @Min(value = 1000, message = "Nashr yili noto'g'ri")
        @Max(value = 2100, message = "Nashr yili noto'g'ri") Integer publishedYear,
        @NotNull(message = "Nusxalar soni kiritilishi shart")
        @Min(value = 1, message = "Nusxalar soni kamida 1 bo'lishi kerak") Integer totalCopies
) {
}
