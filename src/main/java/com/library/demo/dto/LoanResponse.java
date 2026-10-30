package com.library.demo.dto;

import com.library.demo.entity.Loan;

import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String username,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate,
        String status
) {
    public static LoanResponse from(Loan loan) {
        String status;
        if (loan.getReturnDate() != null) {
            status = "RETURNED";
        } else if (loan.getDueDate().isBefore(LocalDate.now())) {
            status = "OVERDUE";
        } else {
            status = "ACTIVE";
        }
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getUser().getUsername(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                status
        );
    }
}
