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
        List<Inventory> movements = inventoryRepository.findAllOrderByDateDesc();
        return movements.stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByProduct(Long productId) {
        Product product = productService.getProductEntityById(productId);
        List<Inventory> movements = inventoryRepository.findByProductOrderByDateDesc(product);
        return movements.stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public InventoryResponse recordInventoryMovement(InventoryRequest request) {
        logger.debug("Recording inventory movement for product: {}", request.getProductId());

        Product product = productService.getProductEntityById(request.getProductId());

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(request.getQuantity());
        inventory.setMovementType(request.getMovementType());
        inventory.setReference(request.getReference());

        // Handle supplier for IN movements
        if (request.getMovementType() == Inventory.MovementType.IN) {
            if (request.getSupplierCode() == null || request.getSupplierCode().trim().isEmpty()) {
                throw new IllegalArgumentException("Supplier code is required for stock IN movements");
            }
            Supplier supplier = supplierService.getSupplierByCode(request.getSupplierCode());
            inventory.setSupplier(supplier);
        }

        // Validate stock for OUT movements
        if (request.getMovementType() == Inventory.MovementType.OUT) {
            if (!productService.hasProductSufficientStock(request.getProductId(), request.getQuantity())) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }
        }

        // Save inventory movement
        Inventory savedInventory = inventoryRepository.save(inventory);

        // Update product stock
        boolean isAddition = request.getMovementType() == Inventory.MovementType.IN;
        productService.updateProductStock(request.getProductId(), request.getQuantity(), isAddition);

        logger.info("Inventory movement recorded successfully with id: {}", savedInventory.getId());
        return inventoryMapper.toResponse(savedInventory);
    }
}
