package com.example.smartgrocery.service;

import com.example.smartgrocery.dto.BudgetDto;
import com.example.smartgrocery.entity.Budget;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseService expenseService;

    public BudgetService(BudgetRepository budgetRepository, ExpenseService expenseService) {
        this.budgetRepository = budgetRepository;
        this.expenseService = expenseService;
    }

    @Transactional(readOnly = true)
    public List<Budget> getUserBudgets(Long userId) {
        return budgetRepository.findByUserIdOrderByBudgetYearDescBudgetMonthDesc(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Budget> getBudgetForMonth(Long userId, int month, int year) {
        return budgetRepository.findByUserIdAndBudgetMonthAndBudgetYear(userId, month, year);
    }

    @Transactional(readOnly = true)
    public Optional<Budget> getCurrentMonthBudget(Long userId) {
        LocalDate now = LocalDate.now();
        return getBudgetForMonth(userId, now.getMonthValue(), now.getYear());
    }

    @Transactional
    public Budget saveOrUpdateBudget(BudgetDto dto, User user) {
        Optional<Budget> existingOpt = budgetRepository.findByUserIdAndBudgetMonthAndBudgetYear(
                user.getId(), dto.getBudgetMonth(), dto.getBudgetYear());

        Budget budget = existingOpt.orElseGet(() -> {
            Budget b = new Budget();
            b.setUser(user);
            b.setBudgetMonth(dto.getBudgetMonth());
            b.setBudgetYear(dto.getBudgetYear());
            return b;
        });

        budget.setBudgetAmount(dto.getBudgetAmount());
        return budgetRepository.save(budget);
    }

    public BigDecimal calculateRemainingBudget(BigDecimal budgetAmount, BigDecimal actualSpending) {
        if (budgetAmount == null) return BigDecimal.ZERO;
        if (actualSpending == null) actualSpending = BigDecimal.ZERO;
        return budgetAmount.subtract(actualSpending).setScale(2, RoundingMode.HALF_UP);
    }

    public double calculateBudgetUsagePercentage(BigDecimal budgetAmount, BigDecimal actualSpending) {
        if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        if (actualSpending == null || actualSpending.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        return actualSpending.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
