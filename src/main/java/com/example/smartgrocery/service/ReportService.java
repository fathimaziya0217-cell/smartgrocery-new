package com.example.smartgrocery.service;

import com.example.smartgrocery.entity.*;
import com.example.smartgrocery.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    private final GroceryItemRepository groceryItemRepository;
    private final InventoryBatchRepository batchRepository;
    private final ExpenseRepository expenseRepository;
    private final InventoryService inventoryService;

    public ReportService(GroceryItemRepository groceryItemRepository,
                         InventoryBatchRepository batchRepository,
                         ExpenseRepository expenseRepository,
                         InventoryService inventoryService) {
        this.groceryItemRepository = groceryItemRepository;
        this.batchRepository = batchRepository;
        this.expenseRepository = expenseRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<GroceryItem> getLowStockReport(Long userId) {
        return groceryItemRepository.findLowStockItems(userId);
    }

    @Transactional(readOnly = true)
    public List<InventoryBatch> getNearExpiryReport(Long userId, int days) {
        LocalDate today = LocalDate.now();
        return batchRepository.findNearExpiryBatches(userId, today, today.plusDays(days));
    }

    @Transactional(readOnly = true)
    public List<InventoryBatch> getExpiredReport(Long userId) {
        return batchRepository.findExpiredBatches(userId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public String generateInventoryCsv(Long userId) {
        List<GroceryItem> items = groceryItemRepository.findByUserIdOrderByNameAsc(userId);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Item ID,Name,Category,Unit,Usable Quantity,Minimum Stock,Unit Price (INR),Estimated Value (INR)");
        for (GroceryItem item : items) {
            BigDecimal usableStock = inventoryService.calculateUsableStock(item.getId());
            BigDecimal unitPrice = item.getEstimatedUnitPrice() != null ? item.getEstimatedUnitPrice() : BigDecimal.ZERO;
            BigDecimal value = usableStock.multiply(unitPrice);

            pw.printf("%d,\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f%n",
                    item.getId(),
                    item.getName().replace("\"", "\"\""),
                    item.getCategory().getName().replace("\"", "\"\""),
                    item.getUnit(),
                    usableStock,
                    item.getMinimumStock(),
                    unitPrice,
                    value);
        }
        pw.flush();
        return sw.toString();
    }

    @Transactional(readOnly = true)
    public String generateExpensesCsv(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(userId, startDate, endDate);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Expense ID,Date,Description,Amount (INR)");
        for (Expense exp : expenses) {
            pw.printf("%d,%s,\"%s\",%.2f%n",
                    exp.getId(),
                    exp.getExpenseDate(),
                    exp.getDescription().replace("\"", "\"\""),
                    exp.getAmount());
        }
        pw.flush();
        return sw.toString();
    }
}
