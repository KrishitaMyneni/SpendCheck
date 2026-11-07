package com.spendcheck.dto;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class MonthlySummaryResponse {

    private int year;
    private int month;
    private String monthName;
    private BigDecimal totalAmount;
    private long count;

    public MonthlySummaryResponse() {
    }

    public MonthlySummaryResponse(int year, int month, BigDecimal totalAmount, long count) {
        this.year = year;
        this.month = month;
        if (month >= 1 && month <= 12) {
            this.monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        }
        this.totalAmount = totalAmount;
        this.count = count;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
        if (month >= 1 && month <= 12) {
            this.monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        }
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
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
}

