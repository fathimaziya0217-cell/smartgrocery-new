package com.example.smartgrocery.service;

import com.example.smartgrocery.entity.*;
import com.example.smartgrocery.exception.ResourceNotFoundException;
import com.example.smartgrocery.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final GroceryItemRepository groceryItemRepository;
    private final InventoryBatchRepository batchRepository;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    @Value("${app.alerts.near-expiry-days:7}")
    private int nearExpiryDaysThreshold;

    public AlertService(AlertRepository alertRepository,
                        GroceryItemRepository groceryItemRepository,
                        InventoryBatchRepository batchRepository,
                        BudgetRepository budgetRepository,
                        ExpenseRepository expenseRepository,
                        UserRepository userRepository,
                        InventoryService inventoryService) {
        this.alertRepository = alertRepository;
        this.groceryItemRepository = groceryItemRepository;
        this.batchRepository = batchRepository;
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<Alert> getActiveAlerts(Long userId) {
        return alertRepository.findByUserIdAndResolvedFalseOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Alert> getAllAlerts(Long userId) {
        return alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadActiveAlertCount(Long userId) {
        return alertRepository.countByUserIdAndResolvedFalse(userId);
    }

    @Transactional
    public void markAlertRead(Long alertId, Long userId) {
        Alert alert = alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        alert.setReadStatus(true);
        alertRepository.save(alert);
    }

    @Transactional
    public void resolveAlert(Long alertId, Long userId) {
        Alert alert = alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        alert.setResolved(true);
        alertRepository.save(alert);
    }

    // ===================================================================
    // Automated Alert Refresh & Generation
    // ===================================================================
    @Transactional
    public void refreshAlertsForUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.plusDays(nearExpiryDaysThreshold);

        // 1. Low-Stock Detection
        List<GroceryItem> userItems = groceryItemRepository.findByUserIdOrderByNameAsc(userId);
        for (GroceryItem item : userItems) {
            BigDecimal usableStock = inventoryService.calculateUsableStock(item.getId());
            BigDecimal minStock = item.getMinimumStock() != null ? item.getMinimumStock() : BigDecimal.ZERO;

            if (usableStock.compareTo(minStock) <= 0) {
                if (!alertRepository.existsByUserIdAndGroceryItemIdAndAlertTypeAndResolvedFalse(userId, item.getId(), "LOW_STOCK")) {
                    Alert alert = new Alert(user, item, "LOW_STOCK",
                            "Low stock alert: " + item.getName() + " has " + usableStock + " " + item.getUnit() +
                            " remaining (Minimum: " + minStock + " " + item.getUnit() + ").");
                    alertRepository.save(alert);
                }
            }
        }

        // 2. Near-Expiry Detection
        List<InventoryBatch> nearExpiryBatches = batchRepository.findNearExpiryBatches(userId, today, thresholdDate);
        for (InventoryBatch batch : nearExpiryBatches) {
            GroceryItem item = batch.getGroceryItem();
            if (!alertRepository.existsByUserIdAndGroceryItemIdAndAlertTypeAndResolvedFalse(userId, item.getId(), "NEAR_EXPIRY")) {
                Alert alert = new Alert(user, item, "NEAR_EXPIRY",
                        "Near expiry alert: " + item.getName() + " (Batch #" + batch.getId() +
                        ") expires on " + batch.getExpiryDate() + ".");
                alertRepository.save(alert);
            }
        }

        // 3. Expired-Item Detection
        List<InventoryBatch> expiredBatches = batchRepository.findExpiredBatches(userId, today);
        for (InventoryBatch batch : expiredBatches) {
            GroceryItem item = batch.getGroceryItem();
            if (!alertRepository.existsByUserIdAndGroceryItemIdAndAlertTypeAndResolvedFalse(userId, item.getId(), "EXPIRED")) {
                Alert alert = new Alert(user, item, "EXPIRED",
                        "Expired alert: " + item.getName() + " (Batch #" + batch.getId() +
                        ") expired on " + batch.getExpiryDate() + " and should be discarded.");
                alertRepository.save(alert);
            }
        }

        // 4. Budget Warning Detection
        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndBudgetMonthAndBudgetYear(userId, today.getMonthValue(), today.getYear());
        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
            BigDecimal spending = expenseRepository.sumExpensesForUserBetweenDates(userId, monthStart, monthEnd);

            if (spending.compareTo(budget.getBudgetAmount()) > 0) {
                if (!alertRepository.existsByUserIdAndAlertTypeAndResolvedFalse(userId, "BUDGET_WARNING")) {
                    Alert alert = new Alert(user, null, "BUDGET_WARNING",
                            "Budget warning: Current spending (₹" + spending + ") has exceeded your allocated budget of ₹" +
                            budget.getBudgetAmount() + " for this month.");
                    alertRepository.save(alert);
                }
            }
        }
    }
}
