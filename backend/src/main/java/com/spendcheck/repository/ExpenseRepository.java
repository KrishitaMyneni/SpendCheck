package com.spendcheck.repository;

import com.spendcheck.model.Category;
import com.spendcheck.model.Expense;
import com.spendcheck.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndUser(Long id, User user);

    List<Expense> findByUserOrderByExpenseDateDescCreatedAtDesc(User user);

    @Query("SELECT e FROM Expense e WHERE e.user = :user " +
            "AND (:category IS NULL OR e.category = :category) " +
            "AND (CAST(:startDate AS date) IS NULL OR e.expenseDate >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR e.expenseDate <= :endDate) " +
            "ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> findFilteredExpenses(
            @Param("user") User user,
            @Param("category") Category category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT e.category, SUM(e.amount), COUNT(e) " +
            "FROM Expense e WHERE e.user = :user " +
            "GROUP BY e.category " +
            "ORDER BY SUM(e.amount) DESC")
    List<Object[]> findCategorySummariesByUser(@Param("user") User user);

    @Query("SELECT YEAR(e.expenseDate), MONTH(e.expenseDate), SUM(e.amount), COUNT(e) " +
            "FROM Expense e WHERE e.user = :user " +
            "AND (:year IS NULL OR YEAR(e.expenseDate) = :year) " +
            "AND (:month IS NULL OR MONTH(e.expenseDate) = :month) " +
            "GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate) " +
            "ORDER BY YEAR(e.expenseDate) DESC, MONTH(e.expenseDate) DESC")
    List<Object[]> findMonthlySummariesByUser(
            @Param("user") User user,
            @Param("year") Integer year,
            @Param("month") Integer month);
}
