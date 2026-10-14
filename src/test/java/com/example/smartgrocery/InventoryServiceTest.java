package com.example.smartgrocery;

import com.example.smartgrocery.entity.GroceryCategory;
import com.example.smartgrocery.entity.GroceryItem;
import com.example.smartgrocery.entity.InventoryBatch;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.exception.InsufficientStockException;
import com.example.smartgrocery.repository.GroceryCategoryRepository;
import com.example.smartgrocery.repository.GroceryItemRepository;
import com.example.smartgrocery.repository.InventoryBatchRepository;
import com.example.smartgrocery.repository.UserRepository;
import com.example.smartgrocery.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroceryCategoryRepository categoryRepository;

    @Autowired
    private GroceryItemRepository groceryItemRepository;

    @Autowired
    private InventoryBatchRepository batchRepository;

    private User testUser;
    private GroceryCategory testCategory;
    private GroceryItem testItem;

    @BeforeEach
    void setupTestData() {
        testUser = userRepository.save(new User("Test User", "testuser@grocery.com", "hashedpass"));
        testCategory = categoryRepository.save(new GroceryCategory("Test Grains", "Sample grains category"));

        testItem = new GroceryItem();
        testItem.setUser(testUser);
        testItem.setCategory(testCategory);
        testItem.setName("Test Jasmine Rice");
        testItem.setUnit("kg");
        testItem.setMinimumStock(new BigDecimal("2.00"));
        testItem.setEstimatedUnitPrice(new BigDecimal("90.00"));
        testItem = groceryItemRepository.save(testItem);
    }

    @Test
    @DisplayName("Should only count non-expired batches in usable stock")
    void testUsableStockExcludesExpiredBatches() {
        // Valid batch (expires in 30 days): 5.0 kg
        InventoryBatch validBatch = new InventoryBatch(testItem, new BigDecimal("5.00"), new BigDecimal("5.00"),
                LocalDate.now(), LocalDate.now().plusDays(30));
        batchRepository.save(validBatch);

        // Expired batch (expired 2 days ago): 3.0 kg
        InventoryBatch expiredBatch = new InventoryBatch(testItem, new BigDecimal("3.00"), new BigDecimal("3.00"),
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));
        batchRepository.save(expiredBatch);

        BigDecimal usableStock = inventoryService.calculateUsableStock(testItem.getId());
        assertEquals(0, new BigDecimal("5.00").compareTo(usableStock));
    }

    @Test
    @DisplayName("Should consume earlier expiring batches first using FEFO")
    void testFEFOStockDeduction() {
        // Batch 1 expires in 5 days: 2.0 kg
        InventoryBatch batch1 = new InventoryBatch(testItem, new BigDecimal("2.00"), new BigDecimal("2.00"),
                LocalDate.now(), LocalDate.now().plusDays(5));
        batchRepository.save(batch1);

        // Batch 2 expires in 20 days: 3.0 kg
        InventoryBatch batch2 = new InventoryBatch(testItem, new BigDecimal("3.00"), new BigDecimal("3.00"),
                LocalDate.now(), LocalDate.now().plusDays(20));
        batchRepository.save(batch2);

        // Deduct 2.5 kg: batch1 should be reduced to 0, batch2 should be reduced to 2.5 kg
        inventoryService.deductStockFEFO(testItem.getId(), new BigDecimal("2.50"));

        InventoryBatch updatedBatch1 = batchRepository.findById(batch1.getId()).orElseThrow();
        InventoryBatch updatedBatch2 = batchRepository.findById(batch2.getId()).orElseThrow();

        assertEquals(0, BigDecimal.ZERO.compareTo(updatedBatch1.getQuantityRemaining()));
        assertEquals(0, new BigDecimal("2.50").compareTo(updatedBatch2.getQuantityRemaining()));
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when deduction exceeds usable stock")
    void testInsufficientStockExceptionThrown() {
        InventoryBatch batch = new InventoryBatch(testItem, new BigDecimal("1.00"), new BigDecimal("1.00"),
                LocalDate.now(), LocalDate.now().plusDays(10));
        batchRepository.save(batch);

        assertThrows(InsufficientStockException.class, () ->
                inventoryService.deductStockFEFO(testItem.getId(), new BigDecimal("5.00"))
        );
    }
}
