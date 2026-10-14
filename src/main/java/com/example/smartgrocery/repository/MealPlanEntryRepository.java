package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.MealPlanEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealPlanEntryRepository extends JpaRepository<MealPlanEntry, Long> {
    List<MealPlanEntry> findByMealPlanIdOrderByPlannedDateAsc(Long mealPlanId);
    Optional<MealPlanEntry> findByIdAndMealPlanUserId(Long id, Long userId);
    List<MealPlanEntry> findByMealPlanUserIdAndPlannedDateBetweenOrderByPlannedDateAsc(Long userId, LocalDate startDate, LocalDate endDate);
    List<MealPlanEntry> findByMealPlanUserIdAndPlannedDateGreaterThanEqualOrderByPlannedDateAsc(Long userId, LocalDate today);
}
