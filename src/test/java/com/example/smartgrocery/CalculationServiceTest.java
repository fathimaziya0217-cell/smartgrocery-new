package com.example.smartgrocery;

import com.example.smartgrocery.dto.RecipeIngredientDto;
import com.example.smartgrocery.entity.*;
import com.example.smartgrocery.service.BudgetService;
import com.example.smartgrocery.service.RecipeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculationServiceTest {

    @Test
    @DisplayName("Should scale ingredients proportionally from 2 to 4 servings")
    void testServingSizeScaling() {
        Recipe recipe = new Recipe();
        recipe.setName("Fried Rice");
        recipe.setDefaultServings(2);

        GroceryItem item = new GroceryItem();
        item.setId(1L);
        item.setName("Basmati Rice");
        item.setUnit("g");

        RecipeIngredient ing = new RecipeIngredient(recipe, item, new BigDecimal("200.00"), "g");
        recipe.getIngredients().add(ing);

        RecipeService recipeService = new RecipeService(null, null, null);
        List<RecipeIngredientDto> scaled = recipeService.calculateScaledIngredients(recipe, 4);

        assertEquals(1, scaled.size());
        assertEquals(0, new BigDecimal("400.00").compareTo(scaled.get(0).getQuantityRequired()));
    }

    @Test
    @DisplayName("Should correctly calculate remaining budget and usage percentage")
    void testBudgetCalculations() {
        BudgetService budgetService = new BudgetService(null, null);

        BigDecimal budget = new BigDecimal("5000.00");
        BigDecimal spent = new BigDecimal("2000.00");

        BigDecimal remaining = budgetService.calculateRemainingBudget(budget, spent);
        assertEquals(0, new BigDecimal("3000.00").compareTo(remaining));

        double usage = budgetService.calculateBudgetUsagePercentage(budget, spent);
        assertEquals(40.0, usage, 0.01);
    }

    @Test
    @DisplayName("Should safely handle zero budget without division by zero")
    void testZeroBudgetSafeguard() {
        BudgetService budgetService = new BudgetService(null, null);

        double usage = budgetService.calculateBudgetUsagePercentage(BigDecimal.ZERO, new BigDecimal("100.00"));
        assertEquals(0.0, usage);
    }

    @Test
    @DisplayName("Should calculate total estimated cost accurately using BigDecimal")
    void testCostEstimationCalculation() {
        ShoppingList list = new ShoppingList();

        GroceryItem item1 = new GroceryItem();
        item1.setName("Milk");

        GroceryItem item2 = new GroceryItem();
        item2.setName("Apples");

        ShoppingListItem sli1 = new ShoppingListItem(list, item1, new BigDecimal("2.50"), "litre", new BigDecimal("65.00"));
        ShoppingListItem sli2 = new ShoppingListItem(list, item2, new BigDecimal("1.50"), "kg", new BigDecimal("120.00"));

        list.getItems().add(sli1);
        list.getItems().add(sli2);

        BigDecimal total = list.calculateEstimatedTotal();
        // 2.5 * 65 = 162.50 ; 1.5 * 120 = 180.00 ; Total = 342.50
        assertEquals(0, new BigDecimal("342.50").compareTo(total));
    }
}
