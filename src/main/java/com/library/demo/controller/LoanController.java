package com.library.demo.controller;

import com.library.demo.dto.LoanResponse;
import com.library.demo.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/borrow/{bookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse borrow(@PathVariable Long bookId, Authentication authentication) {
        return loanService.borrow(authentication.getName(), bookId);
    }

    @PostMapping("/{loanId}/return")
    public LoanResponse returnBook(@PathVariable Long loanId, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return loanService.returnBook(loanId, authentication.getName(), isAdmin);
    }

    @GetMapping("/my")
    public List<LoanResponse> myLoans(Authentication authentication) {
        return loanService.myLoans(authentication.getName());
    }

    @GetMapping("/all")
    public List<LoanResponse> allLoans() {
        return loanService.allLoans();
    }

    @GetMapping("/overdue")
    public List<LoanResponse> overdueLoans() {
        return loanService.overdueLoans();
    }
}
