package com.example.smartgrocery.controller;

import com.example.smartgrocery.dto.DashboardStatsDto;
import com.example.smartgrocery.entity.Alert;
import com.example.smartgrocery.entity.MealPlanEntry;
import com.example.smartgrocery.entity.User;
import com.example.smartgrocery.service.AlertService;
import com.example.smartgrocery.service.DashboardService;
import com.example.smartgrocery.service.MealPlanService;
import com.example.smartgrocery.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final AlertService alertService;
    private final MealPlanService mealPlanService;
    private final UserService userService;

    public DashboardController(DashboardService dashboardService,
                               AlertService alertService,
                               MealPlanService mealPlanService,
                               UserService userService) {
        this.dashboardService = dashboardService;
        this.alertService = alertService;
        this.mealPlanService = mealPlanService;
        this.userService = userService;
    }

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model) {
        User user = userService.getAuthenticatedUser();

        // Refresh alerts dynamically on dashboard visit
        alertService.refreshAlertsForUser(user.getId());

        DashboardStatsDto stats = dashboardService.getDashboardStats(user.getId());
        List<Alert> recentAlerts = alertService.getActiveAlerts(user.getId());
        List<MealPlanEntry> upcomingMeals = mealPlanService.getUpcomingMeals(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        model.addAttribute("recentAlerts", recentAlerts);
        model.addAttribute("upcomingMeals", upcomingMeals);

        return "dashboard";
    }
}
