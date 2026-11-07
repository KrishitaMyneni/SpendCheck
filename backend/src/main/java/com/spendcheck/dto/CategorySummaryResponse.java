package com.spendcheck.dto;

import com.spendcheck.model.Category;

import java.math.BigDecimal;

public class CategorySummaryResponse {

    private Category category;
    private String categoryDisplayName;
    private BigDecimal totalAmount;
    private long count;
    private double percentage;

    public CategorySummaryResponse() {
    }

    public CategorySummaryResponse(Category category, BigDecimal totalAmount, long count, double percentage) {
        this.category = category;
        this.categoryDisplayName = category != null ? category.getDisplayName() : null;
        this.totalAmount = totalAmount;
        this.count = count;
        this.percentage = percentage;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        if (category != null) {
            this.categoryDisplayName = category.getDisplayName();
        }
    }

    public String getCategoryDisplayName() {
        return categoryDisplayName;
    }

    public void setCategoryDisplayName(String categoryDisplayName) {
        this.categoryDisplayName = categoryDisplayName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}

