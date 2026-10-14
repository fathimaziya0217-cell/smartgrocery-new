package com.example.smartgrocery;

import com.example.smartgrocery.exception.InvalidUnitException;
import com.example.smartgrocery.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UnitConversionTest {

    @Test
    @DisplayName("Should convert 2.5 kg to 2500 g correctly")
    void testKgToGConversion() {
        BigDecimal result = InventoryService.convertQuantity(new BigDecimal("2.5"), "kg", "g");
        assertEquals(0, new BigDecimal("2500.0").compareTo(result));
    }

    @Test
    @DisplayName("Should convert 500 g to 0.5 kg correctly")
    void testGToKgConversion() {
        BigDecimal result = InventoryService.convertQuantity(new BigDecimal("500"), "g", "kg");
        assertEquals(0, new BigDecimal("0.5").compareTo(result));
    }

    @Test
    @DisplayName("Should convert 1.5 litre to 1500 ml correctly")
    void testLitreToMlConversion() {
        BigDecimal result = InventoryService.convertQuantity(new BigDecimal("1.5"), "litre", "ml");
        assertEquals(0, new BigDecimal("1500.0").compareTo(result));
    }

    @Test
    @DisplayName("Should convert 250 ml to 0.25 litre correctly")
    void testMlToLitreConversion() {
        BigDecimal result = InventoryService.convertQuantity(new BigDecimal("250"), "ml", "litre");
        assertEquals(0, new BigDecimal("0.25").compareTo(result));
    }

    @Test
    @DisplayName("Should return exact quantity when units match")
    void testIdenticalUnitsConversion() {
        BigDecimal result = InventoryService.convertQuantity(new BigDecimal("4"), "piece", "piece");
        assertEquals(new BigDecimal("4"), result);
    }

    @Test
    @DisplayName("Should reject incompatible unit conversions (e.g. kg to litre)")
    void testIncompatibleUnitConversionThrowsException() {
        assertThrows(InvalidUnitException.class, () ->
                InventoryService.convertQuantity(new BigDecimal("2"), "kg", "litre")
        );
    }

    @Test
    @DisplayName("Should reject count to weight conversion (e.g. piece to kg)")
    void testPieceToKgThrowsException() {
        assertThrows(InvalidUnitException.class, () ->
                InventoryService.convertQuantity(new BigDecimal("5"), "piece", "kg")
        );
    }
}
