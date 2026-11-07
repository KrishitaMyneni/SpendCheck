package com.spendcheck.service;

import com.spendcheck.dto.CategorySummaryResponse;
import com.spendcheck.dto.ExpenseRequest;
import com.spendcheck.dto.ExpenseResponse;
import com.spendcheck.dto.MonthlySummaryResponse;
import com.spendcheck.exception.ResourceNotFoundException;
import com.spendcheck.model.Category;
import com.spendcheck.model.Expense;
import com.spendcheck.model.User;
import com.spendcheck.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse createExpense(User user, ExpenseRequest request) {
        Expense expense = new Expense(
                user,
                request.getTitle().trim(),
                request.getAmount(),
                request.getCategory(),
                request.getExpenseDate(),
                request.getNotes() != null ? request.getNotes().trim() : null
        );

        Expense saved = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses(User user, Category category, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses;
        if (category == null && startDate == null && endDate == null) {
            expenses = expenseRepository.findByUserOrderByExpenseDateDescCreatedAtDesc(user);
        } else {
            expenses = expenseRepository.findFilteredExpenses(user, category, startDate, endDate);
        }
        return expenses.stream().map(ExpenseResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(User user, Long id) {
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Expense with id " + id + " not found"));
        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(User user, Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Expense with id " + id + " not found"));

        expense.setTitle(request.getTitle().trim());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        Expense updated = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteExpense(User user, Long id) {
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Expense with id " + id + " not found"));
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getCategorySummaries(User user) {
        List<Object[]> rawSummaries = expenseRepository.findCategorySummariesByUser(user);
        BigDecimal grandTotal = BigDecimal.ZERO;
        List<CategorySummaryResponse> summaries = new ArrayList<>();

        for (Object[] row : rawSummaries) {
            Category category = (Category) row[0];
            BigDecimal total = (BigDecimal) row[1];
            long count = ((Number) row[2]).longValue();
            grandTotal = grandTotal.add(total);
            summaries.add(new CategorySummaryResponse(category, total, count, 0.0));
        }

        if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
            for (CategorySummaryResponse summary : summaries) {
                double pct = summary.getTotalAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(grandTotal, 2, RoundingMode.HALF_UP)
                        .doubleValue();
                summary.setPercentage(pct);
            }
        }

        return summaries;
    }

    @Transactional(readOnly = true)
    public List<MonthlySummaryResponse> getMonthlySummaries(User user, Integer year, Integer month) {
        List<Object[]> rawSummaries = expenseRepository.findMonthlySummariesByUser(user, year, month);
        List<MonthlySummaryResponse> summaries = new ArrayList<>();

        for (Object[] row : rawSummaries) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            BigDecimal total = (BigDecimal) row[2];
            long count = ((Number) row[3]).longValue();
            summaries.add(new MonthlySummaryResponse(y, m, total, count));
        }

        return summaries;
    }
}

