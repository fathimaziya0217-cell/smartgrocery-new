package com.example.smartgrocery.service;

import com.example.smartgrocery.dto.DashboardStatsDto;
import com.example.smartgrocery.entity.Budget;
import com.example.smartgrocery.repository.GroceryItemRepository;
import com.example.smartgrocery.repository.InventoryBatchRepository;
import com.example.smartgrocery.repository.ShoppingListRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class DashboardService {

    private final GroceryItemRepository groceryItemRepository;
    private final InventoryBatchRepository batchRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final InventoryService inventoryService;
    private final MealPlanService mealPlanService;
    private final ShoppingListService shoppingListService;
    private final BudgetService budgetService;
    private final ExpenseService expenseService;

    @Value("${app.alerts.near-expiry-days:7}")
    private int nearExpiryDaysThreshold;

    public DashboardService(GroceryItemRepository groceryItemRepository,
                            InventoryBatchRepository batchRepository,
                            ShoppingListRepository shoppingListRepository,
                            InventoryService inventoryService,
                            MealPlanService mealPlanService,
                            ShoppingListService shoppingListService,
                            BudgetService budgetService,
                            ExpenseService expenseService) {
        this.groceryItemRepository = groceryItemRepository;
        this.batchRepository = batchRepository;
        this.shoppingListRepository = shoppingListRepository;
        this.inventoryService = inventoryService;
        this.mealPlanService = mealPlanService;
        this.shoppingListService = shoppingListService;
        this.budgetService = budgetService;
        this.expenseService = expenseService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats(Long userId) {
        DashboardStatsDto stats = new DashboardStatsDto();
        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.plusDays(nearExpiryDaysThreshold);

        // 1. Inventory stats
        stats.setTotalGroceryItems(groceryItemRepository.countByUserId(userId));
        stats.setTotalEstimatedInventoryValue(inventoryService.calculateTotalInventoryValue(userId));
        stats.setLowStockCount(groceryItemRepository.findLowStockItems(userId).size());
        stats.setNearExpiryCount(batchRepository.findNearExpiryBatches(userId, today, thresholdDate).size());
        stats.setExpiredCount(batchRepository.findExpiredBatches(userId, today).size());

        // 2. Meal plans and shopping lists
        stats.setUpcomingMealsCount(mealPlanService.getUpcomingMeals(userId).size());
        stats.setShoppingListCount(shoppingListRepository.countByUserId(userId));
        stats.setEstimatedShoppingCost(shoppingListService.calculateTotalShoppingCost(userId));

        // 3. Budget & Expenses
        Optional<Budget> currentBudget = budgetService.getCurrentMonthBudget(userId);
        BigDecimal budgetAmount = currentBudget.map(Budget::getBudgetAmount).orElse(BigDecimal.ZERO);
        BigDecimal actualSpending = expenseService.sumExpensesForMonth(userId, today.getMonthValue(), today.getYear());

        stats.setMonthlyBudget(budgetAmount);
        stats.setActualSpending(actualSpending);
        stats.setRemainingBudget(budgetService.calculateRemainingBudget(budgetAmount, actualSpending));
        stats.setBudgetUsagePercentage(budgetService.calculateBudgetUsagePercentage(budgetAmount, actualSpending));

        return stats;
    }
}
