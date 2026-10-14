package com.example.smartgrocery.controller;

import com.example.smartgrocery.dto.GroceryItemDto;
import com.example.smartgrocery.dto.InventoryBatchDto;
import com.example.smartgrocery.entity.GroceryCategory;
import com.example.smartgrocery.entity.GroceryItem;
import com.example.smartgrocery.entity.InventoryBatch;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.service.CategoryService;
import com.example.smartgrocery.service.InventoryService;
import com.example.smartgrocery.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final CategoryService categoryService;
    private final UserService userService;

    public InventoryController(InventoryService inventoryService,
                               CategoryService categoryService,
                               UserService userService) {
        this.inventoryService = inventoryService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping
    public String listInventory(@RequestParam(value = "categoryId", required = false) Long categoryId,
                                @RequestParam(value = "q", required = false) String search,
                                Model model) {
        User user = userService.getAuthenticatedUser();
        List<GroceryItem> items = inventoryService.getUserItems(user.getId(), categoryId, search);
        List<GroceryCategory> categories = categoryService.getAllCategories();

        // Calculate usable stock for each item for view presentation
        Map<Long, BigDecimal> usableStockMap = new HashMap<>();
        for (GroceryItem item : items) {
            usableStockMap.put(item.getId(), inventoryService.calculateUsableStock(item.getId()));
        }

        BigDecimal totalInventoryValue = inventoryService.calculateTotalInventoryValue(user.getId());

        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("searchQuery", search);
        model.addAttribute("usableStockMap", usableStockMap);
        model.addAttribute("totalInventoryValue", totalInventoryValue);
        model.addAttribute("newBatchDto", new InventoryBatchDto());

        return "inventory";
    }

    @GetMapping("/add")
    public String showAddItemForm(Model model) {
        model.addAttribute("itemDto", new GroceryItemDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "add-item";
    }

    @PostMapping("/add")
    public String addItem(@Valid @ModelAttribute("itemDto") GroceryItemDto itemDto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "add-item";
        }

        inventoryService.createItem(itemDto, user);
        redirectAttributes.addFlashAttribute("successMessage", "Item '" + itemDto.getName() + "' added successfully!");
        return "redirect:/inventory";
    }

    @GetMapping("/edit/{id}")
    public String showEditItemForm(@PathVariable("id") Long id, Model model) {
        User user = userService.getAuthenticatedUser();
        GroceryItem item = inventoryService.getItemByIdAndUser(id, user.getId());

        GroceryItemDto dto = new GroceryItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setCategoryId(item.getCategory().getId());
        dto.setUnit(item.getUnit());
        dto.setMinimumStock(item.getMinimumStock());
        dto.setEstimatedUnitPrice(item.getEstimatedUnitPrice());
        dto.setNotes(item.getNotes());

        model.addAttribute("itemDto", dto);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("item", item);
        return "edit-item";
    }

    @PostMapping("/edit/{id}")
    public String updateItem(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("itemDto") GroceryItemDto itemDto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "edit-item";
        }

        inventoryService.updateItem(id, itemDto, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Item updated successfully!");
        return "redirect:/inventory";
    }

    @PostMapping("/delete/{id}")
    public String deleteItem(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        try {
            inventoryService.deleteItem(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Item deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete item because it is referenced in recipes or shopping lists.");
        }
        return "redirect:/inventory";
    }

    @PostMapping("/{id}/batch")
    public String addBatch(@PathVariable("id") Long id,
                           @Valid @ModelAttribute("newBatchDto") InventoryBatchDto batchDto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        batchDto.setGroceryItemId(id);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid batch data. Please check quantities and dates.");
            return "redirect:/inventory";
        }

        inventoryService.addBatch(batchDto, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Inventory batch added successfully!");
        return "redirect:/inventory";
    }

    @GetMapping("/{id}/batches")
    @ResponseBody
    public List<InventoryBatch> getItemBatches(@PathVariable("id") Long id) {
        User user = userService.getAuthenticatedUser();
        return inventoryService.getItemBatches(id, user.getId());
    }
}
