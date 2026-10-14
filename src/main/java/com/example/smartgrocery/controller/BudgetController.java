package com.example.smartgrocery.controller;

import com.example.smartgrocery.dto.BudgetDto;
import com.example.smartgrocery.entity.Budget;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.service.BudgetService;
import com.example.smartgrocery.service.ExpenseService;
import com.example.smartgrocery.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final ExpenseService expenseService;
    private final UserService userService;

    public BudgetController(BudgetService budgetService,
                            ExpenseService expenseService,
                            UserService userService) {
        this.budgetService = budgetService;
        this.expenseService = expenseService;
        this.userService = userService;
    }

    @GetMapping
    public String showBudgets(Model model) {
        User user = userService.getAuthenticatedUser();
        LocalDate now = LocalDate.now();

        List<Budget> budgets = budgetService.getUserBudgets(user.getId());
        Optional<Budget> currentBudgetOpt = budgetService.getCurrentMonthBudget(user.getId());

        BigDecimal currentBudgetAmount = currentBudgetOpt.map(Budget::getBudgetAmount).orElse(BigDecimal.ZERO);
        BigDecimal actualSpending = expenseService.sumExpensesForMonth(user.getId(), now.getMonthValue(), now.getYear());
        BigDecimal remainingBudget = budgetService.calculateRemainingBudget(currentBudgetAmount, actualSpending);
        double usagePercentage = budgetService.calculateBudgetUsagePercentage(currentBudgetAmount, actualSpending);

        BudgetDto budgetDto = new BudgetDto();
        budgetDto.setBudgetMonth(now.getMonthValue());
        budgetDto.setBudgetYear(now.getYear());
        if (currentBudgetOpt.isPresent()) {
            budgetDto.setBudgetAmount(currentBudgetOpt.get().getBudgetAmount());
        }

        model.addAttribute("budgets", budgets);
        model.addAttribute("currentBudget", currentBudgetOpt.orElse(null));
        model.addAttribute("currentBudgetAmount", currentBudgetAmount);
        model.addAttribute("actualSpending", actualSpending);
        model.addAttribute("remainingBudget", remainingBudget);
        model.addAttribute("usagePercentage", usagePercentage);
        model.addAttribute("budgetDto", budgetDto);

        return "budget";
    }

    @PostMapping("/save")
    public String saveBudget(@Valid @ModelAttribute("budgetDto") BudgetDto budgetDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid budget amount, month, and year.");
            return "redirect:/budgets";
        }

        budgetService.saveOrUpdateBudget(budgetDto, user);
        redirectAttributes.addFlashAttribute("successMessage", "Monthly budget allocated successfully!");
        return "redirect:/budgets";
    }
}
