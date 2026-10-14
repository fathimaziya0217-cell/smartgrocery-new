package com.example.smartgrocery.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "meal_plan_entries")
public class MealPlanEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "meal_type", nullable = false, length = 50)
    private String mealType; // BREAKFAST, LUNCH, DINNER, SNACK

    @Column(nullable = false)
    private Integer servings = 2;

    @Column(name = "preparation_status", nullable = false, length = 50)
    private String preparationStatus = "PLANNED"; // PLANNED, PREPARED, CANCELLED

    public MealPlanEntry() {
    }

    public MealPlanEntry(MealPlan mealPlan, Recipe recipe, LocalDate plannedDate, String mealType, Integer servings) {
        this.mealPlan = mealPlan;
        this.recipe = recipe;
        this.plannedDate = plannedDate;
        this.mealType = mealType;
        this.servings = servings != null ? servings : 2;
        this.preparationStatus = "PLANNED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MealPlan getMealPlan() {
        return mealPlan;
    }

    public void setMealPlan(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
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
