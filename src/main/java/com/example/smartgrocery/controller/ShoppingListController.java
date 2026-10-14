package com.example.smartgrocery.controller;

import com.example.smartgrocery.dto.ShoppingListGenerateDto;
import com.example.smartgrocery.dto.ShoppingListItemDto;
import com.example.smartgrocery.entity.*;
import com.example.smartgrocery.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/shopping-lists")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final MealPlanService mealPlanService;
    private final InventoryService inventoryService;
    private final UserService userService;

    public ShoppingListController(ShoppingListService shoppingListService,
                                  MealPlanService mealPlanService,
                                  InventoryService inventoryService,
                                  UserService userService) {
        this.shoppingListService = shoppingListService;
        this.mealPlanService = mealPlanService;
        this.inventoryService = inventoryService;
        this.userService = userService;
    }

    @GetMapping
    public String viewShoppingLists(@RequestParam(value = "listId", required = false) Long listId,
                                    @RequestParam(value = "category", required = false) String categoryFilter,
                                    Model model) {
        User user = userService.getAuthenticatedUser();
        List<ShoppingList> lists = shoppingListService.getUserShoppingLists(user.getId());
        List<MealPlan> mealPlans = mealPlanService.getUserMealPlans(user.getId());
        List<GroceryItem> userItems = inventoryService.getUserItems(user.getId(), null, null);

        ShoppingList selectedList = null;
        if (listId != null) {
            selectedList = shoppingListService.getShoppingListByIdAndUser(listId, user.getId());
        } else if (!lists.isEmpty()) {
            // Re-fetch with full eager initialization of nested associations
            selectedList = shoppingListService.getShoppingListByIdAndUser(lists.get(0).getId(), user.getId());
        }

        Map<String, List<ShoppingListItem>> itemsByCategory = new LinkedHashMap<>();
        BigDecimal estimatedTotalCost = BigDecimal.ZERO;

        if (selectedList != null) {
            List<ShoppingListItem> items = selectedList.getItems();
            if (categoryFilter != null && !categoryFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(categoryFilter)) {
                items = items.stream()
                        .filter(i -> i.getGroceryItem().getCategory().getName().equalsIgnoreCase(categoryFilter.trim()))
                        .collect(Collectors.toList());
            }

            itemsByCategory = items.stream()
                    .collect(Collectors.groupingBy(i -> i.getGroceryItem().getCategory().getName(), LinkedHashMap::new, Collectors.toList()));

            estimatedTotalCost = selectedList.calculateEstimatedTotal();
        }

        model.addAttribute("lists", lists);
        model.addAttribute("selectedList", selectedList);
        model.addAttribute("itemsByCategory", itemsByCategory);
        model.addAttribute("estimatedTotalCost", estimatedTotalCost);
        model.addAttribute("mealPlans", mealPlans);
        model.addAttribute("userItems", userItems);
        model.addAttribute("generateDto", new ShoppingListGenerateDto());
        model.addAttribute("manualItemDto", new ShoppingListItemDto());
        model.addAttribute("selectedCategoryFilter", categoryFilter);

        return "shopping-list";
    }

    @PostMapping("/generate")
    public String generateList(@Valid @ModelAttribute("generateDto") ShoppingListGenerateDto dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid list name.");
            return "redirect:/shopping-lists";
        }

        ShoppingList generated = shoppingListService.generateShoppingList(dto, user);
        redirectAttributes.addFlashAttribute("successMessage",
                "Automated shopping list '" + generated.getName() + "' generated successfully!");
        return "redirect:/shopping-lists?listId=" + generated.getId();
    }

    @PostMapping("/{id}/item/add")
    public String addManualItem(@PathVariable("id") Long listId,
                                @Valid @ModelAttribute("manualItemDto") ShoppingListItemDto itemDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (bindingResult.hasErrors() || itemDto.getGroceryItemId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select an item and provide a valid quantity.");
            return "redirect:/shopping-lists?listId=" + listId;
        }

        shoppingListService.addManualItem(listId, itemDto, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Item added to shopping list!");
        return "redirect:/shopping-lists?listId=" + listId;
    }

    @PostMapping("/item/{id}/toggle")
    public String togglePurchased(@PathVariable("id") Long itemId,
                                  @RequestParam("listId") Long listId,
                                  @RequestParam(value = "quantityPurchased", required = false) BigDecimal quantityPurchased,
                                  @RequestParam(value = "createBatch", required = false, defaultValue = "false") boolean createBatch,
                                  @RequestParam(value = "recordExpense", required = false, defaultValue = "false") boolean recordExpense,
                                  RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        shoppingListService.togglePurchased(itemId, quantityPurchased, createBatch, recordExpense, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Item purchase status updated!");
        return "redirect:/shopping-lists?listId=" + listId;
    }

    @PostMapping("/item/{id}/delete")
    public String deleteItem(@PathVariable("id") Long itemId,
                             @RequestParam("listId") Long listId,
                             RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        shoppingListService.deleteItem(itemId, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from shopping list.");
        return "redirect:/shopping-lists?listId=" + listId;
    }

    @PostMapping("/{id}/delete")
    public String deleteList(@PathVariable("id") Long listId, RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        shoppingListService.deleteShoppingList(listId, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Shopping list deleted successfully.");
        return "redirect:/shopping-lists";
    }
}
