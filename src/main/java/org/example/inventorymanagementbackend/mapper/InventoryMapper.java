package org.example.inventorymanagementbackend.mapper;

import org.example.inventorymanagementbackend.dto.response.InventoryResponse;
import org.example.inventorymanagementbackend.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * Inventory Mapper
 * Maps between Inventory entity and InventoryResponse DTO
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productCode", source = "product.code")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierCode", source = "supplier.uniqueSupplierCode")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "movementDescription", source = ".", qualifiedByName = "getMovementDescription")
    @Mapping(target = "paymentMethod", source = ".", qualifiedByName = "mapPaymentMethod")
    @Mapping(target = "paymentStatus", source = ".", qualifiedByName = "mapPaymentStatus")
    @Mapping(target = "totalCost", source = ".", qualifiedByName = "calculateTotalCost")
    @Mapping(target = "remainingAmount", source = ".", qualifiedByName = "calculateRemainingAmount")
    InventoryResponse toResponse(Inventory inventory);

    /**
     * Custom method to generate movement description
     */
    @Named("getMovementDescription")
    default String getMovementDescription(Inventory inventory) {
        if (inventory == null) {
            return "";
        }

        StringBuilder description = new StringBuilder();

        // Add movement type
        if (inventory.getMovementType() != null) {
            description.append(inventory.getMovementType().name())
                    .append(" - ");
        }

        // Add quantity
        if (inventory.getQuantity() != null) {
            description.append(inventory.getQuantity())
                    .append(" units");
        }

        // Add product name
        if (inventory.getProduct() != null && inventory.getProduct().getName() != null) {
            description.append(" of ").append(inventory.getProduct().getName());
        }

        // Add supplier for IN movements
        if (inventory.getMovementType() != null &&
                "IN".equals(inventory.getMovementType().name()) &&
                inventory.getSupplier() != null &&
                inventory.getSupplier().getName() != null) {
            description.append(" from ").append(inventory.getSupplier().getName());
        }

        // Add reference if available
        if (inventory.getReference() != null && !inventory.getReference().trim().isEmpty()) {
            description.append(" (Ref: ").append(inventory.getReference()).append(")");
        }

        return description.toString();
    }

    /**
     * Map PaymentMethod enum to String
     */
    @Named("mapPaymentMethod")
    default String mapPaymentMethod(Inventory inventory) {
        if (inventory == null || inventory.getPaymentMethod() == null) {
            return null;
        }
        return inventory.getPaymentMethod().name();
    }

    /**
     * Map PaymentStatus enum to String
     */
    @Named("mapPaymentStatus")
    default String mapPaymentStatus(Inventory inventory) {
        if (inventory == null || inventory.getPaymentStatus() == null) {
            return null;
        }
        return inventory.getPaymentStatus().name();
    }

    /**
     * Calculate total cost (quantity * purchasePrice)
     */
    @Named("calculateTotalCost")
    default java.math.BigDecimal calculateTotalCost(Inventory inventory) {
        if (inventory == null || inventory.getPurchasePrice() == null || inventory.getQuantity() == null) {
            return null;
        }
        return inventory.getPurchasePrice().multiply(java.math.BigDecimal.valueOf(inventory.getQuantity()));
    }

    /**
     * Calculate remaining amount (totalCost - paidAmount)
     */
    @Named("calculateRemainingAmount")
    default java.math.BigDecimal calculateRemainingAmount(Inventory inventory) {
        if (inventory == null || inventory.getPurchasePrice() == null || inventory.getQuantity() == null) {
            return null;
        }
        java.math.BigDecimal totalCost = inventory.getPurchasePrice()
            .multiply(java.math.BigDecimal.valueOf(inventory.getQuantity()));

        java.math.BigDecimal paidAmount = inventory.getPaidAmount();
        if (paidAmount == null) {
            paidAmount = java.math.BigDecimal.ZERO;
        }

        return totalCost.subtract(paidAmount);
    }
}
