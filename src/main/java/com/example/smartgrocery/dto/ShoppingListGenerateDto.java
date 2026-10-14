package com.example.smartgrocery.dto;

import jakarta.validation.constraints.NotBlank;

public class ShoppingListGenerateDto {

    @NotBlank(message = "Shopping list name is required")
    private String name = "Weekly Generated Shopping List";

    private Long mealPlanId;

    private boolean includeLowStock = true;

    public ShoppingListGenerateDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMealPlanId() {
        return mealPlanId;
    }

    public void setMealPlanId(Long mealPlanId) {
        this.mealPlanId = mealPlanId;
    }

    public boolean isIncludeLowStock() {
        return includeLowStock;
    }

    public void setIncludeLowStock(boolean includeLowStock) {
        this.includeLowStock = includeLowStock;
    }
}
