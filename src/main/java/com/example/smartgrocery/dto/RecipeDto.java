package com.example.smartgrocery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class RecipeDto {

    private Long id;

    @NotBlank(message = "Recipe name is required")
    @Size(max = 150, message = "Recipe name cannot exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Default servings count is required")
    @Min(value = 1, message = "Servings must be at least 1")
    private Integer defaultServings = 2;

    private String preparationInstructions;

    private List<RecipeIngredientDto> ingredients = new ArrayList<>();

    public RecipeDto() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDefaultServings() {
        return defaultServings;
    }

    public void setDefaultServings(Integer defaultServings) {
        this.defaultServings = defaultServings;
    }

    public String getPreparationInstructions() {
        return preparationInstructions;
    }

    public void setPreparationInstructions(String preparationInstructions) {
        this.preparationInstructions = preparationInstructions;
    }

    public List<RecipeIngredientDto> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredientDto> ingredients) {
        this.ingredients = ingredients;
    }
}
