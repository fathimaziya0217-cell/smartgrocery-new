package com.example.smartgrocery.controller;

import com.example.smartgrocery.dto.MealPlanDto;
import com.example.smartgrocery.dto.MealPlanEntryDto;
import com.example.smartgrocery.entity.MealPlan;
import com.example.smartgrocery.entity.Recipe;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.exception.InsufficientStockException;
import com.example.smartgrocery.service.MealPlanService;
import com.example.smartgrocery.service.RecipeService;
import com.example.smartgrocery.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;
    private final RecipeService recipeService;
    private final UserService userService;

    public MealPlanController(MealPlanService mealPlanService,
                              RecipeService recipeService,
                              UserService userService) {
        this.mealPlanService = mealPlanService;
        this.recipeService = recipeService;
        this.userService = userService;
    }

    @GetMapping
    public String showMealPlanner(@RequestParam(value = "planId", required = false) Long planId, Model model) {
        User user = userService.getAuthenticatedUser();
        List<MealPlan> mealPlans = mealPlanService.getUserMealPlans(user.getId());
        List<Recipe> recipes = recipeService.getUserRecipes(user.getId(), null);

        MealPlan selectedPlan = null;
        if (planId != null) {
            selectedPlan = mealPlanService.getMealPlanByIdAndUser(planId, user.getId());
        } else if (!mealPlans.isEmpty()) {
            // Re-fetch with full eager initialization of entries and their recipe.ingredients
            selectedPlan = mealPlanService.getMealPlanByIdAndUser(mealPlans.get(0).getId(), user.getId());
        }

        MealPlanDto newPlanDto = new MealPlanDto();
        newPlanDto.setWeekStartDate(LocalDate.now());
        newPlanDto.setWeekEndDate(LocalDate.now().plusDays(6));

        MealPlanEntryDto newEntryDto = new MealPlanEntryDto();
        if (selectedPlan != null) {
            newEntryDto.setMealPlanId(selectedPlan.getId());
            newEntryDto.setPlannedDate(selectedPlan.getWeekStartDate());
        }

        model.addAttribute("mealPlans", mealPlans);
        model.addAttribute("selectedPlan", selectedPlan);
        model.addAttribute("recipes", recipes);
        model.addAttribute("newPlanDto", newPlanDto);
        model.addAttribute("newEntryDto", newEntryDto);

        return "meal-planner";
    }

    @PostMapping("/add")
    public String createMealPlan(@Valid @ModelAttribute("newPlanDto") MealPlanDto planDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid meal plan details.");
            return "redirect:/meal-plans";
        }

        MealPlan created = mealPlanService.createMealPlan(planDto, user);
        redirectAttributes.addFlashAttribute("successMessage", "Meal plan '" + created.getName() + "' created!");
        return "redirect:/meal-plans?planId=" + created.getId();
    }

    @PostMapping("/entry/add")
    public String addMealEntry(@Valid @ModelAttribute("newEntryDto") MealPlanEntryDto entryDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select recipe, date, and valid servings count.");
            return "redirect:/meal-plans" + (entryDto.getMealPlanId() != null ? "?planId=" + entryDto.getMealPlanId() : "");
        }

        mealPlanService.addEntry(entryDto, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Meal added to schedule!");
        return "redirect:/meal-plans?planId=" + entryDto.getMealPlanId();
    }

    @PostMapping("/entry/{id}/delete")
    public String deleteMealEntry(@PathVariable("id") Long id,
                                  @RequestParam("planId") Long planId,
                                  RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        mealPlanService.deleteEntry(id, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Scheduled meal removed.");
        return "redirect:/meal-plans?planId=" + planId;
    }

    @PostMapping("/entry/{id}/prepare")
    public String confirmPreparation(@PathVariable("id") Long id,
                                     @RequestParam("planId") Long planId,
                                     RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        try {
            mealPlanService.confirmMealPreparation(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Meal preparation confirmed! Required ingredients have been deducted from inventory.");
        } catch (InsufficientStockException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Preparation failed: " + ex.getMessage() + " - Please replenish stock or add to shopping list.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error confirming meal: " + ex.getMessage());
        }
        return "redirect:/meal-plans?planId=" + planId;
    }
}
