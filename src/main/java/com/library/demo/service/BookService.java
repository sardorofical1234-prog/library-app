package com.library.demo.service;

import com.library.demo.dto.BookRequest;
import com.library.demo.dto.BookResponse;
import com.library.demo.dto.PageResponse;
import com.library.demo.entity.Book;
import com.library.demo.exception.ApiException;
import com.library.demo.repository.BookRepository;
import com.library.demo.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BookService {
    private static final int MAX_PAGE_SIZE = 50;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    @Transactional(readOnly = true)
    public PageResponse<BookResponse> list(String search, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("title"));
        Page<Book> books;
        if (search == null || search.isBlank()) {
            books = bookRepository.findAll(pageable);
        } else {
            books = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                    search.trim(), search.trim(), pageable);
        }
        return PageResponse.from(books.map(BookResponse::from));
    }

    @Transactional(readOnly = true)
    public BookResponse get(Long id) {
        return BookResponse.from(findOrThrow(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        String isbn = normalizeIsbn(request.isbn());
        if (bookRepository.existsByIsbn(isbn)) {
            throw ApiException.conflict("Bu ISBN bilan kitob allaqachon mavjud: " + isbn);
        }
        Book book = new Book();
        book.setTitle(request.title().trim());
        book.setAuthor(request.author().trim());
        book.setIsbn(isbn);
        book.setPublishedYear(request.publishedYear());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.totalCopies());
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> ApiException.notFound("Kitob topilmadi: id=" + id));
        String isbn = normalizeIsbn(request.isbn());
        if (!book.getIsbn().equals(isbn) && bookRepository.existsByIsbn(isbn)) {
            throw ApiException.conflict("Bu ISBN bilan kitob allaqachon mavjud: " + isbn);
        }

        int borrowed = book.getTotalCopies() - book.getAvailableCopies();
        if (request.totalCopies() < borrowed) {
            throw ApiException.badRequest("Hozir " + borrowed
                    + " ta nusxa o'quvchilarda. Jami nusxa bundan kam bo'lishi mumkin emas");
        }

        book.setTitle(request.title().trim());
        book.setAuthor(request.author().trim());
        book.setIsbn(isbn);
        book.setPublishedYear(request.publishedYear());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.totalCopies() - borrowed);
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id) {
        Book book = bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> ApiException.notFound("Kitob topilmadi: id=" + id));
        if (loanRepository.existsByBookId(id)) {
            throw ApiException.conflict("Bu kitob bo'yicha ijara tarixi bor, o'chirib bo'lmaydi");
        }
        bookRepository.delete(book);
    }

    private Book findOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Kitob topilmadi: id=" + id));
    }

    private String normalizeIsbn(String isbn) {
        return isbn.trim().toUpperCase(Locale.ROOT);
    }
}
