package org.example.inventorymanagementbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Inventory Entity
 * Represents stock movements in the inventory system
 */
@Entity
@Table(name = "inventory", indexes = {
        @Index(name = "idx_inventory_product", columnList = "product_id"),
        @Index(name = "idx_inventory_date", columnList = "date"),
        @Index(name = "idx_inventory_movement_type", columnList = "movementType")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Product relationship - MUST EXIST
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 10)
    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    // Supplier relationship - OPTIONAL (only for IN movements)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    @NotNull(message = "Date is required")
    private LocalDateTime date;

    @Column(length = 200)
    @Size(max = 200, message = "Reference must not exceed 200 characters")
    private String reference;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Movement Type Enum
    public enum MovementType {
        IN("Stock In"),
        OUT("Stock Out");

        private final String displayName;

        MovementType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Business Logic Methods

    /**
     * Check if this is a stock IN movement
     */
    public boolean isStockIn() {
        return MovementType.IN.equals(movementType);
    }

    /**
     * Check if this is a stock OUT movement
     */
    public boolean isStockOut() {
        return MovementType.OUT.equals(movementType);
    }

    /**
     * Get movement description
     */
    public String getMovementDescription() {
        StringBuilder description = new StringBuilder();
        description.append(movementType.getDisplayName())
                .append(" - ")
                .append(quantity)
                .append(" units");

        if (product != null) {
            description.append(" of ").append(product.getName());
        }

        if (isStockIn() && supplier != null) {
            description.append(" from ").append(supplier.getName());
        }

        if (reference != null && !reference.trim().isEmpty()) {
            description.append(" (Ref: ").append(reference).append(")");
        }

        return description.toString();
    }

    /**
     * Validate movement constraints
     */
    public void validateMovement() {
        if (movementType == MovementType.IN && supplier == null) {
            throw new IllegalStateException("Supplier is required for stock IN movements");
        }

        if (movementType == MovementType.OUT && supplier != null) {
            throw new IllegalStateException("Supplier should not be specified for stock OUT movements");
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalStateException("Quantity must be positive");
        }

        if (product == null) {
            throw new IllegalStateException("Product is required");
        }
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (date == null) {
            date = LocalDateTime.now();
        }

        // Validate business rules
        validateMovement();
    }

    @PreUpdate
    protected void onUpdate() {
        // Validate business rules on update
        validateMovement();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}