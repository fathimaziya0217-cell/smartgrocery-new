package com.example.smartgrocery.service;

import com.example.smartgrocery.dto.MealPlanDto;
import com.example.smartgrocery.dto.MealPlanEntryDto;
import com.example.smartgrocery.entity.*;
import com.example.smartgrocery.exception.ResourceNotFoundException;
import com.example.smartgrocery.repository.MealPlanEntryRepository;
import com.example.smartgrocery.repository.MealPlanRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealPlanEntryRepository mealPlanEntryRepository;
    private final RecipeService recipeService;
    private final InventoryService inventoryService;

    public MealPlanService(MealPlanRepository mealPlanRepository,
                           MealPlanEntryRepository mealPlanEntryRepository,
                           RecipeService recipeService,
                           InventoryService inventoryService) {
        this.mealPlanRepository = mealPlanRepository;
        this.mealPlanEntryRepository = mealPlanEntryRepository;
        this.recipeService = recipeService;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<MealPlan> getUserMealPlans(Long userId) {
        return mealPlanRepository.findByUserIdOrderByWeekStartDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public MealPlan getMealPlanByIdAndUser(Long id, Long userId) {
        MealPlan plan = mealPlanRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found with id: " + id));
        // Eagerly initialize lazy collections so they are available in the view
        Hibernate.initialize(plan.getEntries());
        for (MealPlanEntry entry : plan.getEntries()) {
            Hibernate.initialize(entry.getRecipe());
            Hibernate.initialize(entry.getRecipe().getIngredients());
        }
        return plan;
    }

    @Transactional
    public MealPlan createMealPlan(MealPlanDto dto, User user) {
        MealPlan mealPlan = new MealPlan();
        mealPlan.setUser(user);
        mealPlan.setName(dto.getName().trim());
        mealPlan.setWeekStartDate(dto.getWeekStartDate());
        mealPlan.setWeekEndDate(dto.getWeekEndDate());
        return mealPlanRepository.save(mealPlan);
    }

    @Transactional
    public MealPlanEntry addEntry(MealPlanEntryDto dto, Long userId) {
        MealPlan mealPlan = getMealPlanByIdAndUser(dto.getMealPlanId(), userId);
        Recipe recipe = recipeService.getRecipeByIdAndUser(dto.getRecipeId(), userId);

        MealPlanEntry entry = new MealPlanEntry();
        entry.setMealPlan(mealPlan);
        entry.setRecipe(recipe);
        entry.setPlannedDate(dto.getPlannedDate());
        entry.setMealType(dto.getMealType().toUpperCase());
        entry.setServings(dto.getServings() != null ? dto.getServings() : recipe.getDefaultServings());
        entry.setPreparationStatus("PLANNED");

        return mealPlanEntryRepository.save(entry);
    }

    @Transactional
    public void deleteEntry(Long entryId, Long userId) {
        MealPlanEntry entry = mealPlanEntryRepository.findByIdAndMealPlanUserId(entryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan entry not found with id: " + entryId));
        mealPlanEntryRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<MealPlanEntry> getUpcomingMeals(Long userId) {
        return mealPlanEntryRepository.findByMealPlanUserIdAndPlannedDateGreaterThanEqualOrderByPlannedDateAsc(
                userId, LocalDate.now());
    }

    // ===================================================================
    // Ingredient Aggregation Across Planned Meals
    // ===================================================================
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> calculateAggregatedIngredientRequirements(Long mealPlanId, Long userId) {
        MealPlan mealPlan = getMealPlanByIdAndUser(mealPlanId, userId);
        Map<Long, BigDecimal> requirementsMap = new HashMap<>();

        for (MealPlanEntry entry : mealPlan.getEntries()) {
            if ("CANCELLED".equalsIgnoreCase(entry.getPreparationStatus())) {
                continue;
            }
            Recipe recipe = entry.getRecipe();
            int baseServings = (recipe.getDefaultServings() != null && recipe.getDefaultServings() > 0)
                    ? recipe.getDefaultServings() : 1;
            BigDecimal scale = BigDecimal.valueOf(entry.getServings())
                    .divide(BigDecimal.valueOf(baseServings), 4, RoundingMode.HALF_UP);

            for (RecipeIngredient ing : recipe.getIngredients()) {
                GroceryItem item = ing.getGroceryItem();
                // Normalize quantity to item's base unit
                BigDecimal qtyInItemUnit = InventoryService.convertQuantity(
                        ing.getQuantityRequired(), ing.getUnit(), item.getUnit());
                BigDecimal scaledQty = qtyInItemUnit.multiply(scale);

                requirementsMap.merge(item.getId(), scaledQty, BigDecimal::add);
            }
        }
        return requirementsMap;
    }

    // ===================================================================
    // Meal Preparation Confirmation & FEFO Stock Deduction
    // ===================================================================
    @Transactional
    public void confirmMealPreparation(Long entryId, Long userId) {
        MealPlanEntry entry = mealPlanEntryRepository.findByIdAndMealPlanUserId(entryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan entry not found with id: " + entryId));

        if ("PREPARED".equalsIgnoreCase(entry.getPreparationStatus())) {
            throw new IllegalStateException("This meal has already been marked as prepared.");
        }

        Recipe recipe = entry.getRecipe();
        int baseServings = (recipe.getDefaultServings() != null && recipe.getDefaultServings() > 0)
                ? recipe.getDefaultServings() : 1;
        BigDecimal scale = BigDecimal.valueOf(entry.getServings())
                .divide(BigDecimal.valueOf(baseServings), 4, RoundingMode.HALF_UP);

        // Deduct each ingredient's required quantity from inventory
        for (RecipeIngredient ing : recipe.getIngredients()) {
            GroceryItem item = ing.getGroceryItem();
            BigDecimal qtyInItemUnit = InventoryService.convertQuantity(
                    ing.getQuantityRequired(), ing.getUnit(), item.getUnit());
            BigDecimal scaledQty = qtyInItemUnit.multiply(scale).setScale(2, RoundingMode.HALF_UP);

            inventoryService.deductStockFEFO(item.getId(), scaledQty);
        }

        entry.setPreparationStatus("PREPARED");
        mealPlanEntryRepository.save(entry);
    }
}
