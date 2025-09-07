package org.example.inventorymanagementbackend.controller;

import org.example.inventorymanagementbackend.dto.request.InventoryRequest;
import org.example.inventorymanagementbackend.dto.response.ApiResponse;
import org.example.inventorymanagementbackend.dto.response.InventoryResponse;
import org.example.inventorymanagementbackend.service.InventoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryService inventoryService;

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
}
