package com.example.smartgrocery.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "scan_import_items")
public class ScanImportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_import_id", nullable = false)
    private ScanImport scanImport;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grocery_item_id")
    private GroceryItem groceryItem;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "processing_status", nullable = false, length = 50)
    private String processingStatus = "PENDING"; // PENDING, COMPLETED

    public ScanImportItem() {
    }

    public ScanImportItem(ScanImport scanImport, GroceryItem groceryItem, String itemName, BigDecimal quantity, String unit, BigDecimal price) {
        this.scanImport = scanImport;
        this.groceryItem = groceryItem;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.processingStatus = "PENDING";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ScanImport getScanImport() {
        return scanImport;
    }

    public void setScanImport(ScanImport scanImport) {
        this.scanImport = scanImport;
    }

    public GroceryItem getGroceryItem() {
        return groceryItem;
    }

    public void setGroceryItem(GroceryItem groceryItem) {
        this.groceryItem = groceryItem;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
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

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }
}
