package com.example.smartgrocery.service;

import com.example.smartgrocery.dto.ShoppingListGenerateDto;
import com.example.smartgrocery.dto.ShoppingListItemDto;
import com.example.smartgrocery.entity.*;
import com.example.smartgrocery.exception.ResourceNotFoundException;
import com.example.smartgrocery.repository.GroceryItemRepository;
import com.example.smartgrocery.repository.InventoryBatchRepository;
import com.example.smartgrocery.repository.ShoppingListItemRepository;
import com.example.smartgrocery.repository.ShoppingListRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final GroceryItemRepository groceryItemRepository;
    private final InventoryBatchRepository batchRepository;
    private final InventoryService inventoryService;
    private final MealPlanService mealPlanService;
    private final ExpenseService expenseService;

    public ShoppingListService(ShoppingListRepository shoppingListRepository,
                               ShoppingListItemRepository shoppingListItemRepository,
                               GroceryItemRepository groceryItemRepository,
                               InventoryBatchRepository batchRepository,
                               InventoryService inventoryService,
                               MealPlanService mealPlanService,
                               ExpenseService expenseService) {
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingListItemRepository = shoppingListItemRepository;
        this.groceryItemRepository = groceryItemRepository;
        this.batchRepository = batchRepository;
        this.inventoryService = inventoryService;
        this.mealPlanService = mealPlanService;
        this.expenseService = expenseService;
    }

    @Transactional(readOnly = true)
    public List<ShoppingList> getUserShoppingLists(Long userId) {
        List<ShoppingList> lists = shoppingListRepository.findByUserIdOrderByCreatedAtDesc(userId);
        // Eagerly initialize item count so the dropdown can show "X items"
        for (ShoppingList list : lists) {
            Hibernate.initialize(list.getItems());
        }
        return lists;
    }

    @Transactional(readOnly = true)
    public ShoppingList getShoppingListByIdAndUser(Long id, Long userId) {
        ShoppingList list = shoppingListRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list not found with id: " + id));
        // Eagerly initialize items and their nested groceryItem.category for the view
        Hibernate.initialize(list.getItems());
        for (ShoppingListItem item : list.getItems()) {
            Hibernate.initialize(item.getGroceryItem());
            Hibernate.initialize(item.getGroceryItem().getCategory());
        }
        return list;
    }

    // ===================================================================
    // Automated Shopping List Generation
    // ===================================================================
    @Transactional
    public ShoppingList generateShoppingList(ShoppingListGenerateDto dto, User user) {
        ShoppingList list = new ShoppingList();
        list.setUser(user);
        list.setName(dto.getName() != null && !dto.getName().trim().isEmpty() ? dto.getName().trim() : "Generated Shopping List");
        ShoppingList savedList = shoppingListRepository.save(list);

        // Map item ID -> required shortage quantity
        Map<Long, BigDecimal> shortages = new LinkedHashMap<>();

        // 1. Gather meal plan requirements if selected
        if (dto.getMealPlanId() != null && dto.getMealPlanId() > 0) {
            Map<Long, BigDecimal> mealNeeds = mealPlanService.calculateAggregatedIngredientRequirements(dto.getMealPlanId(), user.getId());
            for (Map.Entry<Long, BigDecimal> entry : mealNeeds.entrySet()) {
                Long itemId = entry.getKey();
                BigDecimal totalRequired = entry.getValue();
                BigDecimal usableStock = inventoryService.calculateUsableStock(itemId);

                BigDecimal shortage = totalRequired.subtract(usableStock);
                if (shortage.compareTo(BigDecimal.ZERO) > 0) {
                    shortages.put(itemId, shortage.setScale(2, RoundingMode.HALF_UP));
                }
            }
        }

        // 2. Gather low stock replenishment items if requested
        if (dto.isIncludeLowStock()) {
            List<GroceryItem> userItems = groceryItemRepository.findByUserIdOrderByNameAsc(user.getId());
            for (GroceryItem item : userItems) {
                BigDecimal usableStock = inventoryService.calculateUsableStock(item.getId());
                BigDecimal minStock = item.getMinimumStock() != null ? item.getMinimumStock() : BigDecimal.ZERO;

                if (usableStock.compareTo(minStock) <= 0) {
                    // Replenish target: minimum stock * 2 - current usable
                    BigDecimal target = minStock.multiply(BigDecimal.valueOf(2)).max(BigDecimal.ONE);
                    BigDecimal replenishAmount = target.subtract(usableStock);
                    if (replenishAmount.compareTo(BigDecimal.ZERO) > 0) {
                        shortages.merge(item.getId(), replenishAmount.setScale(2, RoundingMode.HALF_UP), BigDecimal::max);
                    }
                }
            }
        }

        // Persist items for this list
        for (Map.Entry<Long, BigDecimal> entry : shortages.entrySet()) {
            Long itemId = entry.getKey();
            BigDecimal shortageQty = entry.getValue();

            GroceryItem item = groceryItemRepository.findByIdAndUserId(itemId, user.getId()).orElse(null);
            if (item != null && shortageQty.compareTo(BigDecimal.ZERO) > 0) {
                ShoppingListItem listItem = new ShoppingListItem();
                listItem.setShoppingList(savedList);
                listItem.setGroceryItem(item);
                listItem.setQuantityRequired(shortageQty);
                listItem.setUnit(item.getUnit());
                listItem.setEstimatedUnitPrice(item.getEstimatedUnitPrice() != null ? item.getEstimatedUnitPrice() : BigDecimal.ZERO);
                listItem.setEstimatedTotalPrice(shortageQty.multiply(listItem.getEstimatedUnitPrice()).setScale(2, RoundingMode.HALF_UP));
                listItem.setPurchased(false);
                listItem.setQuantityPurchased(BigDecimal.ZERO);
                shoppingListItemRepository.save(listItem);
            }
        }

        return savedList;
    }

    // ===================================================================
    // Manual Items Management & Item Status
    // ===================================================================
    @Transactional
    public ShoppingListItem addManualItem(Long listId, ShoppingListItemDto dto, Long userId) {
        ShoppingList list = getShoppingListByIdAndUser(listId, userId);
        GroceryItem item = groceryItemRepository.findByIdAndUserId(dto.getGroceryItemId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery item not found with id: " + dto.getGroceryItemId()));

        ShoppingListItem listItem = new ShoppingListItem();
        listItem.setShoppingList(list);
        listItem.setGroceryItem(item);
        listItem.setQuantityRequired(dto.getQuantityRequired());
        listItem.setUnit(dto.getUnit() != null ? dto.getUnit() : item.getUnit());
        listItem.setEstimatedUnitPrice(dto.getEstimatedUnitPrice() != null ? dto.getEstimatedUnitPrice() : item.getEstimatedUnitPrice());
        listItem.setEstimatedTotalPrice(listItem.getQuantityRequired().multiply(listItem.getEstimatedUnitPrice()).setScale(2, RoundingMode.HALF_UP));
        listItem.setPurchased(false);
        listItem.setQuantityPurchased(BigDecimal.ZERO);

        return shoppingListItemRepository.save(listItem);
    }

    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        ShoppingListItem item = shoppingListItemRepository.findByIdAndShoppingListUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list item not found with id: " + itemId));
        shoppingListItemRepository.delete(item);
    }

    @Transactional
    public void togglePurchased(Long itemId, BigDecimal quantityPurchased, boolean createInventoryBatch, boolean recordExpense, Long userId) {
        ShoppingListItem item = shoppingListItemRepository.findByIdAndShoppingListUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping list item not found with id: " + itemId));

        boolean newStatus = !item.isPurchased();
        item.setPurchased(newStatus);

        if (newStatus) {
            BigDecimal qty = (quantityPurchased != null && quantityPurchased.compareTo(BigDecimal.ZERO) > 0)
                    ? quantityPurchased : item.getQuantityRequired();
            item.setQuantityPurchased(qty);

            if (createInventoryBatch) {
                InventoryBatch batch = new InventoryBatch();
                batch.setGroceryItem(item.getGroceryItem());
                batch.setQuantityReceived(qty);
                batch.setQuantityRemaining(qty);
                batch.setPurchaseDate(LocalDate.now());
                batchRepository.save(batch);
            }

            if (recordExpense) {
                BigDecimal expenseAmount = qty.multiply(item.getEstimatedUnitPrice()).setScale(2, RoundingMode.HALF_UP);
                if (expenseAmount.compareTo(BigDecimal.ZERO) > 0) {
                    Expense expense = new Expense();
                    expense.setUser(item.getShoppingList().getUser());
                    expense.setShoppingListItem(item);
                    expense.setAmount(expenseAmount);
                    expense.setExpenseDate(LocalDate.now());
                    expense.setDescription("Purchased " + item.getGroceryItem().getName() + " (" + qty + " " + item.getUnit() + ")");
                    expenseService.saveExpense(expense);
                }
            }
        } else {
            item.setQuantityPurchased(BigDecimal.ZERO);
        }

        shoppingListItemRepository.save(item);
    }

    @Transactional
    public void deleteShoppingList(Long id, Long userId) {
        ShoppingList list = getShoppingListByIdAndUser(id, userId);
        shoppingListRepository.delete(list);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalShoppingCost(Long userId) {
        List<ShoppingList> lists = shoppingListRepository.findByUserIdOrderByCreatedAtDesc(userId);
        BigDecimal total = BigDecimal.ZERO;
        for (ShoppingList list : lists) {
            total = total.add(list.calculateEstimatedTotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
