package com.example.smartgrocery.repository;

import com.example.smartgrocery.entity.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {
    List<ShoppingListItem> findByShoppingListId(Long shoppingListId);
    Optional<ShoppingListItem> findByIdAndShoppingListUserId(Long id, Long userId);
    List<ShoppingListItem> findByShoppingListIdOrderByPurchasedAscGroceryItemCategoryNameAsc(Long shoppingListId);
}
