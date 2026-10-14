package com.example.smartgrocery.controller;

import com.example.smartgrocery.dto.CategoryDto;
import com.example.smartgrocery.entity.GroceryCategory;
import com.example.smartgrocery.exception.DuplicateResourceException;
import com.example.smartgrocery.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        List<GroceryCategory> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("categoryDto", new CategoryDto());
        return "categories";
    }

    @PostMapping("/add")
    public String addCategory(@Valid @ModelAttribute("categoryDto") CategoryDto categoryDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "categories";
        }

        try {
            categoryService.createCategory(categoryDto);
            redirectAttributes.addFlashAttribute("successMessage", "Category '" + categoryDto.getName() + "' created successfully!");
        } catch (DuplicateResourceException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete category because grocery items are currently assigned to it.");
        }
        return "redirect:/categories";
    }
}
