package com.example.smartgrocery.service;

import com.example.smartgrocery.dto.CategoryDto;
import com.example.smartgrocery.entity.GroceryCategory;
import com.example.smartgrocery.exception.DuplicateResourceException;
import com.example.smartgrocery.exception.ResourceNotFoundException;
import com.example.smartgrocery.repository.GroceryCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final GroceryCategoryRepository categoryRepository;

    public CategoryService(GroceryCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<GroceryCategory> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public GroceryCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public GroceryCategory getCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
    }

    @Transactional
    public GroceryCategory createCategory(CategoryDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new DuplicateResourceException("Category '" + dto.getName().trim() + "' already exists");
        }
        GroceryCategory category = new GroceryCategory();
        category.setName(dto.getName().trim());
        category.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        GroceryCategory category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}
