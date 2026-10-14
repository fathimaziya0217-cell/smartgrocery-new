package com.example.smartgrocery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MealPlanDto {

    private Long id;

    @NotBlank(message = "Meal plan name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    @NotNull(message = "Week start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate weekStartDate = LocalDate.now();

    @NotNull(message = "Week end date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate weekEndDate = LocalDate.now().plusDays(6);

    private List<MealPlanEntryDto> entries = new ArrayList<>();

    public MealPlanDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public LocalDate getWeekEndDate() {
        return weekEndDate;
    }

    public void setWeekEndDate(LocalDate weekEndDate) {
        this.weekEndDate = weekEndDate;
    }

    public List<MealPlanEntryDto> getEntries() {
        return entries;
    }

    public void setEntries(List<MealPlanEntryDto> entries) {
        this.entries = entries;
    }
}
