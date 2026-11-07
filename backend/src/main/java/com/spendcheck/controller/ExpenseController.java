package com.spendcheck.controller;

import com.spendcheck.config.UserPrincipal;
import com.spendcheck.dto.*;
import com.spendcheck.exception.UnauthorizedException;
import com.spendcheck.model.Category;
import com.spendcheck.model.User;
import com.spendcheck.repository.UserRepository;
import com.spendcheck.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    public ExpenseController(ExpenseService expenseService, UserRepository userRepository) {
        this.expenseService = expenseService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user record not found"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ExpenseRequest request
    ) {
        User user = getAuthenticatedUser(principal);
        ExpenseResponse response = expenseService.createExpense(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Expense created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpenses(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        User user = getAuthenticatedUser(principal);
        List<ExpenseResponse> expenses = expenseService.getExpenses(user, category, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Expenses retrieved successfully", expenses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        User user = getAuthenticatedUser(principal);
        ExpenseResponse expense = expenseService.getExpenseById(user, id);
        return ResponseEntity.ok(ApiResponse.ok("Expense retrieved successfully", expense));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request
    ) {
        User user = getAuthenticatedUser(principal);
        ExpenseResponse updated = expenseService.updateExpense(user, id, request);
        return ResponseEntity.ok(ApiResponse.ok("Expense updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        User user = getAuthenticatedUser(principal);
        expenseService.deleteExpense(user, id);
        return ResponseEntity.ok(ApiResponse.ok("Expense deleted successfully", null));
    }

    @GetMapping("/summary/categories")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getCategorySummary(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = getAuthenticatedUser(principal);
        List<CategorySummaryResponse> summary = expenseService.getCategorySummaries(user);
        return ResponseEntity.ok(ApiResponse.ok("Category summary retrieved successfully", summary));
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<ApiResponse<List<MonthlySummaryResponse>>> getMonthlySummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        User user = getAuthenticatedUser(principal);
        List<MonthlySummaryResponse> summary = expenseService.getMonthlySummaries(user, year, month);
        return ResponseEntity.ok(ApiResponse.ok("Monthly summary retrieved successfully", summary));
    }
}

