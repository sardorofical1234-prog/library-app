package com.library.demo.dto;

import com.library.demo.entity.Book;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        Integer publishedYear,
        int totalCopies,
        int availableCopies
) {
    public static BookResponse from(Book b) {
        return new BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                b.getPublishedYear(), b.getTotalCopies(), b.getAvailableCopies());
    }
}
