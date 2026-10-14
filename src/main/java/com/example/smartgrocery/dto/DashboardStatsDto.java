package com.example.smartgrocery.dto;

import java.math.BigDecimal;

public class DashboardStatsDto {

    private long totalGroceryItems = 0;
    private BigDecimal totalEstimatedInventoryValue = BigDecimal.ZERO;
    private long lowStockCount = 0;
    private long nearExpiryCount = 0;
    private long expiredCount = 0;

    private long upcomingMealsCount = 0;
    private long shoppingListCount = 0;
    private BigDecimal estimatedShoppingCost = BigDecimal.ZERO;

    private BigDecimal monthlyBudget = BigDecimal.ZERO;
    private BigDecimal actualSpending = BigDecimal.ZERO;
    private BigDecimal remainingBudget = BigDecimal.ZERO;
    private double budgetUsagePercentage = 0.0;

    public DashboardStatsDto() {
    }

    public long getTotalGroceryItems() {
        return totalGroceryItems;
    }

    public void setTotalGroceryItems(long totalGroceryItems) {
        this.totalGroceryItems = totalGroceryItems;
    }

    public BigDecimal getTotalEstimatedInventoryValue() {
        return totalEstimatedInventoryValue;
    }

    public void setTotalEstimatedInventoryValue(BigDecimal totalEstimatedInventoryValue) {
        this.totalEstimatedInventoryValue = totalEstimatedInventoryValue;
    }

    public long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public long getNearExpiryCount() {
        return nearExpiryCount;
    }

    public void setNearExpiryCount(long nearExpiryCount) {
        this.nearExpiryCount = nearExpiryCount;
    }

    public long getExpiredCount() {
        return expiredCount;
    }

    public void setExpiredCount(long expiredCount) {
        this.expiredCount = expiredCount;
    }

    public long getUpcomingMealsCount() {
        return upcomingMealsCount;
    }

    public void setUpcomingMealsCount(long upcomingMealsCount) {
        this.upcomingMealsCount = upcomingMealsCount;
    }

    public long getShoppingListCount() {
        return shoppingListCount;
    }

    public void setShoppingListCount(long shoppingListCount) {
        this.shoppingListCount = shoppingListCount;
    }

    public BigDecimal getEstimatedShoppingCost() {
        return estimatedShoppingCost;
    }

    public void setEstimatedShoppingCost(BigDecimal estimatedShoppingCost) {
        this.estimatedShoppingCost = estimatedShoppingCost;
    }

    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public BigDecimal getActualSpending() {
        return actualSpending;
    }

    public void setActualSpending(BigDecimal actualSpending) {
        this.actualSpending = actualSpending;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public double getBudgetUsagePercentage() {
        return budgetUsagePercentage;
    }

    public void setBudgetUsagePercentage(double budgetUsagePercentage) {
        this.budgetUsagePercentage = budgetUsagePercentage;
    }
}
