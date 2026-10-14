package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByUserIdOrderByNameAsc(Long userId);
    Optional<Recipe> findByIdAndUserId(Long id, Long userId);
    List<Recipe> findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(Long userId, String name);
    long countByUserId(Long userId);
}
