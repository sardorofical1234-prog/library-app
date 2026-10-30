package com.library.demo.service;

import com.library.demo.dto.LoanResponse;
import com.library.demo.entity.AppUser;
import com.library.demo.entity.Book;
import com.library.demo.entity.Loan;
import com.library.demo.entity.Role;
import com.library.demo.exception.ApiException;
import com.library.demo.repository.BookRepository;
import com.library.demo.repository.LoanRepository;
import com.library.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    LoanRepository loanRepository;
    @Mock
    BookRepository bookRepository;
    @Mock
    UserRepository userRepository;

    LoanService loanService;
    AppUser ali;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(loanRepository, bookRepository, userRepository, 14, 3);

        ali = new AppUser();
        ali.setId(10L);
        ali.setUsername("ali");
        ali.setFullName("Ali Valiyev");
        ali.setRole(Role.MEMBER);
    }

    private Book book(int available) {
        Book b = new Book();
        b.setId(1L);
        b.setTitle("Clean Code");
        b.setTotalCopies(3);
        b.setAvailableCopies(available);
        return b;
    }

    private Loan activeLoan(Book book) {
        Loan loan = new Loan();
        loan.setId(100L);
        loan.setBook(book);
        loan.setUser(ali);
        loan.setLoanDate(LocalDate.now().minusDays(3));
        loan.setDueDate(LocalDate.now().plusDays(11));
        return loan;
    }

    @Test
    void borrow_success_decreasesCopiesAndSetsDueDate() {
        Book book = book(2);
        when(userRepository.findByUsername("ali")).thenReturn(Optional.of(ali));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(loanRepository.existsByUserIdAndBookIdAndReturnDateIsNull(10L, 1L)).thenReturn(false);
        when(loanRepository.countByUserIdAndReturnDateIsNull(10L)).thenReturn(0L);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanResponse response = loanService.borrow("ali", 1L);

        assertEquals(1, book.getAvailableCopies());
        assertEquals(LocalDate.now().plusDays(14), response.dueDate());
        assertEquals("ACTIVE", response.status());
    }

    @Test
    void borrow_noCopiesLeft_throwsConflict() {
        when(userRepository.findByUsername("ali")).thenReturn(Optional.of(ali));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book(0)));

        ApiException ex = assertThrows(ApiException.class, () -> loanService.borrow("ali", 1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrow_limitReached_throwsBadRequest() {
        when(userRepository.findByUsername("ali")).thenReturn(Optional.of(ali));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book(1)));
        when(loanRepository.existsByUserIdAndBookIdAndReturnDateIsNull(10L, 1L)).thenReturn(false);
        when(loanRepository.countByUserIdAndReturnDateIsNull(10L)).thenReturn(3L);

        ApiException ex = assertThrows(ApiException.class, () -> loanService.borrow("ali", 1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void returnBook_success_increasesCopies() {
        Book book = book(0);
        Loan loan = activeLoan(book);
        when(loanRepository.findById(100L)).thenReturn(Optional.of(loan));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));

        LoanResponse response = loanService.returnBook(100L, "ali", false);

        assertEquals(1, book.getAvailableCopies());
        assertNotNull(loan.getReturnDate());
        assertEquals("RETURNED", response.status());
    }

    @Test
    void returnBook_otherUser_throwsForbidden() {
        when(loanRepository.findById(100L)).thenReturn(Optional.of(activeLoan(book(0))));

        ApiException ex = assertThrows(ApiException.class, () -> loanService.returnBook(100L, "vali", false));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void returnBook_alreadyReturned_throwsConflict() {
        Loan loan = activeLoan(book(1));
        loan.setReturnDate(LocalDate.now());
        when(loanRepository.findById(100L)).thenReturn(Optional.of(loan));

        ApiException ex = assertThrows(ApiException.class, () -> loanService.returnBook(100L, "ali", false));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }
}
