package com.library.demo.service;

import com.library.demo.dto.BookResponse;
import com.library.demo.dto.PageResponse;
import com.library.demo.entity.Book;
import com.library.demo.repository.BookRepository;
import com.library.demo.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServicePaginationTest {

    @Mock
    BookRepository bookRepository;
    @Mock
    LoanRepository loanRepository;
    @InjectMocks
    BookService bookService;

    private Book book() {
        Book b = new Book();
        b.setId(1L);
        b.setTitle("Clean Code");
        b.setAuthor("Robert Martin");
        b.setIsbn("111");
        b.setPublishedYear(2008);
        b.setTotalCopies(2);
        b.setAvailableCopies(2);
        return b;
    }

    @Test
    void list_clampsPageAndSize() {
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(book())));

        PageResponse<BookResponse> result = bookService.list(null, -5, 1000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepository).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(50, captor.getValue().getPageSize());
        assertEquals(1, result.content().size());
    }

    @Test
    void list_withSearch_usesSearchQuery() {
        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                eq("clean"), eq("clean"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(book())));

        PageResponse<BookResponse> result = bookService.list("clean", 0, 10);

        assertEquals(1, result.totalElements());
        assertEquals("Clean Code", result.content().get(0).title());
    }
}
