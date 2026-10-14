package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.GroceryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroceryItemRepository extends JpaRepository<GroceryItem, Long> {
    List<GroceryItem> findByUserIdOrderByNameAsc(Long userId);
    Optional<GroceryItem> findByIdAndUserId(Long id, Long userId);
    List<GroceryItem> findByUserIdAndCategoryIdOrderByNameAsc(Long userId, Long categoryId);
    List<GroceryItem> findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(Long userId, String name);
    long countByUserId(Long userId);

    @Query("SELECT g FROM GroceryItem g WHERE g.user.id = :userId AND " +
           "(SELECT COALESCE(SUM(b.quantityRemaining), 0) FROM InventoryBatch b " +
           " WHERE b.groceryItem = g AND b.quantityRemaining > 0 AND (b.expiryDate IS NULL OR b.expiryDate >= CURRENT_DATE)) <= g.minimumStock")
    List<GroceryItem> findLowStockItems(@Param("userId") Long userId);
}
