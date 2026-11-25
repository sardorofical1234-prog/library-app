package com.library.demo.service;

import com.library.demo.dto.LoanResponse;
import com.library.demo.entity.AppUser;
import com.library.demo.entity.Book;
import com.library.demo.entity.Loan;
import com.library.demo.exception.ApiException;
import com.library.demo.repository.BookRepository;
import com.library.demo.repository.LoanRepository;
import com.library.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final int loanDays;
    private final int maxActiveLoans;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository, UserRepository userRepository,
                       @Value("${library.loan-days:14}") int loanDays,
                       @Value("${library.max-active-loans:3}") int maxActiveLoans) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.loanDays = loanDays;
        this.maxActiveLoans = maxActiveLoans;
    }

    @Transactional
    public LoanResponse borrow(String username, Long bookId) {
        // Locking the member serializes concurrent borrows and protects the per-member limit.
        AppUser user = userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> ApiException.notFound("Foydalanuvchi topilmadi"));
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> ApiException.notFound("Kitob topilmadi: id=" + bookId));

        if (book.getAvailableCopies() <= 0) throw ApiException.conflict("Bu kitobning bo'sh nusxasi qolmagan");
        if (loanRepository.existsByUserIdAndBookIdAndReturnDateIsNull(user.getId(), bookId)) {
            throw ApiException.conflict("Siz bu kitobni allaqachon olgansiz va hali qaytarmagansiz");
        }
        if (loanRepository.countByUserIdAndReturnDateIsNull(user.getId()) >= maxActiveLoans) {
            throw ApiException.badRequest("Bir vaqtda ko'pi bilan " + maxActiveLoans + " ta kitob olish mumkin");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        LocalDate today = LocalDate.now();
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(today);
        loan.setDueDate(today.plusDays(loanDays));
        return LoanResponse.from(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse returnBook(Long loanId, String username, boolean isAdmin) {
        // Locking the loan prevents two concurrent return requests from increasing stock twice.
        Loan loan = loanRepository.findByIdForUpdate(loanId)
                .orElseThrow(() -> ApiException.notFound("Ijara topilmadi: id=" + loanId));
        if (!isAdmin && !loan.getUser().getUsername().equals(username)) {
            throw ApiException.forbidden("Bu ijara sizga tegishli emas");
        }
        if (loan.getReturnDate() != null) throw ApiException.conflict("Bu kitob allaqachon qaytarilgan");

        Book book = bookRepository.findByIdForUpdate(loan.getBook().getId())
                .orElseThrow(() -> ApiException.notFound("Kitob topilmadi"));
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new IllegalStateException("Book inventory invariant violated for book id=" + book.getId());
        }
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        loan.setReturnDate(LocalDate.now());
        return LoanResponse.from(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> myLoans(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.notFound("Foydalanuvchi topilmadi"));
        return loanRepository.findByUserIdOrderByLoanDateDesc(user.getId()).stream().map(LoanResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> allLoans() {
        return loanRepository.findAllByOrderByLoanDateDesc().stream().map(LoanResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> overdueLoans() {
        return loanRepository.findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(LocalDate.now())
                .stream().map(LoanResponse::from).toList();
    }
}