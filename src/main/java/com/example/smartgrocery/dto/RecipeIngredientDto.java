package com.example.smartgrocery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RecipeIngredientDto {

    private Long id;

    @NotNull(message = "Please select a grocery item")
    private Long groceryItemId;

    private String groceryItemName;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantityRequired;

    @NotBlank(message = "Unit is required")
    private String unit;

    public RecipeIngredientDto() {
    }

    public RecipeIngredientDto(Long groceryItemId, String groceryItemName, BigDecimal quantityRequired, String unit) {
        this.groceryItemId = groceryItemId;
        this.groceryItemName = groceryItemName;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroceryItemId() {
        return groceryItemId;
    }

    public void setGroceryItemId(Long groceryItemId) {
        this.groceryItemId = groceryItemId;
    }

    public String getGroceryItemName() {
        return groceryItemName;
    }

    public void setGroceryItemName(String groceryItemName) {
        this.groceryItemName = groceryItemName;
    }

    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(BigDecimal quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
