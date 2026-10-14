package com.example.smartgrocery.service;

import com.example.smartgrocery.dto.RecipeDto;
import com.example.smartgrocery.dto.RecipeIngredientDto;
import com.example.smartgrocery.entity.GroceryItem;
import com.example.smartgrocery.entity.Recipe;
import com.example.smartgrocery.entity.RecipeIngredient;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.exception.ResourceNotFoundException;
import com.example.smartgrocery.repository.GroceryItemRepository;
import com.example.smartgrocery.repository.RecipeIngredientRepository;
import com.example.smartgrocery.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final GroceryItemRepository groceryItemRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         RecipeIngredientRepository recipeIngredientRepository,
                         GroceryItemRepository groceryItemRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.groceryItemRepository = groceryItemRepository;
    }

    @Transactional(readOnly = true)
    public List<Recipe> getUserRecipes(Long userId, String searchQuery) {
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            return recipeRepository.findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(userId, searchQuery.trim());
        }
        return recipeRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional(readOnly = true)
    public Recipe getRecipeByIdAndUser(Long id, Long userId) {
        return recipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));
    }

    @Transactional
    public Recipe createRecipe(RecipeDto dto, User user) {
        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setName(dto.getName().trim());
        recipe.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        recipe.setDefaultServings(dto.getDefaultServings() != null ? dto.getDefaultServings() : 2);
        recipe.setPreparationInstructions(dto.getPreparationInstructions());

        Recipe savedRecipe = recipeRepository.save(recipe);

        if (dto.getIngredients() != null) {
            for (RecipeIngredientDto ingDto : dto.getIngredients()) {
                if (ingDto.getGroceryItemId() != null && ingDto.getQuantityRequired() != null) {
                    GroceryItem item = groceryItemRepository.findByIdAndUserId(ingDto.getGroceryItemId(), user.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Grocery item not found with id: " + ingDto.getGroceryItemId()));

                    RecipeIngredient ingredient = new RecipeIngredient();
                    ingredient.setRecipe(savedRecipe);
                    ingredient.setGroceryItem(item);
                    ingredient.setQuantityRequired(ingDto.getQuantityRequired());
                    ingredient.setUnit(ingDto.getUnit() != null ? ingDto.getUnit().trim().toLowerCase() : item.getUnit());
                    recipeIngredientRepository.save(ingredient);
                }
            }
        }

        return savedRecipe;
    }

    @Transactional
    public Recipe updateRecipe(Long id, RecipeDto dto, Long userId) {
        Recipe recipe = getRecipeByIdAndUser(id, userId);
        recipe.setName(dto.getName().trim());
        recipe.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        recipe.setDefaultServings(dto.getDefaultServings() != null ? dto.getDefaultServings() : 2);
        recipe.setPreparationInstructions(dto.getPreparationInstructions());

        // Replace ingredients
        recipeIngredientRepository.deleteByRecipeId(recipe.getId());

        if (dto.getIngredients() != null) {
            for (RecipeIngredientDto ingDto : dto.getIngredients()) {
                if (ingDto.getGroceryItemId() != null && ingDto.getQuantityRequired() != null) {
                    GroceryItem item = groceryItemRepository.findByIdAndUserId(ingDto.getGroceryItemId(), userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Grocery item not found with id: " + ingDto.getGroceryItemId()));

                    RecipeIngredient ingredient = new RecipeIngredient();
                    ingredient.setRecipe(recipe);
                    ingredient.setGroceryItem(item);
                    ingredient.setQuantityRequired(ingDto.getQuantityRequired());
                    ingredient.setUnit(ingDto.getUnit() != null ? ingDto.getUnit().trim().toLowerCase() : item.getUnit());
                    recipeIngredientRepository.save(ingredient);
                }
            }
        }

        return recipeRepository.save(recipe);
    }

    @Transactional
    public void deleteRecipe(Long id, Long userId) {
        Recipe recipe = getRecipeByIdAndUser(id, userId);
        recipeRepository.delete(recipe);
    }

    // Helper for scaling ingredients based on target servings
    public List<RecipeIngredientDto> calculateScaledIngredients(Recipe recipe, int targetServings) {
        List<RecipeIngredientDto> scaledList = new ArrayList<>();
        int baseServings = (recipe.getDefaultServings() != null && recipe.getDefaultServings() > 0) ? recipe.getDefaultServings() : 1;
        BigDecimal scaleFactor = BigDecimal.valueOf(targetServings).divide(BigDecimal.valueOf(baseServings), 4, RoundingMode.HALF_UP);

        for (RecipeIngredient ing : recipe.getIngredients()) {
            BigDecimal scaledQty = ing.getQuantityRequired().multiply(scaleFactor).setScale(2, RoundingMode.HALF_UP);
            scaledList.add(new RecipeIngredientDto(
                    ing.getGroceryItem().getId(),
                    ing.getGroceryItem().getName(),
                    scaledQty,
                    ing.getUnit()
            ));
        }
        return scaledList;
    }
}
