package com.example.smartgrocery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BarcodeScanDto {

    @NotBlank(message = "Barcode is required")
    private String barcode;

    private String detectedName;
    private String detectedCategory;
    private String detectedUnit;
    private BigDecimal detectedPrice = BigDecimal.ZERO;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity = BigDecimal.ONE;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDate = LocalDate.now().plusDays(30);

    public BarcodeScanDto() {
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDetectedName() {
        return detectedName;
    }

    public void setDetectedName(String detectedName) {
        this.detectedName = detectedName;
    }

    public String getDetectedCategory() {
        return detectedCategory;
    }

    public void setDetectedCategory(String detectedCategory) {
        this.detectedCategory = detectedCategory;
    }

    public String getDetectedUnit() {
        return detectedUnit;
    }

    public void setDetectedUnit(String detectedUnit) {
        this.detectedUnit = detectedUnit;
    }

    public BigDecimal getDetectedPrice() {
        return detectedPrice;
    }

    public void setDetectedPrice(BigDecimal detectedPrice) {
        this.detectedPrice = detectedPrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}
