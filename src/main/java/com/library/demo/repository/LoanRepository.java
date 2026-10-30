package com.library.demo.repository;

import com.library.demo.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserIdOrderByLoanDateDesc(Long userId);

    long countByUserIdAndReturnDateIsNull(Long userId);

    boolean existsByUserIdAndBookIdAndReturnDateIsNull(Long userId, Long bookId);

    boolean existsByBookId(Long bookId);

    List<Loan> findByReturnDateIsNullAndDueDateBefore(LocalDate date);
}
