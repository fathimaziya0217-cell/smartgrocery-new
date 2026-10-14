package com.example.smartgrocery.service;

import com.example.smartgrocery.dto.GroceryItemDto;
import com.example.smartgrocery.dto.InventoryBatchDto;
import com.example.smartgrocery.entity.GroceryCategory;
import com.example.smartgrocery.entity.GroceryItem;
import com.example.smartgrocery.entity.InventoryBatch;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.exception.InsufficientStockException;
import com.example.smartgrocery.exception.InvalidUnitException;
import com.example.smartgrocery.exception.ResourceNotFoundException;
import com.example.smartgrocery.repository.GroceryCategoryRepository;
import com.example.smartgrocery.repository.GroceryItemRepository;
import com.example.smartgrocery.repository.InventoryBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryService {

    private final GroceryItemRepository groceryItemRepository;
    private final GroceryCategoryRepository categoryRepository;
    private final InventoryBatchRepository batchRepository;

    public InventoryService(GroceryItemRepository groceryItemRepository,
                            GroceryCategoryRepository categoryRepository,
                            InventoryBatchRepository batchRepository) {
        this.groceryItemRepository = groceryItemRepository;
        this.categoryRepository = categoryRepository;
        this.batchRepository = batchRepository;
    }

    // ===================================================================
    // Unit Conversion Logic
    // ===================================================================
    public static BigDecimal convertQuantity(BigDecimal quantity, String fromUnit, String toUnit) {
        if (quantity == null) return BigDecimal.ZERO;
        if (fromUnit == null || toUnit == null) {
            throw new InvalidUnitException("Units cannot be null for conversion");
        }
        String from = fromUnit.trim().toLowerCase();
        String to = toUnit.trim().toLowerCase();

        if (from.equals(to)) {
            return quantity;
        }

        // Weight conversions: kg <-> g
        if (from.equals("kg") && to.equals("g")) {
            return quantity.multiply(BigDecimal.valueOf(1000));
        }
        if (from.equals("g") && to.equals("kg")) {
            return quantity.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
        }

        // Volume conversions: litre <-> ml
        if (from.equals("litre") && to.equals("ml")) {
            return quantity.multiply(BigDecimal.valueOf(1000));
        }
        if (from.equals("ml") && to.equals("litre")) {
            return quantity.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
        }

        throw new InvalidUnitException("Cannot convert incompatible units from '" + fromUnit + "' to '" + toUnit + "'");
    }

    // ===================================================================
    // Usable Stock Calculations
    // ===================================================================
    @Transactional(readOnly = true)
    public BigDecimal calculateUsableStock(Long groceryItemId) {
        List<InventoryBatch> usableBatches = batchRepository.findUsableBatchesForItem(groceryItemId, LocalDate.now());
        BigDecimal total = BigDecimal.ZERO;
        for (InventoryBatch batch : usableBatches) {
            if (batch.getQuantityRemaining() != null) {
                total = total.add(batch.getQuantityRemaining());
            }
        }
        return total;
    }
@Transactional(readOnly = true)
public BigDecimal calculateTotalInventoryValue(Long userId) {
    List<GroceryItem> items =
            groceryItemRepository.findByUserIdOrderByNameAsc(userId);

    BigDecimal totalValue = BigDecimal.ZERO;

    for (GroceryItem item : items) {
        BigDecimal usableStock = calculateUsableStock(item.getId());

        BigDecimal price = item.getEstimatedUnitPrice() != null
                ? item.getEstimatedUnitPrice()
                : BigDecimal.ZERO;

        BigDecimal itemValue = calculateItemValue(
                usableStock, item.getUnit(), price
        );

        totalValue = totalValue.add(itemValue);
    }

    return totalValue.setScale(2, RoundingMode.HALF_UP);
}

private BigDecimal calculateItemValue(
        BigDecimal stock, String unit, BigDecimal price) {

    if (stock == null || price == null
            || stock.compareTo(BigDecimal.ZERO) <= 0
            || price.compareTo(BigDecimal.ZERO) <= 0) {
        return BigDecimal.ZERO;
    }

    String normalizedUnit = unit.trim().toLowerCase();

    // estimatedUnitPrice is stored as "price per base unit" (per kg, per litre, per piece).
    // Sub-units (g, ml) must be converted to their base unit before multiplying.
    // Example: 500 g at ₹80/kg → (500 ÷ 1000) × 80 = ₹40
    switch (normalizedUnit) {
        case "g":
            // convert grams → kg, then multiply by per-kg price
            return stock
                    .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP)
                    .multiply(price)
                    .setScale(2, RoundingMode.HALF_UP);
        case "ml":
            // convert ml → litre, then multiply by per-litre price
            return stock
                    .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP)
                    .multiply(price)
                    .setScale(2, RoundingMode.HALF_UP);
        case "kg":
        case "litre":
        case "piece":
        case "pieces":
        default:
            return stock.multiply(price).setScale(2, RoundingMode.HALF_UP);
    }
}    // ===================================================================
    // Grocery Items CRUD
    // ===================================================================
    @Transactional(readOnly = true)
    public List<GroceryItem> getUserItems(Long userId, Long categoryId, String searchQuery) {
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            return groceryItemRepository.findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(userId, searchQuery.trim());
        }
        if (categoryId != null && categoryId > 0) {
            return groceryItemRepository.findByUserIdAndCategoryIdOrderByNameAsc(userId, categoryId);
        }
        return groceryItemRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional(readOnly = true)
    public GroceryItem getItemByIdAndUser(Long id, Long userId) {
        return groceryItemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery item not found with id: " + id));
    }

    @Transactional
    public GroceryItem createItem(GroceryItemDto dto, User user) {
        GroceryCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        GroceryItem item = new GroceryItem();
        item.setUser(user);
        item.setCategory(category);
        item.setName(dto.getName().trim());
        item.setUnit(dto.getUnit().trim().toLowerCase());
        item.setMinimumStock(dto.getMinimumStock() != null ? dto.getMinimumStock() : BigDecimal.ZERO);
        item.setEstimatedUnitPrice(dto.getEstimatedUnitPrice() != null ? dto.getEstimatedUnitPrice() : BigDecimal.ZERO);
        item.setNotes(dto.getNotes() != null ? dto.getNotes().trim() : null);

        GroceryItem savedItem = groceryItemRepository.save(item);

        // If an initial quantity was provided, create an initial batch
        if (dto.getInitialQuantity() != null && dto.getInitialQuantity().compareTo(BigDecimal.ZERO) > 0) {
            InventoryBatch batch = new InventoryBatch();
            batch.setGroceryItem(savedItem);
            batch.setQuantityReceived(dto.getInitialQuantity());
            batch.setQuantityRemaining(dto.getInitialQuantity());
            batch.setPurchaseDate(LocalDate.now());
            batch.setExpiryDate(dto.getInitialExpiryDate());
            batchRepository.save(batch);
        }

        return savedItem;
    }

    @Transactional
    public GroceryItem updateItem(Long id, GroceryItemDto dto, Long userId) {
        GroceryItem item = getItemByIdAndUser(id, userId);
        GroceryCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        item.setCategory(category);
        item.setName(dto.getName().trim());
        item.setUnit(dto.getUnit().trim().toLowerCase());
        item.setMinimumStock(dto.getMinimumStock() != null ? dto.getMinimumStock() : BigDecimal.ZERO);
        item.setEstimatedUnitPrice(dto.getEstimatedUnitPrice() != null ? dto.getEstimatedUnitPrice() : BigDecimal.ZERO);
        item.setNotes(dto.getNotes() != null ? dto.getNotes().trim() : null);

        return groceryItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long id, Long userId) {
        GroceryItem item = getItemByIdAndUser(id, userId);
        groceryItemRepository.delete(item);
    }

    // ===================================================================
    // Batch Management & Stock Deduction
    // ===================================================================
    @Transactional
    public InventoryBatch addBatch(InventoryBatchDto dto, Long userId) {
        GroceryItem item = getItemByIdAndUser(dto.getGroceryItemId(), userId);

        InventoryBatch batch = new InventoryBatch();
        batch.setGroceryItem(item);
        batch.setQuantityReceived(dto.getQuantityReceived());
        batch.setQuantityRemaining(dto.getQuantityReceived());
        batch.setPurchaseDate(dto.getPurchaseDate() != null ? dto.getPurchaseDate() : LocalDate.now());
        batch.setExpiryDate(dto.getExpiryDate());

        return batchRepository.save(batch);
    }

    @Transactional(readOnly = true)
    public List<InventoryBatch> getItemBatches(Long itemId, Long userId) {
        // Validate user ownership of item
        getItemByIdAndUser(itemId, userId);
        return batchRepository.findByGroceryItemIdOrderByExpiryDateAscPurchaseDateAsc(itemId);
    }

    @Transactional
    public void deductStockFEFO(Long itemId, BigDecimal requiredQuantityInItemUnit) {
        if (requiredQuantityInItemUnit == null || requiredQuantityInItemUnit.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        List<InventoryBatch> usableBatches = batchRepository.findUsableBatchesForItem(itemId, LocalDate.now());
        BigDecimal totalAvailable = BigDecimal.ZERO;
        for (InventoryBatch b : usableBatches) {
            totalAvailable = totalAvailable.add(b.getQuantityRemaining());
        }

        if (totalAvailable.compareTo(requiredQuantityInItemUnit) < 0) {
            throw new InsufficientStockException("Insufficient stock for item id " + itemId +
                    ". Available: " + totalAvailable + ", Requested: " + requiredQuantityInItemUnit);
        }

        BigDecimal remainingToDeduct = requiredQuantityInItemUnit;
        for (InventoryBatch batch : usableBatches) {
            if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // Guard: treat null quantityRemaining as ZERO
            BigDecimal currentBatchQty = batch.getQuantityRemaining() != null
                    ? batch.getQuantityRemaining()
                    : BigDecimal.ZERO;

            if (currentBatchQty.compareTo(BigDecimal.ZERO) <= 0) {
                // Already empty batch — skip
                continue;
            }

            if (currentBatchQty.compareTo(remainingToDeduct) <= 0) {
                // Batch is fully consumed
                remainingToDeduct = remainingToDeduct.subtract(currentBatchQty);
                batch.setQuantityRemaining(BigDecimal.ZERO);
            } else {
                // Batch is partially consumed
                batch.setQuantityRemaining(currentBatchQty.subtract(remainingToDeduct));
                remainingToDeduct = BigDecimal.ZERO;
            }
            batchRepository.save(batch);
        }
    }
}
