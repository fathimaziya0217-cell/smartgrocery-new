package com.example.smartgrocery.service;

import com.example.smartgrocery.dto.ExpenseDto;
import com.example.smartgrocery.entity.Expense;
import com.example.smartgrocery.entity.ShoppingListItem;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.exception.ResourceNotFoundException;
import com.example.smartgrocery.repository.ExpenseRepository;
import com.example.smartgrocery.repository.ShoppingListItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          ShoppingListItemRepository shoppingListItemRepository) {
        this.expenseRepository = expenseRepository;
        this.shoppingListItemRepository = shoppingListItemRepository;
    }

    @Transactional(readOnly = true)
    public List<Expense> getUserExpenses(Long userId) {
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public Expense getExpenseByIdAndUser(Long id, Long userId) {
        return expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    @Transactional
    public Expense recordExpense(ExpenseDto dto, User user) {
        Expense expense = new Expense();
        expense.setUser(user);
        expense.setAmount(dto.getAmount());
        expense.setExpenseDate(dto.getExpenseDate() != null ? dto.getExpenseDate() : LocalDate.now());
        expense.setDescription(dto.getDescription().trim());

        if (dto.getShoppingListItemId() != null) {
            ShoppingListItem item = shoppingListItemRepository.findByIdAndShoppingListUserId(dto.getShoppingListItemId(), user.getId())
                    .orElse(null);
            expense.setShoppingListItem(item);
        }

        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense saveExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long id, Long userId) {
        Expense expense = getExpenseByIdAndUser(id, userId);
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumExpensesForMonth(Long userId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return expenseRepository.sumExpensesForUserBetweenDates(userId, start, end);
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpensesForMonth(Long userId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        return expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(userId, ym.atDay(1), ym.atEndOfMonth());
    }
}
