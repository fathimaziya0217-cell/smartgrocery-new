package com.example.smartgrocery.controller;

import com.example.smartgrocery.entity.Alert;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.service.AlertService;
import com.example.smartgrocery.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService alertService;
    private final UserService userService;

    public AlertController(AlertService alertService, UserService userService) {
        this.alertService = alertService;
        this.userService = userService;
    }

    @GetMapping
    public String showAlerts(Model model) {
        User user = userService.getAuthenticatedUser();
        alertService.refreshAlertsForUser(user.getId());

        List<Alert> activeAlerts = alertService.getActiveAlerts(user.getId());
        List<Alert> allAlerts = alertService.getAllAlerts(user.getId());

        model.addAttribute("activeAlerts", activeAlerts);
        model.addAttribute("allAlerts", allAlerts);

        return "alerts";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        alertService.markAlertRead(id, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Alert marked as read.");
        return "redirect:/alerts";
    }

    @PostMapping("/{id}/resolve")
    public String resolveAlert(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        alertService.resolveAlert(id, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Alert resolved.");
        return "redirect:/alerts";
    }

    @PostMapping("/refresh")
    public String refreshAlerts(RedirectAttributes redirectAttributes) {
        User user = userService.getAuthenticatedUser();
        alertService.refreshAlertsForUser(user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Alerts refreshed based on current inventory and budget.");
        return "redirect:/alerts";
    }
}
