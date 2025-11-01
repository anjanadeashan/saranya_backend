package org.example.inventorymanagementbackend.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.example.inventorymanagementbackend.dto.request.SaleItemRequest;
import org.example.inventorymanagementbackend.dto.request.SaleRequest;
import org.example.inventorymanagementbackend.dto.response.ApiResponse;
import org.example.inventorymanagementbackend.dto.response.SaleResponse;
import org.example.inventorymanagementbackend.entity.Sale;
import org.example.inventorymanagementbackend.entity.Sale.PaymentMethod;
import org.example.inventorymanagementbackend.repository.SaleRepository;
import org.example.inventorymanagementbackend.service.SaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SaleController {

    private static final Logger logger = LoggerFactory.getLogger(SaleController.class);

    @Autowired
    private SaleService saleService;

    @Autowired

    private SaleRepository saleRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getAllSales() {
        try {
            List<SaleResponse> sales = saleService.getAllSales();
            return ResponseEntity.ok(ApiResponse.success(sales));
        } catch (Exception e) {
            logger.error("Error fetching sales", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch sales"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleById(@PathVariable Long id) {
        try {
            SaleResponse sale = saleService.getSaleById(id);
            return ResponseEntity.ok(ApiResponse.success(sale));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching sale with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch sale"));
        }
    }

    /**
     * ENHANCED: Create sale with comprehensive stock validation
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(@Valid @RequestBody SaleRequest request, BindingResult bindingResult) {
        
        logger.info("=== SALE CREATION REQUEST ===");
        logger.info("Customer ID: {}, Payment Method: {}", request.getCustomerId(), request.getPaymentMethod());
        logger.info("Total Amount: {}, Items Count: {}", 
            request.getTotalAmount(), 
            request.getSaleItems() != null ? request.getSaleItems().size() : 0);
        
        // Log sale items concisely
        if (request.getSaleItems() != null) {
            for (int i = 0; i < request.getSaleItems().size(); i++) {
                SaleItemRequest item = request.getSaleItems().get(i);
                logger.info("Item {}: ProductID={}, Quantity={}, UnitPrice={}, Discount={}", 
                    i, item.getProductId(), item.getQuantity(), item.getUnitPrice(), item.getDiscount());
            }
        }

        // Handle Bean Validation errors
        if (bindingResult.hasErrors()) {
            logger.warn("=== VALIDATION ERRORS DETECTED ===");
            Map<String, String> errors = new HashMap<>();
            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
            
            for (FieldError error : bindingResult.getFieldErrors()) {
                String fieldName = error.getField();
                String message = error.getDefaultMessage();
                Object rejectedValue = error.getRejectedValue();
                
                logger.warn("Field '{}': {} (Rejected value: {})", fieldName, message, rejectedValue);
                errors.put(fieldName, message);
                errorMessage.append(fieldName).append(": ").append(message).append("; ");
            }
            
            logger.warn("Total validation errors: {}", bindingResult.getErrorCount());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(errorMessage.toString().trim()));
        }

        // Business validation
        try {
            validateBusinessRules(request);
        } catch (IllegalArgumentException e) {
            logger.warn("Business validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }

        // STOCK PRE-CHECK: Validate stock availability BEFORE processing
        try {
            logger.info("Pre-checking stock availability for all items...");
            boolean stockAvailable = saleService.checkStockAvailabilityForSale(request);
            if (!stockAvailable) {
                logger.warn("Stock availability pre-check failed");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Insufficient stock for one or more items in the sale"));
            }
            logger.info("Stock availability pre-check passed");
        } catch (Exception e) {
            logger.error("Error during stock pre-check: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unable to verify stock availability: " + e.getMessage()));
        }

        // Process the sale
        try {
            logger.info("Processing sale creation with validated stock...");
            SaleResponse sale = saleService.createSaleWithFIFO(request);
            
            logger.info("Sale created successfully - ID: {}, Final Total: {}", 
                sale.getId(), sale.getTotalAmount());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Sale created successfully", sale));
                    
        } catch (SaleService.ValidationException e) {
            logger.warn("Sale validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
                    
        } catch (SaleService.InsufficientStockException e) {
            logger.warn("Insufficient stock error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Stock Error: " + e.getMessage()));
                    
        } catch (RuntimeException e) {
            logger.error("Runtime error creating sale: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Sale creation failed: " + e.getMessage()));
            
        } catch (Exception e) {
            logger.error("System error creating sale", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("System error occurred. Please try again."));
        }
    }

    /**
     * ENHANCED: Additional business rule validation with stock checks
     */
    private void validateBusinessRules(SaleRequest request) {
        // Validate sale items exist
        if (request.getSaleItems() == null || request.getSaleItems().isEmpty()) {
            throw new IllegalArgumentException("Sale must contain at least one item");
        }

        // Validate each sale item
        for (int i = 0; i < request.getSaleItems().size(); i++) {
            SaleItemRequest item = request.getSaleItems().get(i);
            
            if (item.getProductId() == null) {
                throw new IllegalArgumentException("Product ID is required for item " + (i + 1));
            }
            
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for item " + (i + 1) + 
                    " (current value: " + item.getQuantity() + ")");
            }
            
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Unit price must be greater than zero for item " + (i + 1));
            }

            // Additional quantity validation
            if (item.getQuantity() > 1000) {
                throw new IllegalArgumentException("Quantity cannot exceed 1000 units for item " + (i + 1));
            }

            // Validate discount
            if (item.getDiscount() != null && item.getDiscount().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Discount cannot be negative for item " + (i + 1));
            }

            // Ensure line total would be positive
            BigDecimal discount = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;
            BigDecimal lineTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .subtract(discount);
            
            if (lineTotal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Line total must be positive for item " + (i + 1) + 
                    ". Check unit price and discount values.");
            }
        }

        // Validate check payment fields
        if (request.getPaymentMethod() == Sale.PaymentMethod.CREDIT_CHECK) {
            if (request.getCheckNumber() == null || request.getCheckNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Check number is required for check payments");
            }
            if (request.getBankName() == null || request.getBankName().trim().isEmpty()) {
                throw new IllegalArgumentException("Bank name is required for check payments");
            }
            if (request.getCheckDate() == null) {
                throw new IllegalArgumentException("Check date is required for check payments");
            }
        }

        // Optional: Validate total amount if provided (since your service calculates it)
        if (request.getTotalAmount() != null) {
            BigDecimal calculatedTotal = calculateExpectedTotal(request.getSaleItems());
            if (request.getTotalAmount().compareTo(calculatedTotal) != 0) {
                logger.warn("Total amount mismatch - Provided: {}, Calculated: {}", 
                    request.getTotalAmount(), calculatedTotal);
                // Uncomment below to enforce strict total validation
                // throw new IllegalArgumentException("Total amount does not match calculated total");
            }
        }
    }

    /**
     * Calculate expected total for validation
     */
    private BigDecimal calculateExpectedTotal(List<SaleItemRequest> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItemRequest item : items) {
            if (item.getUnitPrice() != null && item.getQuantity() != null) {
                BigDecimal discount = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;
                BigDecimal itemTotal = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .subtract(discount);
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSale(@PathVariable Long id) {
        try {
            logger.info("Deleting sale with id: {}", id);
            saleService.deleteSale(id);
            logger.info("Sale deleted successfully with id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Sale deleted successfully", null));
        } catch (RuntimeException e) {
            logger.warn("Sale not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting sale with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete sale"));
        }
    }

    @GetMapping("/unpaid")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getUnpaidSales() {
        try {
            List<SaleResponse> sales = saleService.getUnpaidSales();
            return ResponseEntity.ok(ApiResponse.success(sales));
        } catch (Exception e) {
            logger.error("Error fetching unpaid sales", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch unpaid sales"));
        }
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<ApiResponse<SaleResponse>> markSaleAsPaid(@PathVariable Long id) {
        try {
            logger.info("Marking sale as paid: {}", id);
            SaleResponse sale = saleService.markSaleAsPaid(id);
            logger.info("Sale marked as paid successfully with id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Sale marked as paid", sale));
        } catch (RuntimeException e) {
            logger.warn("Sale not found or already paid: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error marking sale as paid with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to mark sale as paid"));
        }
    }

    /**
     * ENHANCED: Check stock availability with detailed response
     */
    @PostMapping("/check-stock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStockAvailability(@RequestBody SaleRequest request) {
        try {
            logger.info("Checking stock availability for sale request");
            
            boolean available = saleService.checkStockAvailabilityForSale(request);
            Map<String, Object> response = new HashMap<>();
            response.put("stockAvailable", available);
            response.put("itemsChecked", request.getSaleItems().size());
            
            if (!available) {
                response.put("message", "One or more items have insufficient stock");
            } else {
                response.put("message", "All items have sufficient stock");
            }
            
            logger.info("Stock availability check result: {}", available);
            return ResponseEntity.ok(ApiResponse.success("Stock availability checked", response));
            
        } catch (Exception e) {
            logger.error("Error checking stock availability", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("stockAvailable", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to check stock availability"));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getSalesByCustomer(@PathVariable Long customerId) {
        try {
            logger.info("Fetching sales for customer: {}", customerId);
            List<SaleResponse> sales = saleService.getSalesByCustomer(customerId);
            logger.info("Found {} sales for customer: {}", sales.size(), customerId);
            return ResponseEntity.ok(ApiResponse.success(sales));
        } catch (Exception e) {
            logger.error("Error fetching sales for customer: {}", customerId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch sales for customer"));
        }
    }









   // Add these endpoints to your existing SaleController.java
// Make sure you have @Autowired SaleService saleService; at the top of your controller

@PutMapping("/{id}/mark-check-bounced")
public ResponseEntity<?> markCheckAsBounced(
    @PathVariable Long id,
    @RequestBody(required = false) Map<String, String> requestBody) {
    try {
        String bouncedNotes = null;
        if (requestBody != null && requestBody.containsKey("bouncedNotes")) {
            bouncedNotes = requestBody.get("bouncedNotes");
        }
        
        Sale updatedSale = saleService.markCheckAsBounced(id, bouncedNotes);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Check marked as bounced successfully",
            "data", updatedSale
        ));
    } catch (IllegalStateException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, "message", e.getMessage()));
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("success", false, "message", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("success", false, "message", "An error occurred: " + e.getMessage()));
    }
}

@PutMapping("/{id}/mark-check-cleared")
public ResponseEntity<?> markCheckAsCleared(@PathVariable Long id) {
    try {
        Sale updatedSale = saleService.clearBouncedCheckStatus(id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Check status cleared successfully",
            "data", updatedSale
        ));
    } catch (IllegalStateException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, "message", e.getMessage()));
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("success", false, "message", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("success", false, "message", "An error occurred: " + e.getMessage()));
    }
}

@GetMapping("/bounced-checks/summary")
public ResponseEntity<?> getBouncedChecksSummary() {
    try {
        Map<String, Object> summary = saleService.getBouncedChecksSummary();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", summary
        ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("success", false, "message", "An error occurred: " + e.getMessage()));
    }
}

// Optional: Additional endpoint to get all bounced checks
@GetMapping("/bounced-checks")
public ResponseEntity<?> getAllBouncedChecks() {
    try {
        List<Sale> bouncedChecks = saleService.getAllBouncedChecks();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", bouncedChecks
        ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("success", false, "message", "An error occurred: " + e.getMessage()));
    }
}

// Optional: Get bounced checks for a specific customer
@GetMapping("/bounced-checks/customer/{customerId}")
public ResponseEntity<?> getBouncedChecksByCustomer(@PathVariable Long customerId) {
    try {
        List<Sale> bouncedChecks = saleService.getBouncedChecksByCustomer(customerId);
        long count = bouncedChecks.size();
        double totalAmount = saleService.getTotalBouncedAmountByCustomer(customerId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("customerId", customerId);
        result.put("bouncedChecksCount", count);
        result.put("totalBouncedAmount", totalAmount);
        result.put("bouncedChecks", bouncedChecks);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", result
        ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("success", false, "message", "An error occurred: " + e.getMessage()));
    }
}
}