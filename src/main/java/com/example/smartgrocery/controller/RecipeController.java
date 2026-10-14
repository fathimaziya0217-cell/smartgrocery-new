package com.example.smartgrocery.controller;

import com.example.smartgrocery.dto.RecipeDto;
import com.example.smartgrocery.dto.RecipeIngredientDto;
import com.example.smartgrocery.entity.GroceryItem;
import com.example.smartgrocery.entity.Recipe;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.service.InventoryService;
import com.example.smartgrocery.service.RecipeService;
import com.example.smartgrocery.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final InventoryService inventoryService;
    private final UserService userService;

    public RecipeController(RecipeService recipeService,
                            InventoryService inventoryService,
                            UserService userService) {
        this.recipeService = recipeService;
        this.inventoryService = inventoryService;
        this.userService = userService;
    }

    @GetMapping
    public String listRecipes(@RequestParam(value = "q", required = false) String search, Model model) {
        User user = userService.getAuthenticatedUser();
        List<Recipe> recipes = recipeService.getUserRecipes(user.getId(), search);
        List<GroceryItem> userItems = inventoryService.getUserItems(user.getId(), null, null);

        model.addAttribute("recipes", recipes);
        model.addAttribute("searchQuery", search);
        model.addAttribute("userItems", userItems);
        model.addAttribute("recipeDto", new RecipeDto());

        return "recipes";
    }

    @GetMapping("/{id}")
    public String viewRecipe(@PathVariable("id") Long id,
                             @RequestParam(value = "servings", required = false) Integer targetServings,
                             Model model) {
        User user = userService.getAuthenticatedUser();
        Recipe recipe = recipeService.getRecipeByIdAndUser(id, user.getId());

        int servings = (targetServings != null && targetServings > 0) ? targetServings : recipe.getDefaultServings();
        List<RecipeIngredientDto> scaledIngredients = recipeService.calculateScaledIngredients(recipe, servings);

        model.addAttribute("recipe", recipe);
        model.addAttribute("servings", servings);
        model.addAttribute("scaledIngredients", scaledIngredients);

        return "recipes";
    }

    @PostMapping("/add")
    public String createRecipe(@Valid @ModelAttribute("recipeDto") RecipeDto recipeDto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            model.addAttribute("recipes", recipeService.getUserRecipes(user.getId(), null));
            model.addAttribute("userItems", inventoryService.getUserItems(user.getId(), null, null));
            return "recipes";
        }

        recipeService.createRecipe(recipeDto, user);
        redirectAttributes.addFlashAttribute("successMessage", "Recipe '" + recipeDto.getName() + "' created successfully!");
        return "redirect:/recipes";
    }

    @PostMapping("/delete/{id}")
    public String deleteRecipe(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        try {
            recipeService.deleteRecipe(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Recipe deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete recipe because it is currently scheduled in active meal plans.");
        }
        return "redirect:/recipes";
    }
}
