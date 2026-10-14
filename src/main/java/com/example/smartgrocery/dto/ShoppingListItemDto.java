package com.example.smartgrocery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ShoppingListItemDto {

    private Long id;

    @NotNull(message = "Grocery item ID is required")
    private Long groceryItemId;

    private String groceryItemName;
    private String categoryName;

    @NotNull(message = "Quantity required must be specified")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantityRequired;

    private String unit;

    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    private BigDecimal estimatedUnitPrice = BigDecimal.ZERO;

    private BigDecimal estimatedTotalPrice = BigDecimal.ZERO;

    private boolean purchased = false;

    @DecimalMin(value = "0.00", message = "Quantity purchased cannot be negative")
    private BigDecimal quantityPurchased = BigDecimal.ZERO;

    public ShoppingListItemDto() {
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public BigDecimal getEstimatedUnitPrice() {
        return estimatedUnitPrice;
    }

    public void setEstimatedUnitPrice(BigDecimal estimatedUnitPrice) {
        this.estimatedUnitPrice = estimatedUnitPrice;
    }

    public BigDecimal getEstimatedTotalPrice() {
        return estimatedTotalPrice;
    }

    public void setEstimatedTotalPrice(BigDecimal estimatedTotalPrice) {
        this.estimatedTotalPrice = estimatedTotalPrice;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public BigDecimal getQuantityPurchased() {
        return quantityPurchased;
    }

    public void setQuantityPurchased(BigDecimal quantityPurchased) {
        this.quantityPurchased = quantityPurchased;
    }
}
