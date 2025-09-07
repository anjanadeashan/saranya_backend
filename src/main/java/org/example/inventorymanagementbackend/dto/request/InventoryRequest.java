package org.example.inventorymanagementbackend.dto.request;

import  org.example.inventorymanagementbackend.entity.Inventory;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InventoryRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Movement type is required")
    private Inventory.MovementType movementType;

    private String supplierCode; // Required for IN movements

    @Size(max = 200, message = "Reference must not exceed 200 characters")
    private String reference;
}
