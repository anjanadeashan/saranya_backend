package org.example.inventorymanagementbackend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product Entity
 * Represents products in the inventory system
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_code", columnList = "code", unique = true),
        @Index(name = "idx_product_name", columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Product code is required")
    @Size(max = 50, message = "Product code must not exceed 50 characters")
    private String code;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Column(length = 1000)
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Column(name = "fixed_price", nullable = false, precision = 10, scale = 2)
    //@NotNull(message = "Fixed price is required")
    //@DecimalMin(value = "0.0", inclusive = false, message = "Fixed price must be greater than 0")
   // @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private BigDecimal fixedPrice;

    @Column(precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "current_stock", nullable = false)
    @Min(value = 0, message = "Current stock cannot be negative")
    private Integer currentStock = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inventory> inventoryMovements;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SaleItem> saleItems;






    @Column(name = "product_code", unique = true)
private String productCode;

public String getProductCode() {
    return productCode;
}

public void setProductCode(String productCode) {
    this.productCode = productCode;
}




    // Business Logic Methods

    /**
     * Check if product is low on stock
     */
    public boolean isLowStock() {
        return currentStock != null && lowStockThreshold != null &&
                currentStock <= lowStockThreshold;
    }

    /**
     * Calculate discounted price
     */
    public BigDecimal getDiscountedPrice() {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) {
            return fixedPrice;
        }
        BigDecimal discountAmount = fixedPrice.multiply(discount).divide(BigDecimal.valueOf(100));
        return fixedPrice.subtract(discountAmount);
    }

    /**
     * Update stock quantity
     */
    public void updateStock(Integer quantity, boolean isAddition) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (isAddition) {
            this.currentStock = (this.currentStock != null ? this.currentStock : 0) + quantity;
        } else {
            if (this.currentStock == null || this.currentStock < quantity) {
                throw new IllegalArgumentException("Insufficient stock");
            }
            this.currentStock -= quantity;
        }
    }

    /**
     * Check if sufficient stock is available
     */
    public boolean hasSufficientStock(Integer requiredQuantity) {
        return currentStock != null && requiredQuantity != null &&
                currentStock >= requiredQuantity;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (currentStock == null) {
            currentStock = 0;
            
        }if (lowStockThreshold == null) {  // ADD THIS
        lowStockThreshold = 0;
    }
        if (discount == null) {
            discount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(BigDecimal fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Inventory> getInventoryMovements() {
        return inventoryMovements;
    }

    public void setInventoryMovements(List<Inventory> inventoryMovements) {
        this.inventoryMovements = inventoryMovements;
    }

    public List<SaleItem> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(List<SaleItem> saleItems) {
        this.saleItems = saleItems;
    }
}