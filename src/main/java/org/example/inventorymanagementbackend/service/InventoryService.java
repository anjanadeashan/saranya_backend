package org.example.inventorymanagementbackend.service;

import org.example.inventorymanagementbackend.dto.request.InventoryRequest;
import org.example.inventorymanagementbackend.dto.response.InventoryResponse;
import org.example.inventorymanagementbackend.entity.Inventory;
import org.example.inventorymanagementbackend.entity.Product;
import org.example.inventorymanagementbackend.entity.Supplier;
import org.example.inventorymanagementbackend.mapper.InventoryMapper;
import org.example.inventorymanagementbackend.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    public InventoryResponse recordInventoryMovement(InventoryRequest request) {
        logger.info("Recording inventory movement for product: {}", request.getProductId());
        logger.debug("Request details - MovementType: {}, Quantity: {}, SupplierId: {}",
                request.getMovementType(), request.getQuantity(), request.getSupplierId());

        try {
            // Validate and get product
            Product product = productService.getProductEntityById(request.getProductId());
            logger.debug("Found product: {} (ID: {})", product.getName(), product.getId());

            // Create inventory movement
            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantity(request.getQuantity());
            inventory.setReference(request.getReference());
            inventory.setDate(request.getDate());

            // Parse and set movement type
            Inventory.MovementType movementType = parseMovementType(request.getMovementType());
            inventory.setMovementType(movementType);

            // Handle supplier for IN movements
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

            // Validate stock for OUT movements
            if (movementType == Inventory.MovementType.OUT) {
                logger.debug("Validating stock for OUT movement");
                if (!productService.hasProductSufficientStock(request.getProductId(), request.getQuantity())) {
                    logger.warn("Insufficient stock for product: {} (requested: {})",
                            product.getName(), request.getQuantity());
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
                }
            }

            // Save inventory movement
            logger.debug("Saving inventory movement");
            Inventory savedInventory = inventoryRepository.save(inventory);
            logger.debug("Inventory movement saved with ID: {}", savedInventory.getId());

            // Update product stock
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
}