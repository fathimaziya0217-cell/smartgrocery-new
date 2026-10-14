package com.example.smartgrocery.controller;

import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final UserService userService;

    @Value("${app.alerts.near-expiry-days:7}")
    private int nearExpiryDays;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showSettings(Model model) {
        User user = userService.getAuthenticatedUser();
        model.addAttribute("user", user);
        model.addAttribute("nearExpiryDays", nearExpiryDays);
        return "settings";
    }
}
