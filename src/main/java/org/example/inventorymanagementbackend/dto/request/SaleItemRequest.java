package org.example.inventorymanagementbackend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SaleItemRequest {

    @NotNull(message = "Product ID is required")
    @Min(value = 1, message = "Product ID must be positive")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000, message = "Quantity cannot exceed 1000")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid unit price format")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid discount format")
    private BigDecimal discount;

    // Optional field for FIFO tracking (usually set by system, not user)
    private Long inventoryId;

    // System fields for tracking inventory details
    private BigDecimal inventoryUnitPrice;
    private java.time.LocalDateTime inventoryDate;

    // Constructors
    public SaleItemRequest() {
        this.discount = BigDecimal.ZERO; // Initialize discount to prevent null issues
    }

    public SaleItemRequest(Long productId, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = BigDecimal.ZERO;
    }

    public SaleItemRequest(Long productId, Integer quantity, BigDecimal unitPrice, BigDecimal discount) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount != null ? discount : BigDecimal.ZERO;
    }

    // Getters and Setters with enhanced validation logging
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        if (productId != null && productId <= 0) {
            System.err.println("WARNING: Setting invalid product ID: " + productId);
            Thread.dumpStack();
        }
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        // Add debug logging to catch where quantity becomes invalid
        if (quantity != null && quantity <= 0) {
            System.err.println("WARNING: Setting invalid quantity: " + quantity);
            Thread.dumpStack(); // This will show where the invalid quantity is being set
        }
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("WARNING: Setting invalid unit price: " + unitPrice);
            Thread.dumpStack();
        }
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscount() {
        return discount != null ? discount : BigDecimal.ZERO;
    }

    public void setDiscount(BigDecimal discount) {
        if (discount != null && discount.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("WARNING: Setting negative discount: " + discount);
            Thread.dumpStack();
        }
        this.discount = discount != null ? discount : BigDecimal.ZERO;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public BigDecimal getInventoryUnitPrice() {
        return inventoryUnitPrice;
    }

    public void setInventoryUnitPrice(BigDecimal inventoryUnitPrice) {
        this.inventoryUnitPrice = inventoryUnitPrice;
    }

    public java.time.LocalDateTime getInventoryDate() {
        return inventoryDate;
    }

    public void setInventoryDate(java.time.LocalDateTime inventoryDate) {
        this.inventoryDate = inventoryDate;
    }

    // Enhanced helper methods for validation and calculations
    public BigDecimal calculateLineTotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal effectiveDiscount = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(effectiveDiscount);
        return lineTotal.max(BigDecimal.ZERO); // Ensure non-negative
    }

    public boolean isValid() {
        boolean valid = productId != null && productId > 0 &&
               quantity != null && quantity > 0 &&
               unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0 &&
               (discount == null || discount.compareTo(BigDecimal.ZERO) >= 0);
        
        if (!valid) {
            System.err.println("SaleItemRequest validation failed:");
            System.err.println("  productId: " + productId + " (valid: " + (productId != null && productId > 0) + ")");
            System.err.println("  quantity: " + quantity + " (valid: " + (quantity != null && quantity > 0) + ")");
            System.err.println("  unitPrice: " + unitPrice + " (valid: " + (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0) + ")");
            System.err.println("  discount: " + discount + " (valid: " + (discount == null || discount.compareTo(BigDecimal.ZERO) >= 0) + ")");
        }
        
        return valid;
    }

    public boolean hasValidQuantityAndPrice() {
        return quantity != null && quantity > 0 && 
               unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean canCalculateTotal() {
        return hasValidQuantityAndPrice() && 
               (discount == null || discount.compareTo(BigDecimal.ZERO) >= 0);
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (productId == null || productId <= 0) {
            errors.append("Invalid product ID; ");
        }
        if (quantity == null || quantity <= 0) {
            errors.append("Invalid quantity; ");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            errors.append("Invalid unit price; ");
        }
        if (discount != null && discount.compareTo(BigDecimal.ZERO) < 0) {
            errors.append("Invalid discount; ");
        }
        
        return errors.length() > 0 ? errors.toString().trim() : "No validation errors";
    }

    @Override
    public String toString() {
        return "SaleItemRequest{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", discount=" + discount +
                ", inventoryId=" + inventoryId +
                ", lineTotal=" + calculateLineTotal() +
                ", valid=" + isValid() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SaleItemRequest that = (SaleItemRequest) o;
        
        if (!productId.equals(that.productId)) return false;
        if (!quantity.equals(that.quantity)) return false;
        if (!unitPrice.equals(that.unitPrice)) return false;
        return discount != null ? discount.equals(that.discount) : that.discount == null;
    }

    @Override
    public int hashCode() {
        int result = productId != null ? productId.hashCode() : 0;
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (unitPrice != null ? unitPrice.hashCode() : 0);
        result = 31 * result + (discount != null ? discount.hashCode() : 0);
        return result;
    }

    // Factory methods for easy creation
    public static SaleItemRequest create(Long productId, Integer quantity, BigDecimal unitPrice) {
        return new SaleItemRequest(productId, quantity, unitPrice);
    }

    public static SaleItemRequest createWithDiscount(Long productId, Integer quantity, BigDecimal unitPrice, BigDecimal discount) {
        return new SaleItemRequest(productId, quantity, unitPrice, discount);
    }

    // Builder pattern for complex creation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long productId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discount = BigDecimal.ZERO;
        private Long inventoryId;

        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder unitPrice(double unitPrice) {
            this.unitPrice = BigDecimal.valueOf(unitPrice);
            return this;
        }

        public Builder discount(BigDecimal discount) {
            this.discount = discount;
            return this;
        }

        public Builder discount(double discount) {
            this.discount = BigDecimal.valueOf(discount);
            return this;
        }

        public Builder inventoryId(Long inventoryId) {
            this.inventoryId = inventoryId;
            return this;
        }

        public SaleItemRequest build() {
            SaleItemRequest item = new SaleItemRequest(productId, quantity, unitPrice, discount);
            item.setInventoryId(inventoryId);
            return item;
        }
    }
}