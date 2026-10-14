package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    List<ShoppingList> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ShoppingList> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
