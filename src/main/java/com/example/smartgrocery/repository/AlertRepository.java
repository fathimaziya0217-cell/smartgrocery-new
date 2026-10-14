package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserIdAndResolvedFalseOrderByCreatedAtDesc(Long userId);
    List<Alert> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Alert> findByIdAndUserId(Long id, Long userId);
    long countByUserIdAndResolvedFalse(Long userId);
    boolean existsByUserIdAndGroceryItemIdAndAlertTypeAndResolvedFalse(Long userId, Long groceryItemId, String alertType);
    boolean existsByUserIdAndAlertTypeAndResolvedFalse(Long userId, String alertType);
}
