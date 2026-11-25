package com.library.demo.config;

import com.library.demo.entity.AppUser;
import com.library.demo.entity.Book;
import com.library.demo.entity.Role;
import com.library.demo.repository.BookRepository;
import com.library.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Locale;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedData(
            UserRepository userRepository,
            BookRepository bookRepository,
            PasswordEncoder passwordEncoder,
            @Value("${library.bootstrap-admin.full-name}") String adminFullName,
            @Value("${library.bootstrap-admin.username}") String adminUsername,
            @Value("${library.bootstrap-admin.password}") String adminPassword) {
        return args -> {
            boolean hasUsername = adminUsername != null && !adminUsername.isBlank();
            boolean hasPassword = adminPassword != null && !adminPassword.isBlank();
            if (hasUsername != hasPassword) {
                throw new IllegalStateException("Set both BOOTSTRAP_ADMIN_USERNAME and BOOTSTRAP_ADMIN_PASSWORD, or leave both empty");
            }
            if (hasUsername) {
                if (adminPassword.length() < 12) {
                    throw new IllegalStateException("Bootstrap admin password must contain at least 12 characters");
                }
                String username = adminUsername.trim().toLowerCase(Locale.ROOT);
                if (!userRepository.existsByUsername(username)) {
                    AppUser admin = new AppUser();
                    admin.setFullName(adminFullName.trim());
                    admin.setUsername(username);
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    admin.setRole(Role.ADMIN);
                    userRepository.save(admin);
                }
            }
            if (bookRepository.count() == 0) {
                bookRepository.save(newBook("O'tkan kunlar", "Abdulla Qodiriy", "978-9943-0001-1", 1926, 3));
                bookRepository.save(newBook("Mehrobdan chayon", "Abdulla Qodiriy", "978-9943-0001-2", 1929, 2));
                bookRepository.save(newBook("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 2));
                bookRepository.save(newBook("Effective Java", "Joshua Bloch", "978-0134685991", 2018, 2));
            }
        };
    }

    private Book newBook(String title, String author, String isbn, int year, int copies) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPublishedYear(year);
        book.setTotalCopies(copies);
        book.setAvailableCopies(copies);
        return book;
    }
}