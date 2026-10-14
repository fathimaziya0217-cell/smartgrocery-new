package com.example.smartgrocery.controller;

import com.example.smartgrocery.dto.BarcodeScanDto;
import com.example.smartgrocery.dto.ReceiptImportDto;
import com.example.smartgrocery.entity.GroceryCategory;
import com.example.smartgrocery.entity.ScanImport;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.repository.ScanImportRepository;
import com.example.smartgrocery.service.CategoryService;
import com.example.smartgrocery.service.ScanningService;
import com.example.smartgrocery.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/scanning")
public class ScanningController {

    private final ScanningService scanningService;
    private final CategoryService categoryService;
    private final ScanImportRepository scanImportRepository;
    private final UserService userService;

    public ScanningController(ScanningService scanningService,
                              CategoryService categoryService,
                              ScanImportRepository scanImportRepository,
                              UserService userService) {
        this.scanningService = scanningService;
        this.categoryService = categoryService;
        this.scanImportRepository = scanImportRepository;
        this.userService = userService;
    }

    @GetMapping
    public String showScanningHub(@RequestParam(value = "barcode", required = false) String barcode,
                                  Model model) {
        User user = userService.getAuthenticatedUser();
        List<GroceryCategory> categories = categoryService.getAllCategories();
        Map<String, ScanningService.BarcodeCatalogProduct> catalog = ScanningService.getBarcodeCatalog();
        List<ScanImport> recentImports = scanImportRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        BarcodeScanDto barcodeDto = new BarcodeScanDto();
        if (barcode != null && !barcode.trim().isEmpty()) {
            barcodeDto.setBarcode(barcode.trim());
            Optional<ScanningService.BarcodeCatalogProduct> catalogProduct = scanningService.lookupBarcode(barcode.trim());
            if (catalogProduct.isPresent()) {
                ScanningService.BarcodeCatalogProduct product = catalogProduct.get();
                barcodeDto.setDetectedName(product.name());
                barcodeDto.setDetectedCategory(product.category());
                barcodeDto.setDetectedUnit(product.unit());
                barcodeDto.setDetectedPrice(product.price());
                model.addAttribute("productFound", true);
            } else {
                model.addAttribute("productFound", false);
            }
        }

        ReceiptImportDto receiptDto = new ReceiptImportDto();
        // Provide sample rows for ease of testing
        ReceiptImportDto.ReceiptItemDto sampleItem1 = new ReceiptImportDto.ReceiptItemDto();
        sampleItem1.setItemName("Fresh Milk 1L");
        sampleItem1.setCategoryName("Dairy");
        sampleItem1.setUnit("litre");
        receiptDto.getItems().add(sampleItem1);

        model.addAttribute("barcodeDto", barcodeDto);
        model.addAttribute("receiptDto", receiptDto);
        model.addAttribute("categories", categories);
        model.addAttribute("catalog", catalog);
        model.addAttribute("recentImports", recentImports);

        return "scanning";
    }

    @PostMapping("/barcode/lookup")
    public String lookupBarcode(@RequestParam("barcode") String barcode) {
        return "redirect:/scanning?barcode=" + (barcode != null ? barcode.trim() : "");
    }

    @PostMapping("/barcode/confirm")
    public String confirmBarcodeScan(@Valid @ModelAttribute("barcodeDto") BarcodeScanDto barcodeDto,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid barcode and positive quantity.");
            return "redirect:/scanning";
        }

        scanningService.processBarcodeConfirmation(barcodeDto, user);
        redirectAttributes.addFlashAttribute("successMessage",
                "Barcode simulated scan successful! Added " + barcodeDto.getQuantity() + " of '" +
                barcodeDto.getDetectedName() + "' to inventory batches.");
        return "redirect:/inventory";
    }

    @PostMapping("/receipt/confirm")
    public String confirmReceiptImport(@ModelAttribute("receiptDto") ReceiptImportDto receiptDto,
                                       RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        if (receiptDto.getItems() == null || receiptDto.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter at least one receipt line item.");
            return "redirect:/scanning";
        }

        scanningService.processReceiptImport(receiptDto, user);
        redirectAttributes.addFlashAttribute("successMessage",
                "Receipt import simulation successful! Inventory and expenses have been updated.");
        return "redirect:/inventory";
    }
}
