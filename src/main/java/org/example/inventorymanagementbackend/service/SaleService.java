package org.example.inventorymanagementbackend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.inventorymanagementbackend.dto.request.SaleItemRequest;
import org.example.inventorymanagementbackend.dto.request.SaleRequest;
import org.example.inventorymanagementbackend.dto.response.SaleResponse;
import org.example.inventorymanagementbackend.entity.Customer;
import org.example.inventorymanagementbackend.entity.Inventory;
import org.example.inventorymanagementbackend.entity.Product;
import org.example.inventorymanagementbackend.entity.Sale;
import org.example.inventorymanagementbackend.entity.Sale.PaymentMethod;
import org.example.inventorymanagementbackend.entity.SaleItem;
import org.example.inventorymanagementbackend.mapper.SaleMapper;
import org.example.inventorymanagementbackend.repository.InventoryRepository;
import org.example.inventorymanagementbackend.repository.SaleItemRepository;
import org.example.inventorymanagementbackend.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SaleService {

    private static final Logger logger = LoggerFactory.getLogger(SaleService.class);
    private static final int MAX_QUANTITY_PER_ITEM = 1000;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private SaleMapper saleMapper;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    // Custom Exceptions
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }

    public static class SaleProcessingException extends RuntimeException {
        public SaleProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Read-only operations
    @Transactional(readOnly = true)
    public List<SaleResponse> getAllSales() {
        try {
            logger.debug("Fetching all sales");
            List<Sale> sales = saleRepository.findAllOrderBySaleDateDesc();
            return sales.stream()
                    .map(saleMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all sales: {}", e.getMessage(), e);
            throw new SaleProcessingException("Failed to retrieve sales", e);
        }
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleById(Long id) {
        try {
            logger.debug("Fetching sale with id: {}", id);
            Sale sale = saleRepository.findById(id)
                    .orElseThrow(() -> new ValidationException("Sale not found with id: " + id));
            return saleMapper.toResponse(sale);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching sale with id {}: {}", id, e.getMessage(), e);
            throw new SaleProcessingException("Failed to retrieve sale", e);
        }
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getUnpaidSales() {
        try {
            logger.debug("Fetching unpaid sales");
            List<Sale> sales = saleRepository.findByIsPaidOrderBySaleDateDesc(false);
            return sales.stream()
                    .map(saleMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching unpaid sales: {}", e.getMessage(), e);
            throw new SaleProcessingException("Failed to retrieve unpaid sales", e);
        }
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getSalesByCustomer(Long customerId) {
        try {
            logger.debug("Fetching sales for customer id: {}", customerId);
            Customer customer = customerService.getCustomerEntityById(customerId);
            List<Sale> sales = saleRepository.findByCustomerOrderBySaleDateDesc(customer);
            return sales.stream()
                    .map(saleMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching sales for customer id {}: {}", customerId, e.getMessage(), e);
            throw new SaleProcessingException("Failed to retrieve customer sales", e);
        }
    }

    /**
     * Check stock availability for all items in a sale request - ENHANCED VERSION
     */
    @Transactional(readOnly = true)
    public boolean checkStockAvailabilityForSale(SaleRequest request) {
        try {
            logger.debug("Checking stock availability for sale with {} items", request.getSaleItems().size());
            
            // Group by product ID to handle multiple items of same product
            Map<Long, Integer> requiredQuantities = request.getSaleItems().stream()
                    .collect(Collectors.groupingBy(
                            SaleItemRequest::getProductId,
                            Collectors.summingInt(SaleItemRequest::getQuantity)
                    ));

            for (Map.Entry<Long, Integer> entry : requiredQuantities.entrySet()) {
                Long productId = entry.getKey();
                Integer totalRequired = entry.getValue();
                
                int available = inventoryService.getTotalAvailableStock(productId);
                logger.debug("Stock check - Product {}: Required={}, Available={}", productId, totalRequired, available);
                
                if (available < totalRequired) {
                    Product product = productService.getProductEntityById(productId);
                    logger.warn("Insufficient stock for product {}: Required={}, Available={}", 
                        product.getName(), totalRequired, available);
                    return false;
                }
            }
            
            logger.debug("Stock availability check passed for all products");
            return true;
        } catch (Exception e) {
            logger.error("Error checking stock availability: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Create sale with FIFO inventory management - ENHANCED WITH STRICT STOCK VALIDATION
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SaleResponse createSaleWithFIFO(SaleRequest request) {
        try {
            logger.debug("Creating sale with FIFO for customer: {}", request.getCustomerId());
            logger.debug("Received sale request with {} items", request.getSaleItems().size());

            // STEP 1: Basic validation
            validateSaleRequest(request);

            // STEP 2: PRE-VALIDATE STOCK BEFORE ANY PROCESSING
            preValidateStockBeforeProcessing(request.getSaleItems());

            Customer customer = customerService.getCustomerEntityById(request.getCustomerId());

            // STEP 3: STRICT stock availability validation (batch operation)
            validateStockAvailability(request.getSaleItems());

            // STEP 4: Calculate total amount
            BigDecimal totalAmount = calculateTotalAmount(request.getSaleItems());

            // STEP 5: Create and save sale entity FIRST
            Sale sale = createSaleEntity(customer, request, totalAmount);
            Sale savedSale = saleRepository.save(sale);

            logger.debug("Sale entity saved with id: {}, now processing items with stock locks", savedSale.getId());

            // STEP 6: Process sale items with additional stock protection
            processSaleItemsWithStockProtection(savedSale, request.getSaleItems());

            // STEP 7: Update customer balance for unpaid sales
            if (!savedSale.getIsPaid()) {
                updateCustomerBalance(customer, totalAmount);
            }

            // STEP 8: Final save to persist all relationships
            savedSale = saleRepository.save(savedSale);
            
            // STEP 9: Verify final stock levels are not negative
            verifyNoNegativeStock(request.getSaleItems());
            
            logger.info("Sale created successfully with id: {}, total: {}", savedSale.getId(), savedSale.getTotalAmount());

            return saleMapper.toResponse(savedSale);

        } catch (ValidationException | InsufficientStockException e) {
            logger.warn("Business logic error creating sale: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating sale: {}", e.getMessage(), e);
            throw new SaleProcessingException("Failed to create sale", e);
        }
    }

    /**
     * PRE-VALIDATE STOCK BEFORE ANY PROCESSING
     */
    private void preValidateStockBeforeProcessing(List<SaleItemRequest> saleItems) {
        logger.debug("Pre-validating stock availability before sale processing");
        
        for (SaleItemRequest item : saleItems) {
            Product product = productService.getProductEntityById(item.getProductId());
            int available = inventoryService.getTotalAvailableStock(item.getProductId());
            
            if (available < item.getQuantity()) {
                throw new InsufficientStockException(
                    String.format("Cannot create sale: Product '%s' has insufficient stock. Required: %d, Available: %d", 
                        product.getName(), item.getQuantity(), available));
            }
            
            // Additional check for product current stock
            if (product.getCurrentStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                    String.format("Cannot create sale: Product '%s' current stock is insufficient. Required: %d, Product Stock: %d", 
                        product.getName(), item.getQuantity(), product.getCurrentStock()));
            }
        }
    }

    /**
     * Consolidated validation for sale request
     */
    private void validateSaleRequest(SaleRequest request) {
        if (request == null) {
            throw new ValidationException("Sale request cannot be null");
        }

        if (request.getPaymentMethod() == null) {
            throw new ValidationException("Payment method is required");
        }

        if (request.getSaleItems() == null || request.getSaleItems().isEmpty()) {
            throw new ValidationException("Sale must have at least one item");
        }

        // Validate each sale item
        for (int i = 0; i < request.getSaleItems().size(); i++) {
            validateSaleItemRequest(request.getSaleItems().get(i), i + 1);
        }
    }

    /**
     * Validate individual sale item request
     */
    private void validateSaleItemRequest(SaleItemRequest itemRequest, int itemNumber) {
        if (itemRequest.getProductId() == null) {
            throw new ValidationException("Product ID cannot be null for sale item " + itemNumber);
        }

        if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
            throw new ValidationException("Quantity must be greater than zero for sale item " + itemNumber);
        }

        if (itemRequest.getQuantity() > MAX_QUANTITY_PER_ITEM) {
            throw new ValidationException("Quantity cannot exceed " + MAX_QUANTITY_PER_ITEM + " units for sale item " + itemNumber);
        }

        if (itemRequest.getUnitPrice() == null || itemRequest.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Unit price must be greater than zero for sale item " + itemNumber);
        }
    }

    /**
     * ENHANCED: Validate stock availability with absolute prevention of negative inventory
     */
    private void validateStockAvailability(List<SaleItemRequest> saleItems) {
        logger.debug("Performing strict stock validation for {} sale items", saleItems.size());
        
        // Group by product ID to handle multiple items of same product
        Map<Long, Integer> requiredQuantities = saleItems.stream()
                .collect(Collectors.groupingBy(
                        SaleItemRequest::getProductId,
                        Collectors.summingInt(SaleItemRequest::getQuantity)
                ));

        List<String> stockErrors = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : requiredQuantities.entrySet()) {
            Long productId = entry.getKey();
            Integer totalRequired = entry.getValue();

            // Get current available stock (sum of all positive inventory batches)
            int currentAvailable = inventoryService.getTotalAvailableStock(productId);
            
            // Get product details for error message
            Product product = productService.getProductEntityById(productId);
            
            logger.debug("Product {}: Required={}, Available={}", product.getName(), totalRequired, currentAvailable);

            if (currentAvailable < totalRequired) {
                String error = String.format("Insufficient stock for product '%s' (Code: %s). Required: %d, Available: %d, Shortfall: %d",
                    product.getName(),
                    product.getCode(),
                    totalRequired,
                    currentAvailable,
                    (totalRequired - currentAvailable));
                stockErrors.add(error);
            }
            
            // Additional check: Verify FIFO batches can fulfill the requirement
            List<Inventory> fifoBatches = inventoryRepository.findAvailableStockForProductFIFO(productId);
            int fifoAvailable = fifoBatches.stream().mapToInt(Inventory::getQuantity).sum();
            
            if (fifoAvailable < totalRequired) {
                String error = String.format("FIFO inventory insufficient for product '%s'. FIFO Available: %d, Required: %d", 
                    product.getName(), fifoAvailable, totalRequired);
                stockErrors.add(error);
            }
        }

        // If any stock errors, throw exception to prevent sale
        if (!stockErrors.isEmpty()) {
            String combinedError = "Stock validation failed:\n" + String.join("\n", stockErrors);
            logger.warn("Sale blocked due to insufficient stock: {}", combinedError);
            throw new InsufficientStockException(combinedError);
        }
        
        logger.debug("Stock validation passed for all {} products", requiredQuantities.size());
    }

    /**
     * Calculate total amount for all sale items
     */
    /**
 * Calculate total amount for all sale items
 */
private BigDecimal calculateTotalAmount(List<SaleItemRequest> saleItems) {
    BigDecimal totalAmount = BigDecimal.ZERO;

    for (SaleItemRequest itemRequest : saleItems) {
        // Ensure discount is always non-null and properly scaled
        BigDecimal discount = itemRequest.getDiscount() != null
                ? itemRequest.getDiscount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Calculate gross amount = price × quantity
        BigDecimal gross = itemRequest.getUnitPrice()
                .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        // Subtract discount and normalize to 2 decimals
        BigDecimal lineTotal = gross.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        // ✅ Allow 0 (100% discount), but never negative
        if (lineTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(
                "Line total cannot be negative for product: " + itemRequest.getProductId());
        }

        totalAmount = totalAmount.add(lineTotal);
    }

    // Normalize total to 2 decimals and ensure it’s not zero or negative
    totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
    if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new ValidationException("Total amount must be greater than zero for the entire sale");
    }

    return totalAmount;
}



    /**
     * Create sale entity with all required fields
     */
    private Sale createSaleEntity(Customer customer, SaleRequest request, BigDecimal totalAmount) {
        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setSaleDate(LocalDateTime.now());
        sale.setCheckNumber(request.getCheckNumber());
        sale.setBankName(request.getBankName());
        sale.setCheckDate(request.getCheckDate());
        sale.setNotes(request.getNotes());
        sale.setTotalAmount(totalAmount);

        // Set payment status based on payment method
        sale.setIsPaid(request.getPaymentMethod().isPaidImmediately());

        // IMPORTANT: Initialize the saleItems collection if not already done in entity
        if (sale.getSaleItems() == null) {
            sale.setSaleItems(new ArrayList<>());
        }

        return sale;
    }

    /**
     * Process sale items with additional stock protection
     */
    private void processSaleItemsWithStockProtection(Sale sale, List<SaleItemRequest> itemRequests) {
        logger.debug("Processing {} sale items with stock protection for sale id: {}", itemRequests.size(), sale.getId());

        for (int i = 0; i < itemRequests.size(); i++) {
            SaleItemRequest itemRequest = itemRequests.get(i);
            try {
                logger.debug("Processing item {}/{}: Product ID {}, Quantity {}", 
                    i + 1, itemRequests.size(), itemRequest.getProductId(), itemRequest.getQuantity());
                
                // Additional stock check right before processing
                int currentAvailable = inventoryService.getTotalAvailableStock(itemRequest.getProductId());
                if (currentAvailable < itemRequest.getQuantity()) {
                    Product product = productService.getProductEntityById(itemRequest.getProductId());
                    throw new InsufficientStockException(
                        String.format("Stock depleted during processing for product '%s'. Available: %d, Required: %d", 
                            product.getName(), currentAvailable, itemRequest.getQuantity()));
                }
                
                processSingleSaleItemWithFIFO(sale, itemRequest);
                
            } catch (Exception e) {
                logger.error("Failed to process sale item {}/{} for product {}: {}", 
                    i + 1, itemRequests.size(), itemRequest.getProductId(), e.getMessage());
                throw new SaleProcessingException("Failed to process sale item for product " + itemRequest.getProductId(), e);
            }
        }

        logger.debug("Completed processing all sale items for sale id: {}", sale.getId());
    }

    /**
     * FIXED: Process single sale item with FIFO, updating quantities instead of deleting
     */
    private void processSingleSaleItemWithFIFO(Sale sale, SaleItemRequest itemRequest) {
        logger.debug("Processing FIFO for product: {}, quantity: {}", itemRequest.getProductId(), itemRequest.getQuantity());

        Product product = productService.getProductEntityById(itemRequest.getProductId());

        // Get available inventory batches with locking to prevent race conditions
        List<Inventory> availableBatches = inventoryRepository.findAvailableStockForProductFIFO(itemRequest.getProductId());

        if (availableBatches.isEmpty()) {
            throw new InsufficientStockException("No available inventory batches for product: " + product.getName());
        }

        int remainingQuantity = itemRequest.getQuantity();
        int totalStockUsed = 0;
        List<Inventory> batchesToUpdate = new ArrayList<>();

        for (Inventory batch : availableBatches) {
            if (remainingQuantity <= 0) break;

            int quantityFromBatch = Math.min(remainingQuantity, batch.getQuantity());
            if (quantityFromBatch <= 0) continue;

            // Create sale item and add to existing collection properly
            SaleItem saleItem = createSaleItemForBatch(sale, product, itemRequest, quantityFromBatch, batch);
            
            // Add to existing collection, don't replace it
            sale.getSaleItems().add(saleItem);
            
            // FIXED: Always update quantity, never delete
            int newQuantity = batch.getQuantity() - quantityFromBatch;
            batch.setQuantity(newQuantity); // Set to 0 if depleted, but don't delete
            batchesToUpdate.add(batch);

            totalStockUsed += quantityFromBatch;
            remainingQuantity -= quantityFromBatch;

            logger.debug("Used {} units from batch {}, new quantity: {}, remaining to fulfill: {}", 
                quantityFromBatch, batch.getId(), newQuantity, remainingQuantity);
        }

        if (remainingQuantity > 0) {
            throw new InsufficientStockException("Could not fulfill complete quantity for product: " + product.getName() + 
                ". Still need: " + remainingQuantity);
        }

        // Batch update all inventory records (including those with 0 quantity)
        if (!batchesToUpdate.isEmpty()) {
            inventoryRepository.saveAll(batchesToUpdate);
            logger.debug("Updated {} inventory batches", batchesToUpdate.size());
        }

        // Update product stock
        product.setCurrentStock(product.getCurrentStock() - totalStockUsed);
        productService.saveProduct(product);

        logger.debug("FIFO processing completed for product: {}, total used: {}", product.getName(), totalStockUsed);
    }

    /**
     * Create sale item with proper bidirectional relationship
     */
    private SaleItem createSaleItemForBatch(Sale sale, Product product, SaleItemRequest request, 
                                           int quantity, Inventory batch) {
        SaleItem saleItem = new SaleItem();
        
        // Set bidirectional relationship properly
        saleItem.setSale(sale);
        saleItem.setProduct(product);
        saleItem.setQuantity(quantity);
        saleItem.setUnitPrice(request.getUnitPrice());
        saleItem.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
        saleItem.setInventoryId(batch.getId());
        
        // Set additional inventory tracking fields
        if (batch.getUnitPrice() != null) {
            saleItem.setInventoryUnitPrice(batch.getUnitPrice());
        }
        if (batch.getDate() != null) {
            saleItem.setInventoryDate(batch.getDate());
        }
        
        // Calculate line total
        saleItem.updateLineTotal();

        logger.debug("Created sale item: Product {}, Quantity {}, Unit Price {}, Line Total {}", 
            product.getName(), quantity, request.getUnitPrice(), saleItem.getLineTotal());

        return saleItem;
    }

    /**
 * SAFE VERSION: Verify no negative stock after sale completion
 */
private void verifyNoNegativeStock(List<SaleItemRequest> saleItems) {
    for (SaleItemRequest item : saleItems) {
        try {
            Product product = productService.getProductEntityById(item.getProductId());
            if (product.getCurrentStock() < 0) {
                logger.error("CRITICAL: Negative stock detected for product {}: {}", product.getName(), product.getCurrentStock());
                throw new RuntimeException("Critical error: Negative stock detected for product: " + product.getName());
            }
            
            int inventoryStock = inventoryService.getTotalAvailableStock(item.getProductId());
            if (inventoryStock < 0) {
                logger.error("CRITICAL: Negative inventory stock for product {}: {}", product.getName(), inventoryStock);
                throw new RuntimeException("Critical error: Negative inventory stock for product: " + product.getName());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying stock for product {}: {}", item.getProductId(), e.getMessage());
        }
    }
}

    /**
     * Update customer outstanding balance
     */
    private void updateCustomerBalance(Customer customer, BigDecimal amount) {
        customer.addToOutstandingBalance(amount);
        customerService.updateCustomer(customer);
    }

    /**
     * Legacy method - redirects to FIFO
     */
    public SaleResponse createSale(SaleRequest request) {
        return createSaleWithFIFO(request);
    }

    // ... (keeping all other existing methods unchanged - markSaleAsPaid, deleteSale, etc.)
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SaleResponse markSaleAsPaid(Long saleId) {
        try {
            logger.debug("Marking sale as paid: {}", saleId);

            Sale sale = saleRepository.findById(saleId)
                    .orElseThrow(() -> new ValidationException("Sale not found with id: " + saleId));

            if (sale.getIsPaid()) {
                throw new ValidationException("Sale is already marked as paid");
            }

            sale.markAsPaid();

            // Process inventory for check payments (inventory is updated when check clears)
            if (sale.getPaymentMethod() == Sale.PaymentMethod.CREDIT_CHECK) {
                processInventoryForCheckPayment(sale);
            }

            // Update customer outstanding balance
            Customer customer = sale.getCustomer();
            customer.reduceOutstandingBalance(sale.getTotalAmount());
            customerService.updateCustomer(customer);

            Sale savedSale = saleRepository.save(sale);
            logger.info("Sale marked as paid successfully with id: {}", saleId);

            return saleMapper.toResponse(savedSale);

        } catch (ValidationException e) {
            logger.warn("Validation error marking sale as paid: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error marking sale as paid: {}", e.getMessage(), e);
            throw new SaleProcessingException("Failed to mark sale as paid", e);
        }
    }

    private void processInventoryForCheckPayment(Sale sale) {
        try {
            logger.debug("Processing inventory for check payment: {}", sale.getId());

            List<SaleItem> saleItems = saleItemRepository.findBySaleId(sale.getId());

            for (SaleItem saleItem : saleItems) {
                if (saleItem.getInventoryId() == null) {
                    processPendingSaleItem(saleItem);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing inventory for check payment: {}", e.getMessage(), e);
            throw new SaleProcessingException("Failed to process inventory for check payment", e);
        }
    }

    private void processPendingSaleItem(SaleItem saleItem) {
        try {
            Product product = saleItem.getProduct();
            int requiredQuantity = saleItem.getQuantity();

            List<Inventory> availableBatches = inventoryRepository.findAvailableStockForProductFIFO(product.getId());

            int remainingQuantity = requiredQuantity;
            int totalStockReduction = 0;
            List<Inventory> batchesToUpdate = new ArrayList<>();

            for (Inventory batch : availableBatches) {
                if (remainingQuantity <= 0) break;

                int quantityFromBatch = Math.min(remainingQuantity, batch.getQuantity());
                if (quantityFromBatch <= 0) continue;

                batch.setQuantity(batch.getQuantity() - quantityFromBatch);
                batchesToUpdate.add(batch);

                totalStockReduction += quantityFromBatch;

                if (saleItem.getInventoryId() == null) {
                    saleItem.setInventoryId(batch.getId());
                }

                remainingQuantity -= quantityFromBatch;
            }

            // Batch operations
            inventoryRepository.saveAll(batchesToUpdate);
            if (saleItem.getInventoryId() != null) {
                saleItemRepository.save(saleItem);
            }

            // Update product stock
            product.setCurrentStock(product.getCurrentStock() - totalStockReduction);
            productService.saveProduct(product);

        } catch (Exception e) {
            logger.error("Error processing pending sale item: {}", e.getMessage(), e);
            throw new SaleProcessingException("Failed to process pending sale item", e);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteSale(Long id) {
        try {
            logger.debug("Deleting sale with id: {}", id);

            Sale sale = saleRepository.findById(id)
                    .orElseThrow(() -> new ValidationException("Sale not found with id: " + id));

            // Reverse inventory if sale was processed
            if (sale.getIsPaid() || sale.getPaymentMethod() != Sale.PaymentMethod.CREDIT_CHECK) {
                reverseInventoryForSale(sale);
            }

            // Update customer balance if sale was unpaid
            if (!sale.getIsPaid()) {
                Customer customer = sale.getCustomer();
                customer.reduceOutstandingBalance(sale.getTotalAmount());
                customerService.updateCustomer(customer);
            }

            saleRepository.delete(sale);
            logger.info("Sale deleted successfully with id: {}", id);

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting sale: {}", e.getMessage(), e);
            throw new SaleProcessingException("Failed to delete sale", e);
        }
    }

    /**
     * FIXED: Reverse inventory changes when deleting a sale
     */
    private void reverseInventoryForSale(Sale sale) {
        try {
            logger.debug("Reversing inventory for sale: {}", sale.getId());

            List<SaleItem> saleItems = saleItemRepository.findBySaleId(sale.getId());
            List<Inventory> batchesToUpdate = new ArrayList<>();
            Map<Long, Integer> productStockToReverse = new HashMap<>();

            for (SaleItem saleItem : saleItems) {
                // Reverse inventory batch quantity
                if (saleItem.getInventoryId() != null) {
                    Inventory batch = inventoryRepository.findById(saleItem.getInventoryId()).orElse(null);
                    if (batch != null) {
                        int newQuantity = batch.getQuantity() + saleItem.getQuantity();
                        batch.setQuantity(newQuantity);
                        batchesToUpdate.add(batch);
                        logger.debug("Reversing batch {}: adding {} units (new total: {})", 
                            batch.getId(), saleItem.getQuantity(), newQuantity);
                    }
                }

                // Accumulate product stock to reverse
                Long productId = saleItem.getProduct().getId();
                productStockToReverse.merge(productId, saleItem.getQuantity(), Integer::sum);
            }

            // Batch update inventory
            if (!batchesToUpdate.isEmpty()) {
                inventoryRepository.saveAll(batchesToUpdate);
                logger.debug("Updated {} inventory batches during reversal", batchesToUpdate.size());
            }

            // Reverse product stock
            for (Map.Entry<Long, Integer> entry : productStockToReverse.entrySet()) {
                Product product = productService.getProductEntityById(entry.getKey());
                int stockToAdd = entry.getValue();
                product.setCurrentStock(product.getCurrentStock() + stockToAdd);
                productService.saveProduct(product);
                logger.debug("Reversed {} units for product: {}", stockToAdd, product.getName());
            }

            logger.debug("Inventory reversal completed for sale: {}", sale.getId());

        } catch (Exception e) {
            logger.error("Error reversing inventory for sale: {}", e.getMessage(), e);
            throw new SaleProcessingException("Failed to reverse inventory", e);
        }
    }



    /**
 * Mark a check payment as bounced/returned
 */
public Sale markCheckAsBounced(Long saleId, String bouncedNotes) {
    Sale sale = saleRepository.findById(saleId)
        .orElseThrow(() -> new RuntimeException("Sale not found with id: " + saleId));
    
    // Validation
    if (sale.getPaymentMethod() != PaymentMethod.CREDIT_CHECK) {
        throw new IllegalStateException("This sale is not a check payment");
    }
    
    if (sale.getIsPaid()) {
        throw new IllegalStateException("Cannot mark a paid sale as bounced");
    }
    
    if (sale.isCheckBounced()) {
        throw new IllegalStateException("This check is already marked as bounced");
    }
    
    // Mark as bounced
    sale.setCheckBounced(true);
    sale.setCheckBouncedDate(LocalDateTime.now());
    
    if (bouncedNotes != null && !bouncedNotes.trim().isEmpty()) {
        sale.setCheckBouncedNotes(bouncedNotes.trim());
    }
    
    Sale updatedSale = saleRepository.save(sale);
    
    // Optional: Log the bounced check event
    logger.info("Check bounced - Sale ID: {}, Check Number: {}, Amount: {}", 
        saleId, sale.getCheckNumber(), sale.getTotalAmount());
    
    return updatedSale;
}

/**
 * Clear the bounced status from a check payment
 */
public Sale clearBouncedCheckStatus(Long saleId) {
    Sale sale = saleRepository.findById(saleId)
        .orElseThrow(() -> new RuntimeException("Sale not found with id: " + saleId));
    
    // Validation
    if (sale.getPaymentMethod() != PaymentMethod.CREDIT_CHECK) {
        throw new IllegalStateException("This sale is not a check payment");
    }
    
    if (!sale.isCheckBounced()) {
        throw new IllegalStateException("This check is not marked as bounced");
    }
    
    // Clear bounced status
    sale.setCheckBounced(false);
    sale.setCheckBouncedDate(null);
    sale.setCheckBouncedNotes(null);
    
    Sale updatedSale = saleRepository.save(sale);
    
    // Optional: Log the status change
    logger.info("Bounced check status cleared - Sale ID: {}, Check Number: {}", 
        saleId, sale.getCheckNumber());
    
    return updatedSale;
}

/**
 * Get summary of all bounced checks
 */
public Map<String, Object> getBouncedChecksSummary() {
    List<Sale> bouncedChecks = saleRepository.findByPaymentMethodAndCheckBouncedTrue(
        PaymentMethod.CREDIT_CHECK
    );
    
    // Calculate total bounced amount - handle BigDecimal or Double
    double totalBouncedAmount = bouncedChecks.stream()
        .mapToDouble(sale -> {
            if (sale.getTotalAmount() == null) return 0.0;
            // If getTotalAmount() returns BigDecimal
            if (sale.getTotalAmount() instanceof BigDecimal) {
                return ((BigDecimal) sale.getTotalAmount()).doubleValue();
            }
            // If getTotalAmount() returns Double
            return sale.getTotalAmount().doubleValue();
        })
        .sum();
    
    // Build summary map
    Map<String, Object> summary = new HashMap<>();
    summary.put("totalBouncedChecks", bouncedChecks.size());
    summary.put("totalBouncedAmount", totalBouncedAmount);
    summary.put("bouncedChecks", bouncedChecks);
    
    return summary;
}

/**
 * Get all bounced checks (optional - for detailed reports)
 */
public List<Sale> getAllBouncedChecks() {
    return saleRepository.findByPaymentMethodAndCheckBouncedTrue(
        PaymentMethod.CREDIT_CHECK
    );
}

/**
 * Get bounced checks by customer (optional - for customer reports)
 */
public List<Sale> getBouncedChecksByCustomer(Long customerId) {
    return saleRepository.findByPaymentMethodAndCheckBouncedTrue(
        PaymentMethod.CREDIT_CHECK
    ).stream()
    .filter(sale -> sale.getCustomer() != null && 
                    sale.getCustomer().getId().equals(customerId))
    .collect(Collectors.toList());
}

/**
 * Check if a customer has any bounced checks (optional - for credit checks)
 */
public boolean customerHasBouncedChecks(Long customerId) {
    List<Sale> bouncedChecks = getBouncedChecksByCustomer(customerId);
    return !bouncedChecks.isEmpty();
}

/**
 * Get bounced checks count for a customer (optional)
 */
public long getBouncedChecksCountByCustomer(Long customerId) {
    return getBouncedChecksByCustomer(customerId).size();
}

/**
 * Get total bounced amount for a customer (optional)
 */
public double getTotalBouncedAmountByCustomer(Long customerId) {
    return getBouncedChecksByCustomer(customerId).stream()
        .mapToDouble(sale -> {
            if (sale.getTotalAmount() == null) return 0.0;
            // If getTotalAmount() returns BigDecimal
            if (sale.getTotalAmount() instanceof BigDecimal) {
                return ((BigDecimal) sale.getTotalAmount()).doubleValue();
            }
            // If getTotalAmount() returns Double
            return sale.getTotalAmount().doubleValue();
        })
        .sum();
}









}