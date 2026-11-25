package com.library.demo;

import com.library.demo.dto.AuthResponse;
import com.library.demo.dto.BookRequest;
import com.library.demo.dto.BookResponse;
import com.library.demo.dto.LoginRequest;
import com.library.demo.dto.LoanResponse;
import com.library.demo.dto.RegisterRequest;
import com.library.demo.dto.UserResponse;
import com.library.demo.repository.BookRepository;
import com.library.demo.repository.LoanRepository;
import com.library.demo.repository.UserRepository;
import com.library.demo.service.AuthService;
import com.library.demo.service.BookService;
import com.library.demo.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class LibraryPersistenceIntegrationTest {
    @Autowired private AuthService authService;
    @Autowired private BookService bookService;
    @Autowired private LoanService loanService;
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private JwtDecoder jwtDecoder;

    @BeforeEach
    void cleanDatabase() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registrationLoginBorrowAndReturn_workAgainstPostgresSchema() {
        UserResponse member = authService.register(
                new RegisterRequest("Ali Valiyev", "Ali", "strong-password-123"));
        assertEquals("MEMBER", member.role());

        AuthResponse login = authService.login(new LoginRequest("ALI", "strong-password-123"));
        assertEquals("Bearer", login.tokenType());
        assertEquals(900, login.expiresInSeconds());

        Jwt jwt = jwtDecoder.decode(login.accessToken());
        assertEquals("ali", jwt.getSubject());
        assertEquals(List.of("MEMBER"), jwt.getClaimAsStringList("roles"));

        BookResponse book = bookService.create(
                new BookRequest("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 2));
        assertEquals(2, book.availableCopies());

        LoanResponse borrowed = loanService.borrow("ali", book.id());
        assertEquals("ACTIVE", borrowed.status());
        assertEquals(1, bookRepository.findById(book.id()).orElseThrow().getAvailableCopies());

        LoanResponse returned = loanService.returnBook(borrowed.id(), "ali", false);
        assertEquals("RETURNED", returned.status());
        assertEquals(2, bookRepository.findById(book.id()).orElseThrow().getAvailableCopies());
        assertNotNull(loanRepository.findById(borrowed.id()).orElseThrow().getReturnDate());
    }
}