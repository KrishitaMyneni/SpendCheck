package com.spendcheck.repository;

import com.spendcheck.model.Category;
import com.spendcheck.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Find all expenses under a specific category.
     */
    List<Expense> findByCategory(Category category);

    /**
     * Find all expenses within a given date range.
     */
    List<Expense> findByExpenseDateBetweenOrderByExpenseDateDesc(LocalDate startDate, LocalDate endDate);
}
