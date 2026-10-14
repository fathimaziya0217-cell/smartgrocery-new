package com.example.smartgrocery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReceiptImportDto {

    private String storeName = "Supermarket";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchaseDate = LocalDate.now();

    private boolean recordExpense = true;

    private List<ReceiptItemDto> items = new ArrayList<>();

    public ReceiptImportDto() {
    }

    public BigDecimal calculateTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (ReceiptItemDto item : items) {
            if (item.getPrice() != null && item.getQuantity() != null) {
                total = total.add(item.getPrice().multiply(item.getQuantity()));
            }
        }
        return total;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public boolean isRecordExpense() {
        return recordExpense;
    }

    public void setRecordExpense(boolean recordExpense) {
        this.recordExpense = recordExpense;
    }

    public List<ReceiptItemDto> getItems() {
        return items;
    }

    public void setItems(List<ReceiptItemDto> items) {
        this.items = items;
    }

    public static class ReceiptItemDto {
        @NotBlank(message = "Item name is required")
        private String itemName;

        private Long groceryItemId;

        private String categoryName = "Other";

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
        private BigDecimal quantity = BigDecimal.ONE;

        private String unit = "piece";

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        private BigDecimal price = BigDecimal.ZERO;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate expiryDate;

        public ReceiptItemDto() {
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public Long getGroceryItemId() {
            return groceryItemId;
        }

        public void setGroceryItemId(Long groceryItemId) {
            this.groceryItemId = groceryItemId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public LocalDate getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(LocalDate expiryDate) {
            this.expiryDate = expiryDate;
        }
    }
}
