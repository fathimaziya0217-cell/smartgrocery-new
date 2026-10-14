package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    List<MealPlan> findByUserIdOrderByWeekStartDateDesc(Long userId);
    Optional<MealPlan> findByIdAndUserId(Long id, Long userId);
    Optional<MealPlan> findFirstByUserIdAndWeekStartDateLessThanEqualAndWeekEndDateGreaterThanEqual(Long userId, LocalDate start, LocalDate end);
}
