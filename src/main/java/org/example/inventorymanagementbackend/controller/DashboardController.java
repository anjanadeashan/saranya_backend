package org.example.inventorymanagementbackend.controller;

import org.example.inventorymanagementbackend.dto.response.*;
import org.example.inventorymanagementbackend.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dashboard Controller
 * Handles dashboard analytics and metrics endpoints
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get dashboard summary metrics
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        try {
            DashboardSummaryResponse summary = dashboardService.getDashboardSummary();
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            logger.error("Error fetching dashboard summary", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch dashboard summary"));
        }
    }

    /**
     * Get low stock alerts
     */
    @GetMapping("/low-stock-alerts")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockAlerts() {
        try {
            List<ProductResponse> products = dashboardService.getLowStockAlerts();
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            logger.error("Error fetching low stock alerts", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch low stock alerts"));
        }
    }

    /**
     * Get check date reminders
     */
    @GetMapping("/check-reminders")
    public ResponseEntity<ApiResponse<List<CheckReminderResponse>>> getCheckReminders() {
        try {
            List<CheckReminderResponse> reminders = dashboardService.getCheckReminders();
            return ResponseEntity.ok(ApiResponse.success(reminders));
        } catch (Exception e) {
            logger.error("Error fetching check reminders", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch check reminders"));
        }
    }

    /**
     * Get top selling products
     */
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<TopSellingProductResponse>>> getTopSellingProducts() {
        try {
            List<TopSellingProductResponse> products = dashboardService.getTopSellingProducts();
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            logger.error("Error fetching top selling products", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch top selling products"));
        }
    }

    /**
     * Get sales vs purchases data for charts
     */
    @GetMapping("/sales-vs-purchases")
    public ResponseEntity<ApiResponse<List<SalesVsPurchasesResponse>>> getSalesVsPurchases() {
        try {
            List<SalesVsPurchasesResponse> data = dashboardService.getSalesVsPurchasesData();
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            logger.error("Error fetching sales vs purchases data", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch sales vs purchases data"));
        }
    }
}