package com.example.smartgrocery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GroceryItemDto {

    private Long id;

    @NotNull(message = "Please select a grocery category")
    private Long categoryId;

    @NotBlank(message = "Item name is required")
    @Size(max = 150, message = "Item name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Unit is required")
    private String unit; // kg, g, litre, ml, piece

    @NotNull(message = "Minimum stock is required")
    @DecimalMin(value = "0.00", message = "Minimum stock cannot be negative")
    private BigDecimal minimumStock = BigDecimal.ZERO;

    @NotNull(message = "Estimated unit price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    private BigDecimal estimatedUnitPrice = BigDecimal.ZERO;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    // Optional initial batch fields on item creation
    @DecimalMin(value = "0.00", message = "Initial quantity cannot be negative")
    private BigDecimal initialQuantity = BigDecimal.ZERO;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate initialExpiryDate;

    public GroceryItemDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(BigDecimal minimumStock) {
        this.minimumStock = minimumStock;
    }

    public BigDecimal getEstimatedUnitPrice() {
        return estimatedUnitPrice;
    }

    public void setEstimatedUnitPrice(BigDecimal estimatedUnitPrice) {
        this.estimatedUnitPrice = estimatedUnitPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(BigDecimal initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public LocalDate getInitialExpiryDate() {
        return initialExpiryDate;
    }

    public void setInitialExpiryDate(LocalDate initialExpiryDate) {
        this.initialExpiryDate = initialExpiryDate;
    }
}
