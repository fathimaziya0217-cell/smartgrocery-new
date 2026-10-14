package com.example.smartgrocery.controller;

import com.example.smartgrocery.entity.GroceryItem;
import com.example.smartgrocery.entity.InventoryBatch;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.service.InventoryService;
import com.example.smartgrocery.service.ReportService;
import com.example.smartgrocery.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final InventoryService inventoryService;
    private final UserService userService;

    public ReportController(ReportService reportService,
                            InventoryService inventoryService,
                            UserService userService) {
        this.reportService = reportService;
        this.inventoryService = inventoryService;
        this.userService = userService;
    }

    @GetMapping
    public String showReports(@RequestParam(value = "nearExpiryDays", required = false, defaultValue = "7") int nearExpiryDays,
                              Model model) {
        User user = userService.getAuthenticatedUser();

        List<GroceryItem> lowStockItems = reportService.getLowStockReport(user.getId());
        List<InventoryBatch> nearExpiryBatches = reportService.getNearExpiryReport(user.getId(), nearExpiryDays);
        List<InventoryBatch> expiredBatches = reportService.getExpiredReport(user.getId());

        Map<Long, BigDecimal> usableStockMap = new HashMap<>();
        for (GroceryItem item : lowStockItems) {
            usableStockMap.put(item.getId(), inventoryService.calculateUsableStock(item.getId()));
        }

        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("nearExpiryBatches", nearExpiryBatches);
        model.addAttribute("expiredBatches", expiredBatches);
        model.addAttribute("usableStockMap", usableStockMap);
        model.addAttribute("nearExpiryDays", nearExpiryDays);

        return "reports";
    }

    @GetMapping("/export/inventory-csv")
    public void exportInventoryCsv(HttpServletResponse response) throws IOException {
        User user = userService.getAuthenticatedUser();
        String csvData = reportService.generateInventoryCsv(user.getId());

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"grocery_inventory_" + LocalDate.now() + ".csv\"");
        response.getWriter().write(csvData);
    }

    @GetMapping("/export/expenses-csv")
    public void exportExpensesCsv(HttpServletResponse response) throws IOException {
        User user = userService.getAuthenticatedUser();
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        String csvData = reportService.generateExpensesCsv(user.getId(), startOfMonth, endOfMonth);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"grocery_expenses_" + LocalDate.now() + ".csv\"");
        response.getWriter().write(csvData);
    }
}
