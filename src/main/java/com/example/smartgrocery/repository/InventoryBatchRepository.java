package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {

    List<InventoryBatch> findByGroceryItemIdOrderByExpiryDateAscPurchaseDateAsc(Long groceryItemId);

    Optional<InventoryBatch> findByIdAndGroceryItemUserId(Long id, Long userId);

    @Query("SELECT b FROM InventoryBatch b WHERE b.groceryItem.id = :itemId AND b.quantityRemaining > 0 " +
           "AND (b.expiryDate IS NULL OR b.expiryDate >= :today) " +
           "ORDER BY CASE WHEN b.expiryDate IS NULL THEN 1 ELSE 0 END, b.expiryDate ASC, b.purchaseDate ASC")
    List<InventoryBatch> findUsableBatchesForItem(@Param("itemId") Long itemId, @Param("today") LocalDate today);

    @Query("SELECT b FROM InventoryBatch b WHERE b.groceryItem.user.id = :userId AND b.quantityRemaining > 0 " +
           "AND b.expiryDate IS NOT NULL AND b.expiryDate >= :today AND b.expiryDate <= :thresholdDate " +
           "ORDER BY b.expiryDate ASC")
    List<InventoryBatch> findNearExpiryBatches(@Param("userId") Long userId,
                                               @Param("today") LocalDate today,
                                               @Param("thresholdDate") LocalDate thresholdDate);

    @Query("SELECT b FROM InventoryBatch b WHERE b.groceryItem.user.id = :userId AND b.quantityRemaining > 0 " +
           "AND b.expiryDate IS NOT NULL AND b.expiryDate < :today ORDER BY b.expiryDate ASC")
    List<InventoryBatch> findExpiredBatches(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT b FROM InventoryBatch b WHERE b.groceryItem.user.id = :userId ORDER BY b.purchaseDate DESC")
    List<InventoryBatch> findAllBatchesForUser(@Param("userId") Long userId);
}
