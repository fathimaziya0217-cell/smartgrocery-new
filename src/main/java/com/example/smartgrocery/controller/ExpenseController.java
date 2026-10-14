package com.example.smartgrocery.controller;

import com.example.smartgrocery.dto.ExpenseDto;
import com.example.smartgrocery.entity.Expense;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.service.ExpenseService;
import com.example.smartgrocery.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;

    public ExpenseController(ExpenseService expenseService, UserService userService) {
        this.expenseService = expenseService;
        this.userService = userService;
    }

    @GetMapping
    public String listExpenses(@RequestParam(value = "month", required = false) Integer month,
                               @RequestParam(value = "year", required = false) Integer year,
                               Model model) {
        User user = userService.getAuthenticatedUser();
        LocalDate now = LocalDate.now();
        int selectedMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();
        int selectedYear = (year != null && year >= 2020) ? year : now.getYear();

        List<Expense> expenses = expenseService.getExpensesForMonth(user.getId(), selectedMonth, selectedYear);
        BigDecimal totalMonthExpenses = expenseService.sumExpensesForMonth(user.getId(), selectedMonth, selectedYear);

        ExpenseDto newExpenseDto = new ExpenseDto();
        newExpenseDto.setExpenseDate(LocalDate.now());

        model.addAttribute("expenses", expenses);
        model.addAttribute("totalMonthExpenses", totalMonthExpenses);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("expenseDto", newExpenseDto);

        return "expenses";
    }

    @PostMapping("/add")
    public String addExpense(@Valid @ModelAttribute("expenseDto") ExpenseDto expenseDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid amount and description.");
            return "redirect:/expenses";
        }

        expenseService.recordExpense(expenseDto, user);
        redirectAttributes.addFlashAttribute("successMessage", "Expense recorded successfully!");
        return "redirect:/expenses";
    }

    @PostMapping("/delete/{id}")
    public String deleteExpense(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        expenseService.deleteExpense(id, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Expense entry removed.");
        return "redirect:/expenses";
    }
}
