package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.GroceryCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroceryCategoryRepository extends JpaRepository<GroceryCategory, Long> {
    Optional<GroceryCategory> findByName(String name);
    Optional<GroceryCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<GroceryCategory> findAllByOrderByNameAsc();
}
