package org.example.inventorymanagementbackend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    
    // Flat product fields
    private Long productId;
    private String productCode;
    private String productName;
    private Integer currentStock; // Add this for product stock info
    
    private Integer quantity;
    private String movementType;
    private BigDecimal unitPrice;
    
    // Flat supplier fields
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    
    private LocalDateTime date;
    private String reference;
    private LocalDateTime createdAt;
    private String movementDescription;
    
    public BigDecimal getTotalValue() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}