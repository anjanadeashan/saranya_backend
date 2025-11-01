package org.example.inventorymanagementbackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.inventorymanagementbackend.dto.request.InventoryRequest;
import org.example.inventorymanagementbackend.dto.response.ApiResponse;
import org.example.inventorymanagementbackend.dto.response.InventoryResponse;
import org.example.inventorymanagementbackend.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryService inventoryService;

    // ===================== EXISTING ENDPOINTS =====================

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllInventoryMovements() {
        try {
            List<InventoryResponse> movements = inventoryService.getAllInventoryMovements();
            return ResponseEntity.ok(ApiResponse.success(movements));
        } catch (Exception e) {
            logger.error("Error fetching inventory movements", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch inventory movements"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> recordInventoryMovement(@Valid @RequestBody InventoryRequest request) {
        try {
            InventoryResponse movement = inventoryService.recordInventoryMovement(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Inventory movement recorded successfully", movement));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error recording inventory movement", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to record inventory movement"));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryByProduct(@PathVariable Long productId) {
        try {
            List<InventoryResponse> movements = inventoryService.getInventoryByProduct(productId);
            return ResponseEntity.ok(ApiResponse.success(movements));
        } catch (Exception e) {
            logger.error("Error fetching inventory for product: {}", productId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch inventory for product"));
        }
    }

    @GetMapping("/available-stock/{productId}")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAvailableStockByProduct(@PathVariable Long productId) {
        try {
            List<InventoryResponse> availableStock = inventoryService.getAvailableStockForProduct(productId);
            return ResponseEntity.ok(ApiResponse.success(availableStock));
        } catch (Exception e) {
            logger.error("Error fetching available stock for product: {}", productId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch available stock"));
        }
    }

    @GetMapping("/total-available/{productId}")
    public ResponseEntity<ApiResponse<Integer>> getTotalAvailableStock(@PathVariable Long productId) {
        try {
            int totalAvailable = inventoryService.getTotalAvailableStock(productId);
            return ResponseEntity.ok(ApiResponse.success("Total available stock retrieved", totalAvailable));
        } catch (Exception e) {
            logger.error("Error fetching total available stock for product: {}", productId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch total available stock"));
        }
    }

    @GetMapping("/check-availability/{productId}/{quantity}")
    public ResponseEntity<ApiResponse<Boolean>> checkStockAvailability(@PathVariable Long productId, @PathVariable int quantity) {
        try {
            boolean isAvailable = inventoryService.isStockAvailable(productId, quantity);
            return ResponseEntity.ok(ApiResponse.success("Stock availability checked", isAvailable));
        } catch (Exception e) {
            logger.error("Error checking stock availability for product: {} quantity: {}", productId, quantity, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to check stock availability"));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInventorySummary() {
        try {
            logger.info("Fetching inventory summary...");
            Map<String, Object> summary = inventoryService.getInventorySummary();
            logger.info("Inventory summary fetched successfully: {}", summary);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            logger.error("Error fetching inventory summary", e);
            
            Map<String, Object> defaultSummary = new HashMap<>();
            defaultSummary.put("productsWithStock", 0);
            defaultSummary.put("totalInventoryValue", 0);
            defaultSummary.put("lowStockProducts", 0);
            defaultSummary.put("totalBatches", 0);
            defaultSummary.put("error", "Summary temporarily unavailable");
            
            return ResponseEntity.ok(ApiResponse.success(defaultSummary));
        }
    }

    @GetMapping("/aging-report")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryAgingReport() {
        try {
            List<InventoryResponse> agingReport = inventoryService.getInventoryAgingReport();
            return ResponseEntity.ok(ApiResponse.success(agingReport));
        } catch (Exception e) {
            logger.error("Error fetching inventory aging report", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch inventory aging report"));
        }
    }

    // ===================== NEW FIFO ENDPOINTS FOR FRONTEND =====================

    /**
     * Get FIFO stock entries for a product (Frontend compatible)
     * Maps existing inventory IN movements to stock entries format
     */
    @GetMapping("/product/{productId}/entries")
    public ResponseEntity<?> getProductStockEntries(@PathVariable Long productId) {
        try {
            logger.info("Fetching stock entries for product ID: {}", productId);
            
            List<Map<String, Object>> stockEntries = inventoryService.getProductStockEntries(productId);
            
            // Return in format expected by frontend
            Map<String, Object> response = new HashMap<>();
            response.put("data", stockEntries);
            response.put("message", "Stock entries retrieved successfully");
            response.put("count", stockEntries.size());
            
            logger.info("Returning {} stock entries for product {}", stockEntries.size(), productId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Validation error for product {}: {}", productId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching stock entries for product {}: ", productId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch stock entries"));
        }
    }

    /**
     * Add a new stock entry (Frontend compatible)
     * Creates an inventory IN movement from the stock entry data
     */
    @PostMapping("/product/{productId}/entries")
    public ResponseEntity<?> addStockEntry(@PathVariable Long productId, @RequestBody Map<String, Object> stockData) {
        try {
            logger.info("Adding stock entry for product ID: {}", productId);
            logger.debug("Stock data received: {}", stockData);
            
            // Validate required fields
            if (!stockData.containsKey("quantity") || !stockData.containsKey("purchasePrice")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Quantity and purchase price are required"));
            }
            
            Map<String, Object> result = inventoryService.addStockEntry(productId, stockData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Stock entry added successfully");
            response.put("data", result);
            
            logger.info("Stock entry added successfully for product {}", productId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Validation error adding stock entry for product {}: {}", productId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding stock entry for product {}: ", productId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to add stock entry: " + e.getMessage()));
        }
    }

    /**
     * Get stock entry by ID (Optional - for future use)
     */
    @GetMapping("/entries/{entryId}")
    public ResponseEntity<?> getStockEntryById(@PathVariable Long entryId) {
        try {
            // This would require additional service method implementation
            // For now, return a simple response
            return ResponseEntity.ok(Map.of("message", "Stock entry details endpoint - to be implemented"));
        } catch (Exception e) {
            logger.error("Error fetching stock entry {}: ", entryId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch stock entry"));
        }
    }

    /**
     * Update stock entry (Optional - for future use)
     */
    @PostMapping("/entries/{entryId}/update")
    public ResponseEntity<?> updateStockEntry(@PathVariable Long entryId, @RequestBody Map<String, Object> updateData) {
        try {
            // This would require additional service method implementation
            // For now, return a simple response
            return ResponseEntity.ok(Map.of("message", "Stock entry update endpoint - to be implemented"));
        } catch (Exception e) {
            logger.error("Error updating stock entry {}: ", entryId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update stock entry"));
        }
    }

    // Add these endpoints to your InventoryController class

/**
 * Delete an inventory entry by ID
 */
@DeleteMapping("/{inventoryId}")
public ResponseEntity<ApiResponse<String>> deleteInventoryEntry(@PathVariable Long inventoryId) {
    try {
        inventoryService.deleteInventoryEntry(inventoryId);
        logger.info("Inventory entry deleted successfully with ID: {}", inventoryId);
        return ResponseEntity.ok(ApiResponse.success("Inventory entry deleted successfully", null));
    } catch (RuntimeException e) {
        logger.error("Inventory entry not found or validation error: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
        logger.error("Error deleting inventory entry with ID: {}", inventoryId, e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to delete inventory entry"));
    }
}

/**
 * Delete a stock entry for a specific product (Frontend compatible)
 */
@DeleteMapping("/product/{productId}/entries/{entryId}")
public ResponseEntity<?> deleteStockEntry(@PathVariable Long productId, @PathVariable Long entryId) {
    try {
        logger.info("Deleting stock entry {} for product {}", entryId, productId);
        
        inventoryService.deleteStockEntry(productId, entryId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Stock entry deleted successfully");
        response.put("entryId", entryId);
        response.put("productId", productId);
        
        logger.info("Stock entry {} deleted successfully for product {}", entryId, productId);
        return ResponseEntity.ok(response);
        
    } catch (IllegalArgumentException e) {
        logger.error("Validation error deleting stock entry {} for product {}: {}", entryId, productId, e.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (RuntimeException e) {
        logger.error("Stock entry not found: {}", e.getMessage());
        return ResponseEntity.notFound().build();
    } catch (Exception e) {
        logger.error("Error deleting stock entry {} for product {}: ", entryId, productId, e);
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to delete stock entry"));
    }
}

/**
 * Bulk delete inventory entries (Optional - for advanced operations)
 */

}
