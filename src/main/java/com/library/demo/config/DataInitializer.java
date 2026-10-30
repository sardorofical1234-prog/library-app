package com.library.demo.config;

import com.library.demo.entity.AppUser;
import com.library.demo.entity.Book;
import com.library.demo.entity.Role;
import com.library.demo.repository.BookRepository;
import com.library.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Dastur birinchi marta ishga tushganda admin va bir nechta namunaviy kitob yaratadi.
 * DIQQAT: admin paroli faqat o'rganish uchun! Real loyihada o'zgartiring.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               BookRepository bookRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                AppUser admin = new AppUser();
                admin.setFullName("Kutubxonachi");
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
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
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setIsbn(isbn);
        b.setPublishedYear(year);
        b.setTotalCopies(copies);
        b.setAvailableCopies(copies);
        return b;
    }
}
