package com.example.smartgrocery;

import com.example.smartgrocery.dto.BudgetDto;
import com.example.smartgrocery.dto.GroceryItemDto;
import com.example.smartgrocery.dto.UserRegistrationDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should detect invalid email in user registration")
    void testInvalidEmailRegistration() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("John Doe");
        dto.setEmail("invalid-email-string");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");

        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Should reject password shorter than 6 characters")
    void testShortPasswordRegistration() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("John Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("123");
        dto.setConfirmPassword("123");

        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    @DisplayName("Should reject negative minimum stock and negative price for grocery item")
    void testNegativePriceAndStockRejection() {
        GroceryItemDto dto = new GroceryItemDto();
        dto.setName("Flour");
        dto.setCategoryId(1L);
        dto.setUnit("kg");
        dto.setMinimumStock(new BigDecimal("-1.00"));
        dto.setEstimatedUnitPrice(new BigDecimal("-50.00"));

        Set<ConstraintViolation<GroceryItemDto>> violations = validator.validate(dto);
        assertEquals(2, violations.size());
    }

    @Test
    @DisplayName("Should reject invalid month greater than 12 in budget")
    void testInvalidMonthBudget() {
        BudgetDto dto = new BudgetDto();
        dto.setBudgetMonth(13);
        dto.setBudgetYear(2026);
        dto.setBudgetAmount(new BigDecimal("5000.00"));

        Set<ConstraintViolation<BudgetDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("budgetMonth")));
    }
}
