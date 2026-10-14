package com.example.smartgrocery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class MealPlanEntryDto {

    private Long id;

    @NotNull(message = "Please select a meal plan")
    private Long mealPlanId;

    @NotNull(message = "Please select a recipe")
    private Long recipeId;

    private String recipeName;

    @NotNull(message = "Planned date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate plannedDate;

    @NotBlank(message = "Meal type is required")
    private String mealType; // BREAKFAST, LUNCH, DINNER, SNACK

    @NotNull(message = "Servings count is required")
    @Min(value = 1, message = "Servings must be at least 1")
    private Integer servings = 2;

    private String preparationStatus = "PLANNED";

    public MealPlanEntryDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMealPlanId() {
        return mealPlanId;
    }

    public void setMealPlanId(Long mealPlanId) {
        this.mealPlanId = mealPlanId;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public LocalDate getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(LocalDate plannedDate) {
        this.plannedDate = plannedDate;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public String getPreparationStatus() {
        return preparationStatus;
    }

    public void setPreparationStatus(String preparationStatus) {
        this.preparationStatus = preparationStatus;
    }
}
