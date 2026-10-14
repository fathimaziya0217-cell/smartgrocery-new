package com.example.smartgrocery.service;

import com.example.smartgrocery.dto.BarcodeScanDto;
import com.example.smartgrocery.dto.ReceiptImportDto;
import com.example.smartgrocery.entity.*;
import com.example.smartgrocery.exception.ResourceNotFoundException;
import com.example.smartgrocery.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ScanningService {

    private final GroceryItemRepository groceryItemRepository;
    private final GroceryCategoryRepository categoryRepository;
    private final InventoryBatchRepository batchRepository;
    private final ScanImportRepository scanImportRepository;
    private final ScanImportItemRepository scanImportItemRepository;
    private final ExpenseService expenseService;

    // Simulated Barcode Product Database
    private static final Map<String, BarcodeCatalogProduct> BARCODE_CATALOG = new HashMap<>();

    static {
        BARCODE_CATALOG.put("8901030382910", new BarcodeCatalogProduct("Whole Milk 1L", "Dairy", "litre", new BigDecimal("65.00")));
        BARCODE_CATALOG.put("8901491101831", new BarcodeCatalogProduct("Basmati Rice 1kg", "Grains", "kg", new BigDecimal("85.00")));
        BARCODE_CATALOG.put("8901058852891", new BarcodeCatalogProduct("Extra Virgin Olive Oil 500ml", "Spices", "litre", new BigDecimal("650.00")));
        BARCODE_CATALOG.put("8901234567890", new BarcodeCatalogProduct("Whole Wheat Bread", "Bakery", "piece", new BigDecimal("45.00")));
        BARCODE_CATALOG.put("8909876543210", new BarcodeCatalogProduct("Fresh Farm Eggs 6pcs", "Meat and Fish", "piece", new BigDecimal("55.00")));
        BARCODE_CATALOG.put("8901112223334", new BarcodeCatalogProduct("Royal Gala Apples 1kg", "Fruits", "kg", new BigDecimal("150.00")));
        BARCODE_CATALOG.put("8902223334445", new BarcodeCatalogProduct("Red Onions 1kg", "Vegetables", "kg", new BigDecimal("35.00")));
        BARCODE_CATALOG.put("8903334445556", new BarcodeCatalogProduct("Tomato Puree 500g", "Vegetables", "kg", new BigDecimal("40.00")));
        BARCODE_CATALOG.put("8904445556667", new BarcodeCatalogProduct("Green Tea Bags 25pcs", "Beverages", "piece", new BigDecimal("180.00")));
        BARCODE_CATALOG.put("8905556667778", new BarcodeCatalogProduct("Roasted Almonds 200g", "Snacks", "g", new BigDecimal("220.00")));
        BARCODE_CATALOG.put("8906667778889", new BarcodeCatalogProduct("Dishwashing Gel 750ml", "Household Essentials", "ml", new BigDecimal("130.00")));
    }

    public ScanningService(GroceryItemRepository groceryItemRepository,
                           GroceryCategoryRepository categoryRepository,
                           InventoryBatchRepository batchRepository,
                           ScanImportRepository scanImportRepository,
                           ScanImportItemRepository scanImportItemRepository,
                           ExpenseService expenseService) {
        this.groceryItemRepository = groceryItemRepository;
        this.categoryRepository = categoryRepository;
        this.batchRepository = batchRepository;
        this.scanImportRepository = scanImportRepository;
        this.scanImportItemRepository = scanImportItemRepository;
        this.expenseService = expenseService;
    }

    public static Map<String, BarcodeCatalogProduct> getBarcodeCatalog() {
        return Collections.unmodifiableMap(BARCODE_CATALOG);
    }

    public Optional<BarcodeCatalogProduct> lookupBarcode(String barcode) {
        if (barcode == null) return Optional.empty();
        return Optional.ofNullable(BARCODE_CATALOG.get(barcode.trim()));
    }

    @Transactional
    public InventoryBatch processBarcodeConfirmation(BarcodeScanDto dto, User user) {
        String barcode = dto.getBarcode().trim();
        BarcodeCatalogProduct catalogItem = BARCODE_CATALOG.get(barcode);

        String itemName = dto.getDetectedName() != null ? dto.getDetectedName() :
                (catalogItem != null ? catalogItem.name() : "Scanned Item " + barcode);
        String categoryName = dto.getDetectedCategory() != null ? dto.getDetectedCategory() :
                (catalogItem != null ? catalogItem.category() : "Other");
        String unit = dto.getDetectedUnit() != null ? dto.getDetectedUnit() :
                (catalogItem != null ? catalogItem.unit() : "piece");
        BigDecimal price = dto.getDetectedPrice() != null ? dto.getDetectedPrice() :
                (catalogItem != null ? catalogItem.price() : BigDecimal.ZERO);

        GroceryCategory category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> categoryRepository.save(new GroceryCategory(categoryName, "Auto-created from scan")));

        // Find or create grocery item for user
        List<GroceryItem> existingItems = groceryItemRepository.findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(user.getId(), itemName);
        GroceryItem groceryItem;
        if (!existingItems.isEmpty()) {
            groceryItem = existingItems.get(0);
        } else {
            groceryItem = new GroceryItem();
            groceryItem.setUser(user);
            groceryItem.setName(itemName);
            groceryItem.setCategory(category);
            groceryItem.setUnit(unit);
            groceryItem.setMinimumStock(BigDecimal.ONE);
            groceryItem.setEstimatedUnitPrice(price);
            groceryItem.setNotes("Imported via barcode scan simulation [" + barcode + "]");
            groceryItem = groceryItemRepository.save(groceryItem);
        }

        // Create new inventory batch
        InventoryBatch batch = new InventoryBatch();
        batch.setGroceryItem(groceryItem);
        batch.setQuantityReceived(dto.getQuantity());
        batch.setQuantityRemaining(dto.getQuantity());
        batch.setPurchaseDate(LocalDate.now());
        batch.setExpiryDate(dto.getExpiryDate());
        InventoryBatch savedBatch = batchRepository.save(batch);

        // Record scan import session
        ScanImport scanImport = new ScanImport(user, "BARCODE");
        scanImport.setProcessingStatus("COMPLETED");
        scanImport.setProcessedAt(LocalDateTime.now());
        ScanImport savedImport = scanImportRepository.save(scanImport);

        ScanImportItem importItem = new ScanImportItem(
                savedImport, groceryItem, itemName, dto.getQuantity(), unit, price);
        importItem.setProcessingStatus("COMPLETED");
        scanImportItemRepository.save(importItem);

        return savedBatch;
    }

    @Transactional
    public ScanImport processReceiptImport(ReceiptImportDto dto, User user) {
        ScanImport scanImport = new ScanImport(user, "RECEIPT");
        scanImport.setProcessingStatus("COMPLETED");
        scanImport.setProcessedAt(LocalDateTime.now());
        ScanImport savedImport = scanImportRepository.save(scanImport);

        BigDecimal totalReceiptExpense = BigDecimal.ZERO;

        for (ReceiptImportDto.ReceiptItemDto itemDto : dto.getItems()) {
            if (itemDto.getItemName() == null || itemDto.getItemName().trim().isEmpty()) {
                continue;
            }

            String categoryName = itemDto.getCategoryName() != null ? itemDto.getCategoryName() : "Other";
            GroceryCategory category = categoryRepository.findByNameIgnoreCase(categoryName)
                    .orElseGet(() -> categoryRepository.save(new GroceryCategory(categoryName, "Auto-created from receipt")));

            List<GroceryItem> existing = groceryItemRepository.findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(
                    user.getId(), itemDto.getItemName().trim());

            GroceryItem item;
            if (!existing.isEmpty()) {
                item = existing.get(0);
            } else {
                item = new GroceryItem();
                item.setUser(user);
                item.setName(itemDto.getItemName().trim());
                item.setCategory(category);
                item.setUnit(itemDto.getUnit() != null ? itemDto.getUnit() : "piece");
                item.setMinimumStock(BigDecimal.ONE);
                item.setEstimatedUnitPrice(itemDto.getPrice());
                item.setNotes("Imported from receipt (" + dto.getStoreName() + ")");
                item = groceryItemRepository.save(item);
            }

            // Create inventory batch
            InventoryBatch batch = new InventoryBatch();
            batch.setGroceryItem(item);
            batch.setQuantityReceived(itemDto.getQuantity());
            batch.setQuantityRemaining(itemDto.getQuantity());
            batch.setPurchaseDate(dto.getPurchaseDate() != null ? dto.getPurchaseDate() : LocalDate.now());
            batch.setExpiryDate(itemDto.getExpiryDate());
            batchRepository.save(batch);

            // Audit record
            ScanImportItem importItem = new ScanImportItem(
                    savedImport, item, item.getName(), itemDto.getQuantity(), item.getUnit(), itemDto.getPrice());
            importItem.setProcessingStatus("COMPLETED");
            scanImportItemRepository.save(importItem);

            totalReceiptExpense = totalReceiptExpense.add(itemDto.getPrice().multiply(itemDto.getQuantity()));
        }

        // Record expense if requested
        if (dto.isRecordExpense() && totalReceiptExpense.compareTo(BigDecimal.ZERO) > 0) {
            Expense expense = new Expense();
            expense.setUser(user);
            expense.setAmount(totalReceiptExpense);
            expense.setExpenseDate(dto.getPurchaseDate() != null ? dto.getPurchaseDate() : LocalDate.now());
            expense.setDescription("Receipt Import: " + (dto.getStoreName() != null ? dto.getStoreName() : "Grocery Store"));
            expenseService.saveExpense(expense);
        }

        return savedImport;
    }

    public record BarcodeCatalogProduct(String name, String category, String unit, BigDecimal price) {}
}
