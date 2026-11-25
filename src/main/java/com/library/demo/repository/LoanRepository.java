package com.library.demo.repository;

import com.library.demo.entity.Loan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    @EntityGraph(attributePaths = {"book", "user"})
    List<Loan> findByUserIdOrderByLoanDateDesc(Long userId);

    long countByUserIdAndReturnDateIsNull(Long userId);
    boolean existsByUserIdAndBookIdAndReturnDateIsNull(Long userId, Long bookId);
    boolean existsByBookId(Long bookId);

    @EntityGraph(attributePaths = {"book", "user"})
    List<Loan> findAllByOrderByLoanDateDesc();

    @EntityGraph(attributePaths = {"book", "user"})
    List<Loan> findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Loan l where l.id = :id")
    Optional<Loan> findByIdForUpdate(@Param("id") Long id);
}