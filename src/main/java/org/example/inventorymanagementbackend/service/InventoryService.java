package org.example.inventorymanagementbackend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.inventorymanagementbackend.dto.request.InventoryRequest;
import org.example.inventorymanagementbackend.dto.response.InventoryResponse;
import org.example.inventorymanagementbackend.entity.Inventory;
import org.example.inventorymanagementbackend.entity.Product;
import org.example.inventorymanagementbackend.entity.Supplier;
import org.example.inventorymanagementbackend.mapper.InventoryMapper;
import org.example.inventorymanagementbackend.repository.InventoryRepository;
import org.example.inventorymanagementbackend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;

    

    // Custom Exceptions
    public static class InventoryValidationException extends RuntimeException {
        public InventoryValidationException(String message) {
            super(message);
        }
    }

    public static class InsufficientInventoryException extends RuntimeException {
        public InsufficientInventoryException(String message) {
            super(message);
        }
    }

    // ===================== EXISTING METHODS =====================

    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventoryMovements() {
        try {
            List<Inventory> movements = inventoryRepository.findAllOrderByDateDesc();
            return movements.stream()
                    .map(inventoryMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all inventory movements: ", e);
            throw new RuntimeException("Failed to fetch inventory movements", e);
        }
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByProduct(Long productId) {
        try {
            Product product = productService.getProductEntityById(productId);
            List<Inventory> movements = inventoryRepository.findByProductOrderByDateDesc(product);
            return movements.stream()
                    .map(inventoryMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching inventory for product ID {}: ", productId, e);
            throw new RuntimeException("Failed to fetch inventory for product", e);
        }
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getAvailableStockForProduct(Long productId) {
        try {
            List<Inventory> availableStock = inventoryRepository.findAvailableStockForProductFIFO(productId);
            return availableStock.stream()
                    .map(inventoryMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching available stock for product ID {}: ", productId, e);
            throw new RuntimeException("Failed to fetch available stock for product", e);
        }
    }

    /**
     * ENHANCED: Check if sufficient stock is available for a product
     */
    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long productId, int requiredQuantity) {
        try {
            if (requiredQuantity <= 0) {
                logger.debug("No stock required for product {}", productId);
                return true;
            }
            
            List<Inventory> availableBatches = inventoryRepository.findAvailableStockForProductFIFO(productId);
            int totalAvailable = availableBatches.stream()
                    .mapToInt(Inventory::getQuantity)
                    .sum();
            
            boolean available = totalAvailable >= requiredQuantity;
            logger.debug("Stock check for product {}: Required={}, Available={}, Sufficient={}", 
                productId, requiredQuantity, totalAvailable, available);
            
            return available;
        } catch (Exception e) {
            logger.error("Error checking stock availability for product ID {}: ", productId, e);
            return false;
        }
    }

    /**
     * ENHANCED: Get total available stock for a product (only positive quantities)
     */
    @Transactional(readOnly = true)
    public int getTotalAvailableStock(Long productId) {
        try {
            // Try using the enhanced repository method first
            Integer stock = null;
            try {
                stock = inventoryRepository.getTotalAvailableStockForProduct(productId);
            } catch (Exception e) {
                logger.debug("Repository method not available, using fallback approach");
            }
            
            if (stock != null) {
                int availableStock = stock;
                logger.debug("Total available stock for product {} (from repository): {}", productId, availableStock);
                return availableStock;
            }
            
            // Fallback method
            List<Inventory> availableBatches = inventoryRepository.findAvailableStockForProductFIFO(productId);
            int totalStock = availableBatches.stream()
                    .mapToInt(Inventory::getQuantity)
                    .sum();
                    
            logger.debug("Total available stock for product {} (from calculation): {}", productId, totalStock);
            return totalStock;
            
        } catch (Exception e) {
            logger.error("Error fetching total available stock for product ID {}: ", productId, e);
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInventorySummary() {
        try {
            Map<String, Object> summary = new HashMap<>();
            
            List<Inventory> allStock;
            try {
                allStock = inventoryRepository.findOldestAvailableStock();
            } catch (Exception e) {
                allStock = inventoryRepository.findByQuantityGreaterThan(0);
            }
            
            if (allStock == null) {
                allStock = new ArrayList<>();
            }
            
            long productsWithStock = allStock.stream()
                    .filter(inv -> inv != null && inv.getProduct() != null)
                    .map(inv -> inv.getProduct().getId())
                    .distinct()
                    .count();
            
            BigDecimal totalValue = allStock.stream()
                    .filter(inv -> inv != null && inv.getUnitPrice() != null && inv.getQuantity() != null)
                    .map(inv -> {
                        try {
                            return inv.getUnitPrice().multiply(BigDecimal.valueOf(inv.getQuantity()));
                        } catch (Exception e) {
                            logger.warn("Error calculating value for inventory item: " + inv.getId(), e);
                            return BigDecimal.ZERO;
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            long lowStockCount = 0;
            try {
                Map<Long, Integer> productStockMap = allStock.stream()
                        .filter(inv -> inv != null && inv.getProduct() != null)
                        .collect(Collectors.groupingBy(
                            inv -> inv.getProduct().getId(),
                            Collectors.summingInt(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0)
                        ));
                
                lowStockCount = productStockMap.entrySet().stream()
                        .filter(entry -> {
                            try {
                                Product product = productService.getProductEntityById(entry.getKey());
                                if (product != null && product.getLowStockThreshold() != null) {
                                    return entry.getValue() <= product.getLowStockThreshold();
                                }
                                return false;
                            } catch (Exception e) {
                                logger.warn("Error checking low stock for product: " + entry.getKey(), e);
                                return false;
                            }
                        })
                        .count();
            } catch (Exception e) {
                logger.warn("Error calculating low stock count", e);
                lowStockCount = 0;
            }
            
            summary.put("productsWithStock", productsWithStock);
            summary.put("totalInventoryValue", totalValue);
            summary.put("lowStockProducts", lowStockCount);
            summary.put("totalBatches", allStock.size());
            
            logger.info("Inventory summary calculated successfully: " + summary);
            return summary;
            
        } catch (Exception e) {
            logger.error("Error fetching inventory summary: ", e);
            
            Map<String, Object> emptySummary = new HashMap<>();
            emptySummary.put("productsWithStock", 0L);
            emptySummary.put("totalInventoryValue", BigDecimal.ZERO);
            emptySummary.put("lowStockProducts", 0L);
            emptySummary.put("totalBatches", 0);
            emptySummary.put("error", "Failed to calculate inventory summary");
            
            return emptySummary;
        }
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryAgingReport() {
        try {
            List<Inventory> oldestStock = inventoryRepository.findOldestAvailableStock();
            return oldestStock.stream()
                    .map(inventoryMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching inventory aging report: ", e);
            throw new RuntimeException("Failed to fetch inventory aging report", e);
        }
    }

    public InventoryResponse recordInventoryMovement(InventoryRequest request) {
        logger.info("Recording inventory movement for product: {}", request.getProductId());
        logger.debug("Request details - MovementType: {}, Quantity: {}, UnitPrice: {}, SupplierId: {}",
                request.getMovementType(), request.getQuantity(), request.getUnitPrice(), request.getSupplierId());

        try {
            Product product = productService.getProductEntityById(request.getProductId());
            logger.debug("Found product: {} (ID: {})", product.getName(), product.getId());

            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantity(request.getQuantity());
            inventory.setReference(request.getReference());
            inventory.setDate(request.getDate());

            if (request.getUnitPrice() != null) {
                inventory.setUnitPrice(BigDecimal.valueOf(request.getUnitPrice()));
                logger.debug("Set unit price: {}", request.getUnitPrice());
            }

            Inventory.MovementType movementType = parseMovementType(request.getMovementType());
            inventory.setMovementType(movementType);

            if (movementType == Inventory.MovementType.IN) {
                if (request.getSupplierId() == null) {
                    logger.error("Supplier ID is null for IN movement");
                    throw new IllegalArgumentException("Supplier is required for stock IN movements");
                }

                logger.debug("Looking for supplier with ID: {}", request.getSupplierId());
                Supplier supplier = supplierService.getSupplierEntityById(request.getSupplierId());

                logger.debug("Found supplier: {} (ID: {})", supplier.getName(), supplier.getId());
                inventory.setSupplier(supplier);
            }

            if (movementType == Inventory.MovementType.OUT) {
                logger.debug("Validating stock for OUT movement");
                if (!isStockAvailable(request.getProductId(), request.getQuantity())) {
                    logger.warn("Insufficient stock for product: {} (requested: {})",
                            product.getName(), request.getQuantity());
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
                }
            }

            logger.debug("Saving inventory movement");
            Inventory savedInventory = inventoryRepository.save(inventory);
            logger.debug("Inventory movement saved with ID: {}", savedInventory.getId());

            boolean isAddition = movementType == Inventory.MovementType.IN;
            logger.debug("Updating product stock, isAddition: {}, quantity: {}", isAddition, request.getQuantity());
            productService.updateProductStock(request.getProductId(), request.getQuantity(), isAddition);

            logger.info("Inventory movement recorded successfully with ID: {}", savedInventory.getId());
            return inventoryMapper.toResponse(savedInventory);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error while recording inventory movement: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error recording inventory movement: ", e);
            throw new RuntimeException("Failed to record inventory movement: " + e.getMessage(), e);
        }
    }

    private Inventory.MovementType parseMovementType(String movementType) {
        try {
            return Inventory.MovementType.valueOf(movementType.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid movement type: {}", movementType);
            throw new IllegalArgumentException("Invalid movement type: " + movementType + ". Must be IN or OUT");
        }
    }

    // ===================== ENHANCED METHODS FOR STOCK VALIDATION =====================

    /**
     * ENHANCED: Get detailed stock information for validation and debugging
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStockDetails(Long productId) {
        try {
            Map<String, Object> details = new HashMap<>();
            
            // Get inventory batches
            List<Inventory> activeBatches = inventoryRepository.findAvailableStockForProductFIFO(productId);
            List<Inventory> allBatches = inventoryRepository.findByProductIdOrderByDateAsc(productId);
            
            int inventoryTotal = activeBatches.stream().mapToInt(Inventory::getQuantity).sum();
            int depletedBatches = (int) allBatches.stream().filter(i -> i.getQuantity() == 0).count();
            
            // Get product current stock
            Product product = productService.getProductEntityById(productId);
            int productStock = product.getCurrentStock();
            
            details.put("productId", productId);
            details.put("productName", product.getName());
            details.put("productCode", product.getProductCode());
            details.put("inventoryTotal", inventoryTotal);
            details.put("productStock", productStock);
            details.put("activeBatchCount", activeBatches.size());
            details.put("totalBatchCount", allBatches.size());
            details.put("depletedBatchCount", depletedBatches);
            details.put("activeBatches", activeBatches.stream().map(inventoryMapper::toResponse).collect(Collectors.toList()));
            details.put("stockDiscrepancy", Math.abs(inventoryTotal - productStock));
            
            logger.debug("Stock details for product {}: Inventory={}, Product={}, Active Batches={}", 
                productId, inventoryTotal, productStock, activeBatches.size());
            
            return details;
            
        } catch (Exception e) {
            logger.error("Error getting stock details for product {}: {}", productId, e.getMessage(), e);
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("error", "Unable to retrieve stock details");
            errorDetails.put("productId", productId);
            return errorDetails;
        }
    }

    /**
     * ENHANCED: Validate stock sufficiency with detailed error reporting
     */
    @Transactional(readOnly = true)
    public void validateStockSufficiency(Long productId, Integer requiredQuantity) throws InsufficientInventoryException {
        try {
            if (requiredQuantity == null || requiredQuantity <= 0) {
                return; // No validation needed
            }

            Product product = productService.getProductEntityById(productId);
            int availableStock = getTotalAvailableStock(productId);
            
            if (availableStock < requiredQuantity) {
                String error = String.format(
                    "Insufficient stock for product '%s' (ID: %d, Code: %s). " +
                    "Required: %d, Available: %d, Shortfall: %d",
                    product.getName(), productId, product.getProductCode(),
                    requiredQuantity, availableStock, (requiredQuantity - availableStock));
                
                logger.warn("Stock validation failed: {}", error);
                throw new InsufficientInventoryException(error);
            }
            
            logger.debug("Stock validation passed for product {}: Required={}, Available={}", 
                productId, requiredQuantity, availableStock);
                
        } catch (InsufficientInventoryException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error validating stock sufficiency for product {}: {}", productId, e.getMessage(), e);
            throw new InsufficientInventoryException("Unable to validate stock for product: " + productId);
        }
    }

    /**
     * ENHANCED: Get FIFO batches with detailed information
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFIFOBatchDetails(Long productId, Integer requestedQuantity) {
        try {
            List<Inventory> fifoBatches = inventoryRepository.findAvailableStockForProductFIFO(productId);
            List<Map<String, Object>> batchDetails = new ArrayList<>();
            
            int remainingQuantity = requestedQuantity != null ? requestedQuantity : 0;
            
            for (Inventory batch : fifoBatches) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("id", batch.getId());
                detail.put("date", batch.getDate());
                detail.put("quantity", batch.getQuantity());
                detail.put("unitPrice", batch.getUnitPrice());
                detail.put("expiryDate", batch.getExpiryDate());
                
                if (requestedQuantity != null && requestedQuantity > 0) {
                    int quantityUsed = Math.min(remainingQuantity, batch.getQuantity());
                    detail.put("quantityUsed", quantityUsed);
                    detail.put("quantityRemaining", batch.getQuantity() - quantityUsed);
                    remainingQuantity -= quantityUsed;
                } else {
                    detail.put("quantityUsed", 0);
                    detail.put("quantityRemaining", batch.getQuantity());
                }
                
                batchDetails.add(detail);
                
                if (remainingQuantity <= 0) {
                    break;
                }
            }
            
            return batchDetails;
            
        } catch (Exception e) {
            logger.error("Error getting FIFO batch details for product {}: {}", productId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ===================== NEW FIFO METHODS FOR FRONTEND =====================

    /**
     * Get FIFO stock entries for a product (for frontend compatibility)
     * Maps existing inventory IN movements to stock entries format
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductStockEntries(Long productId) {
        try {
            logger.info("Fetching stock entries for product ID: {}", productId);
            
            // Use a fallback method if the specific repository method doesn't exist
            List<Inventory> stockMovements = new ArrayList<>();
            try {
                // Try the specific method first - you may need to add this to your repository
                // stockMovements = inventoryRepository.findByProductIdAndMovementTypeOrderByDateAsc(
                //     productId, Inventory.MovementType.IN);
                
                // For now, use fallback approach
                Product product = productService.getProductEntityById(productId);
                stockMovements = inventoryRepository.findByProductOrderByDateDesc(product)
                    .stream()
                    .filter(inv -> inv.getMovementType() == Inventory.MovementType.IN)
                    .sorted((a, b) -> a.getDate().compareTo(b.getDate())) // Sort by date ascending for FIFO
                    .collect(Collectors.toList());
            } catch (Exception e) {
                logger.warn("Error fetching inventory movements: {}", e.getMessage());
                stockMovements = new ArrayList<>();
            }
            
            logger.debug("Found {} stock movements for product {}", stockMovements.size(), productId);
            
            List<Map<String, Object>> result = stockMovements.stream()
                .filter(inv -> inv.getQuantity() > 0) // Only show entries with remaining stock
                .map(this::mapInventoryToStockEntry)
                .collect(Collectors.toList());
                
            logger.info("Returning {} stock entries for product {}", result.size(), productId);
            return result;
            
        } catch (Exception e) {
            logger.error("Error fetching stock entries for product ID {}: ", productId, e);
            // Return empty list instead of throwing exception to prevent UI breakage
            return new ArrayList<>();
        }
    }

    /**
     * Add a new stock entry (compatible with frontend) - SIMPLIFIED VERSION
     */
    @Transactional
    public Map<String, Object> addStockEntry(Long productId, Map<String, Object> stockData) {
        try {
            logger.info("Adding stock entry for product ID: {}", productId);
            logger.debug("Stock data: {}", stockData);
            
            // Create inventory request from stock entry data
            InventoryRequest request = new InventoryRequest();
            request.setProductId(productId);
            request.setQuantity(((Number) stockData.get("quantity")).intValue());
            request.setUnitPrice(((Number) stockData.get("purchasePrice")).doubleValue());
            request.setMovementType("IN");
            
            // Create reference from batch number if available
            String batchNumber = (String) stockData.get("batchNumber");
            if (batchNumber != null && !batchNumber.trim().isEmpty()) {
                request.setReference("BATCH-" + batchNumber);
            } else {
                request.setReference("STOCK-" + System.currentTimeMillis());
            }
            
            // Set current date - use LocalDateTime
            request.setDate(LocalDateTime.now());
            
            // Use default supplier ID - make sure supplier with ID 1 exists in your database
            request.setSupplierId(1L);
            
            // Record the inventory movement
            InventoryResponse response = recordInventoryMovement(request);
            
            // Return in format expected by frontend
            Map<String, Object> result = new HashMap<>();
            result.put("id", response.getId());
            result.put("quantity", response.getQuantity());
            result.put("remainingQuantity", response.getQuantity()); // Initially same as quantity
            result.put("purchasePrice", response.getUnitPrice());
            result.put("batchNumber", stockData.get("batchNumber"));
            result.put("supplier", stockData.get("supplier"));
            result.put("dateAdded", response.getDate());
            result.put("expiryDate", stockData.get("expiryDate"));
            result.put("notes", stockData.get("notes"));
            
            logger.info("Stock entry added successfully with ID: {}", response.getId());
            return result;
            
        } catch (Exception e) {
            logger.error("Error adding stock entry for product ID {}: ", productId, e);
            throw new RuntimeException("Failed to add stock entry: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to map Inventory entity to stock entry format
     */
    private Map<String, Object> mapInventoryToStockEntry(Inventory inventory) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", inventory.getId());
        entry.put("quantity", inventory.getQuantity() != null ? inventory.getQuantity() : 0);
        entry.put("remainingQuantity", inventory.getQuantity() != null ? inventory.getQuantity() : 0);
        entry.put("purchasePrice", inventory.getUnitPrice() != null ? inventory.getUnitPrice() : BigDecimal.ZERO);
        entry.put("batchNumber", inventory.getReference() != null ? inventory.getReference() : "N/A");
        entry.put("supplier", inventory.getSupplier() != null ? inventory.getSupplier().getName() : "Unknown");
        entry.put("dateAdded", inventory.getDate() != null ? inventory.getDate() : new java.util.Date());
        entry.put("expiryDate", null); // Add expiry date field to Inventory entity if needed
        entry.put("notes", "Stock entry from inventory system");
        return entry;
    }

    // ===================== INVENTORY MANAGEMENT METHODS =====================

    /**
     * Delete an inventory entry by ID
     * This will also update the product's current stock accordingly
     */
    @Transactional
    public void deleteInventoryEntry(Long inventoryId) {
        try {
            logger.info("Deleting inventory entry with ID: {}", inventoryId);
            
            // Find the inventory entry
            Inventory inventory = inventoryRepository.findById(inventoryId)
                    .orElseThrow(() -> new RuntimeException("Inventory entry not found with id: " + inventoryId));
            
            // Store details for stock adjustment
            Product product = inventory.getProduct();
            Integer quantity = inventory.getQuantity();
            Inventory.MovementType movementType = inventory.getMovementType();
            
            logger.debug("Found inventory entry - Product: {}, Quantity: {}, Type: {}", 
                    product.getName(), quantity, movementType);
            
            // Delete the inventory entry
            inventoryRepository.delete(inventory);
            logger.debug("Inventory entry deleted from database");
            
            // Adjust product stock based on movement type
            // If it was an IN movement, we need to subtract from current stock
            // If it was an OUT movement, we need to add back to current stock
            boolean isSubtraction = (movementType == Inventory.MovementType.IN);
            
            logger.debug("Adjusting product stock - isSubtraction: {}, quantity: {}", isSubtraction, quantity);
            productService.updateProductStock(product.getId(), quantity, !isSubtraction);
            
            logger.info("Inventory entry {} deleted successfully and product stock adjusted", inventoryId);
            
        } catch (RuntimeException e) {
            logger.error("Error deleting inventory entry {}: {}", inventoryId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting inventory entry {}: ", inventoryId, e);
            throw new RuntimeException("Failed to delete inventory entry: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a stock entry (Frontend compatible method)
     */
    @Transactional
    public void deleteStockEntry(Long productId, Long entryId) {
        try {
            logger.info("Deleting stock entry {} for product {}", entryId, productId);
            
            // Verify the entry belongs to the specified product
            Inventory inventory = inventoryRepository.findById(entryId)
                    .orElseThrow(() -> new RuntimeException("Stock entry not found with id: " + entryId));
            
            if (!inventory.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("Stock entry does not belong to the specified product");
            }
            
            // Only allow deletion of IN movements (stock additions)
            if (inventory.getMovementType() != Inventory.MovementType.IN) {
                throw new IllegalArgumentException("Can only delete stock addition entries");
            }
            
            // Use the main delete method
            deleteInventoryEntry(entryId);
            
            logger.info("Stock entry {} deleted successfully for product {}", entryId, productId);
            
        } catch (RuntimeException e) {
            logger.error("Error deleting stock entry {} for product {}: {}", entryId, productId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting stock entry {} for product {}: ", entryId, productId, e);
            throw new RuntimeException("Failed to delete stock entry: " + e.getMessage(), e);
        }
    }

    /**
     * Soft delete inventory entry (mark as inactive instead of physical delete)
     * Alternative approach if you prefer to keep records
     */
    @Transactional
    public void softDeleteInventoryEntry(Long inventoryId) {
        try {
            logger.info("Soft deleting inventory entry with ID: {}", inventoryId);
            
            // Find the inventory entry
            Inventory inventory = inventoryRepository.findById(inventoryId)
                    .orElseThrow(() -> new RuntimeException("Inventory entry not found with id: " + inventoryId));
            
            // Add an isActive field to your Inventory entity if you want this approach
            // inventory.setIsActive(false);
            // inventoryRepository.save(inventory);
            
            // For now, use hard delete
            deleteInventoryEntry(inventoryId);
            
        } catch (Exception e) {
            logger.error("Error soft deleting inventory entry {}: ", inventoryId, e);
            throw new RuntimeException("Failed to soft delete inventory entry: " + e.getMessage(), e);
        }
    }

    // ===================== ADDITIONAL UTILITY METHODS =====================

    /**
     * Check for low stock products
     */
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockInventories() {
        try {
            logger.debug("Fetching low stock inventories");
            List<Inventory> lowStockInventories = inventoryRepository.findLowStockInventories();
            return lowStockInventories.stream()
                    .map(inventoryMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching low stock inventories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve low stock inventories", e);
        }
    }

    /**
     * Get inventory summary by products
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getInventorySummaryByProducts() {
        try {
            logger.debug("Generating inventory summary by products");
            List<Object[]> summaryData = inventoryRepository.getInventorySummary();
            
            return summaryData.stream().map(row -> {
                Map<String, Object> summary = new HashMap<>();
                summary.put("productId", row[0]);
                summary.put("productName", row[1]);
                summary.put("totalStock", row[2]);
                summary.put("activeBatches", row[3]);
                summary.put("depletedBatches", row[4]);
                return summary;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Error generating inventory summary by products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate inventory summary by products", e);
        }
    }

    /**
     * Create new inventory entry
     */
    public InventoryResponse createInventory(InventoryRequest request) {
        try {
            logger.debug("Creating new inventory entry for product: {}", request.getProductId());
            
            // Validate request
            validateInventoryRequest(request);
            
            Product product = productService.getProductEntityById(request.getProductId());
            
            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantity(request.getQuantity());
            inventory.setUnitPrice(request.getUnitPrice() != null ? BigDecimal.valueOf(request.getUnitPrice()) : BigDecimal.ZERO);
            inventory.setDate(request.getDate() != null ? request.getDate() : LocalDateTime.now());
            inventory.setReference(request.getReference());
            inventory.setMovementType(parseMovementType(request.getMovementType() != null ? request.getMovementType() : "IN"));
            
            if (request.getSupplierId() != null) {
                Supplier supplier = supplierService.getSupplierEntityById(request.getSupplierId());
                inventory.setSupplier(supplier);
            }
            
            Inventory savedInventory = inventoryRepository.save(inventory);
            
            // Update product current stock for IN movements
            if (inventory.getMovementType() == Inventory.MovementType.IN) {
                product.setCurrentStock(product.getCurrentStock() + request.getQuantity());
                productService.saveProduct(product);
            }
            
            logger.info("Inventory created successfully with id: {}", savedInventory.getId());
            return inventoryMapper.toResponse(savedInventory);
            
        } catch (InventoryValidationException e) {
            logger.warn("Validation error creating inventory: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating inventory: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create inventory", e);
        }
    }

    /**
     * Update existing inventory
     */
    public InventoryResponse updateInventory(Long id, InventoryRequest request) {
        try {
            logger.debug("Updating inventory with id: {}", id);
            
            Inventory inventory = inventoryRepository.findById(id)
                    .orElseThrow(() -> new InventoryValidationException("Inventory not found with id: " + id));
            
            validateInventoryRequest(request);
            
            // Calculate stock difference for IN movements only
            int oldQuantity = inventory.getQuantity();
            int newQuantity = request.getQuantity();
            int quantityDifference = newQuantity - oldQuantity;
            
            // Update inventory fields
            inventory.setQuantity(newQuantity);
            if (request.getUnitPrice() != null) {
                inventory.setUnitPrice(BigDecimal.valueOf(request.getUnitPrice()));
            }
            if (request.getDate() != null) {
                inventory.setDate(request.getDate());
            }
            if (request.getReference() != null) {
                inventory.setReference(request.getReference());
            }
            
            Inventory savedInventory = inventoryRepository.save(inventory);
            
            // Update product current stock only for IN movements
            if (inventory.getMovementType() == Inventory.MovementType.IN) {
                Product product = inventory.getProduct();
                product.setCurrentStock(product.getCurrentStock() + quantityDifference);
                productService.saveProduct(product);
            }
            
            logger.info("Inventory updated successfully with id: {}", id);
            return inventoryMapper.toResponse(savedInventory);
            
        } catch (InventoryValidationException e) {
            logger.warn("Validation error updating inventory: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating inventory: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update inventory", e);
        }
    }

    /**
     * Delete inventory
     */
    public void deleteInventory(Long id) {
        try {
            logger.debug("Deleting inventory with id: {}", id);
            
            Inventory inventory = inventoryRepository.findById(id)
                    .orElseThrow(() -> new InventoryValidationException("Inventory not found with id: " + id));
            
            // Update product current stock only for IN movements
            if (inventory.getMovementType() == Inventory.MovementType.IN) {
                Product product = inventory.getProduct();
                product.setCurrentStock(product.getCurrentStock() - inventory.getQuantity());
                productService.saveProduct(product);
            }
            
            inventoryRepository.delete(inventory);
            
            logger.info("Inventory deleted successfully with id: {}", id);
            
        } catch (InventoryValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting inventory: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete inventory", e);
        }
    }

    /**
     * Validate inventory request
     */
    private void validateInventoryRequest(InventoryRequest request) {
        if (request.getProductId() == null) {
            throw new InventoryValidationException("Product ID is required");
        }
        
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new InventoryValidationException("Quantity must be non-negative");
        }
        
        if (request.getUnitPrice() != null && request.getUnitPrice() < 0) {
            throw new InventoryValidationException("Unit price must be non-negative");
        }
    }

    /**
     * Cleanup depleted inventories (utility method)
     */
    @Transactional
    public int cleanupDepletedInventories() {
        try {
            logger.debug("Cleaning up depleted inventories");
            List<Inventory> depletedInventories = inventoryRepository.findDepletedInventories();
            int count = depletedInventories.size();
            
            if (count > 0) {
                inventoryRepository.deleteDepletedInventories();
                logger.info("Cleaned up {} depleted inventory records", count);
            }
            
            return count;
            
        } catch (Exception e) {
            logger.error("Error cleaning up depleted inventories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to cleanup depleted inventories", e);
        }
    }

    /**
     * Get all inventories with detailed information
     */
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventories() {
        try {
            logger.debug("Fetching all inventories");
            List<Inventory> inventories = inventoryRepository.findAll();
            return inventories.stream()
                    .map(inventoryMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching inventories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve inventories", e);
        }
    }

    /**
     * Get inventory by ID
     */
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(Long id) {
        try {
            logger.debug("Fetching inventory with id: {}", id);
            Inventory inventory = inventoryRepository.findById(id)
                    .orElseThrow(() -> new InventoryValidationException("Inventory not found with id: " + id));
            return inventoryMapper.toResponse(inventory);
        } catch (InventoryValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching inventory with id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve inventory", e);
        }
    }

    /**
     * Get inventories by product with detailed filtering
     */
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoriesByProduct(Long productId) {
        try {
            logger.debug("Fetching inventories for product id: {}", productId);
            List<Inventory> inventories = inventoryRepository.findByProductIdOrderByDateAsc(productId);
            return inventories.stream()
                    .map(inventoryMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching inventories for product {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve product inventories", e);
        }
    }

    /**
     * Check if any product has sufficient stock
     */
    @Transactional(readOnly = true)
    public boolean hasAnyAvailableStock(Long productId) {
        try {
            Integer stock = inventoryRepository.getTotalAvailableStockForProduct(productId);
            return stock != null && stock > 0;
        } catch (Exception e) {
            logger.error("Error checking if product {} has available stock: {}", productId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get product stock status summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProductStockStatus(Long productId) {
        try {
            Map<String, Object> status = new HashMap<>();
            
            Product product = productService.getProductEntityById(productId);
            int totalAvailable = getTotalAvailableStock(productId);
            int productStock = product.getCurrentStock();
            
            status.put("productId", productId);
            status.put("productName", product.getName());
            status.put("availableStock", totalAvailable);
            status.put("productStock", productStock);
            status.put("hasStock", totalAvailable > 0);
            status.put("stockDiscrepancy", Math.abs(totalAvailable - productStock));
            
            // Low stock check
            if (product.getLowStockThreshold() != null) {
                status.put("lowStockThreshold", product.getLowStockThreshold());
                status.put("isLowStock", totalAvailable <= product.getLowStockThreshold());
            } else {
                status.put("lowStockThreshold", 0);
                status.put("isLowStock", false);
            }
            
            return status;
            
        } catch (Exception e) {
            logger.error("Error getting stock status for product {}: {}", productId, e.getMessage(), e);
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("error", "Unable to retrieve stock status");
            errorStatus.put("productId", productId);
            return errorStatus;
        }
    }
}