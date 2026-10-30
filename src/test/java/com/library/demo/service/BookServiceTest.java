package com.library.demo.service;

import com.library.demo.dto.BookRequest;
import com.library.demo.dto.BookResponse;
import com.library.demo.entity.Book;
import com.library.demo.exception.ApiException;
import com.library.demo.repository.BookRepository;
import com.library.demo.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository bookRepository;
    @Mock
    LoanRepository loanRepository;
    @InjectMocks
    BookService bookService;

    private BookRequest request(int copies) {
        return new BookRequest("Clean Code", "Robert Martin", "111-222", 2008, copies);
    }

    private Book existingBook(int total, int available) {
        Book b = new Book();
        b.setId(1L);
        b.setTitle("Old");
        b.setAuthor("Old");
        b.setIsbn("111-222");
        b.setPublishedYear(2000);
        b.setTotalCopies(total);
        b.setAvailableCopies(available);
        return b;
    }

    @Test
    void create_setsAvailableEqualToTotal() {
        when(bookRepository.existsByIsbn("111-222")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookResponse response = bookService.create(request(5));

        assertEquals(5, response.totalCopies());
        assertEquals(5, response.availableCopies());
    }

    @Test
    void create_duplicateIsbn_throwsConflict() {
        when(bookRepository.existsByIsbn("111-222")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> bookService.create(request(1)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void update_totalLessThanBorrowed_throwsBadRequest() {
        // jami 5 ta, 3 tasi o'quvchilarda (available = 2)
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook(5, 2)));

        ApiException ex = assertThrows(ApiException.class, () -> bookService.update(1L, request(2)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void delete_bookWithLoans_throwsConflict() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook(2, 2)));
        when(loanRepository.existsByBookId(1L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> bookService.delete(1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(bookRepository, never()).delete(any());
    }
}
