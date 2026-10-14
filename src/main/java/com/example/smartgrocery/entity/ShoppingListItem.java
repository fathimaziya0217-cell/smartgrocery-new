package com.example.smartgrocery.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shopping_list_items")
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "grocery_item_id", nullable = false)
    private GroceryItem groceryItem;

    @Column(name = "quantity_required", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityRequired;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(name = "estimated_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedUnitPrice = BigDecimal.ZERO;

    @Column(name = "estimated_total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedTotalPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean purchased = false;

    @Column(name = "quantity_purchased", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityPurchased = BigDecimal.ZERO;

    public ShoppingListItem() {
    }

    public ShoppingListItem(ShoppingList shoppingList, GroceryItem groceryItem, BigDecimal quantityRequired,
                            String unit, BigDecimal estimatedUnitPrice) {
        this.shoppingList = shoppingList;
        this.groceryItem = groceryItem;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
        this.estimatedUnitPrice = estimatedUnitPrice != null ? estimatedUnitPrice : BigDecimal.ZERO;
        calculateEstimatedTotal();
    }

    public void calculateEstimatedTotal() {
        if (this.quantityRequired != null && this.estimatedUnitPrice != null) {
            this.estimatedTotalPrice = this.quantityRequired.multiply(this.estimatedUnitPrice);
        } else {
            this.estimatedTotalPrice = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ShoppingList getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    public GroceryItem getGroceryItem() {
        return groceryItem;
    }

    public void setGroceryItem(GroceryItem groceryItem) {
        this.groceryItem = groceryItem;
    }

    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(BigDecimal quantityRequired) {
        this.quantityRequired = quantityRequired;
        calculateEstimatedTotal();
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
        calculateEstimatedTotal();
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
