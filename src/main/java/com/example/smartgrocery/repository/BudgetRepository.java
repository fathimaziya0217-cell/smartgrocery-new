package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndBudgetMonthAndBudgetYear(Long userId, Integer month, Integer year);
    List<Budget> findByUserIdOrderByBudgetYearDescBudgetMonthDesc(Long userId);
    Optional<Budget> findByIdAndUserId(Long id, Long userId);
}
