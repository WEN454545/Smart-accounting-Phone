package com.example.myapplication.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey
    @NonNull
    private String yearMonth;
    private double totalBudget;
    private long createdAt;
    private long userId;

    public Budget() {}

    @Ignore
    public Budget(@NonNull String yearMonth, double totalBudget, long createdAt, long userId) {
        this.yearMonth = yearMonth;
        this.totalBudget = totalBudget;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    @NonNull
    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(@NonNull String yearMonth) { this.yearMonth = yearMonth; }

    public double getTotalBudget() { return totalBudget; }
    public void setTotalBudget(double totalBudget) { this.totalBudget = totalBudget; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
}
