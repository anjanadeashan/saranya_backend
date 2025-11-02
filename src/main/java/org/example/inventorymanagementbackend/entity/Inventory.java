package org.example.inventorymanagementbackend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.inventorymanagementbackend.enums.PaymentMethod;
import org.example.inventorymanagementbackend.enums.PaymentStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inventory Entity
 * Represents stock movements and batches in the FIFO inventory system
 */
@Entity
@Table(name = "inventory", indexes = {
        @Index(name = "idx_inventory_product", columnList = "product_id"),
        @Index(name = "idx_inventory_date", columnList = "date"),
        @Index(name = "idx_inventory_movement_type", columnList = "movementType"),
        @Index(name = "idx_inventory_product_date", columnList = "product_id, date") // For FIFO queries
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

    // FIXED: Allow 0 quantity for depleted batches in FIFO
    @Column(nullable = false)
    @Min(value = 0, message = "Quantity cannot be negative")
    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 10)
    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    // Unit price for this inventory batch
    @Column(name = "unit_price", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true, message = "Unit price must be non-negative")
    private BigDecimal unitPrice;

    // Supplier relationship - OPTIONAL (only for IN movements typically)
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

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "minimum_stock")
    private Integer minimumStock;

    // Payment Tracking Fields
    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "check_number", length = 100)
    private String checkNumber;

    @Column(name = "check_date")
    private LocalDate checkDate;

    @Column(name = "notes", length = 1000)
    private String notes;

    // Movement Type Enum
    public enum MovementType {
        IN("Stock In"),
        OUT("Stock Out"),
        ADJUSTMENT("Stock Adjustment");

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
     * Calculate total value for this inventory batch
     */
    public BigDecimal getTotalValue() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

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
     * Check if this inventory batch is depleted (quantity = 0)
     */
    public boolean isDepleted() {
        return quantity != null && quantity == 0;
    }

    /**
     * Check if this inventory batch has available stock
     */
    public boolean hasAvailableStock() {
        return quantity != null && quantity > 0;
    }

    /**
     * Reduce quantity by the specified amount (for FIFO operations)
     */
    public void reduceQuantity(int amount) {
        if (quantity == null) {
            throw new IllegalStateException("Cannot reduce quantity: current quantity is null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot reduce by negative amount: " + amount);
        }
        if (amount > quantity) {
            throw new IllegalArgumentException("Cannot reduce quantity by " + amount + 
                ": only " + quantity + " available");
        }
        this.quantity = quantity - amount;
    }

    /**
     * Add quantity (for stock returns or adjustments)
     */
    public void addQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add negative quantity: " + amount);
        }
        if (quantity == null) {
            this.quantity = amount;
        } else {
            this.quantity = quantity + amount;
        }
    }

    /**
     * Get movement description for logging and display
     */
    public String getMovementDescription() {
        StringBuilder description = new StringBuilder();
        description.append(movementType.getDisplayName())
                .append(" - ")
                .append(quantity != null ? quantity : 0)
                .append(" units");

        if (product != null) {
            description.append(" of ").append(product.getName());
        }

        if (unitPrice != null) {
            description.append(" at ").append(unitPrice).append(" each");
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
     * FIXED: Validate inventory movement with proper FIFO support
     */
    private void validateMovement() {
        if (quantity == null) {
            throw new IllegalStateException("Quantity cannot be null");
        }
        
        // Allow 0 quantity for depleted FIFO batches, but not negative
        if (quantity < 0) {
            throw new IllegalStateException("Quantity cannot be negative, got: " + quantity);
        }
        
        // For new IN movements, quantity should be > 0
        if (movementType == MovementType.IN && id == null && quantity <= 0) {
            throw new IllegalStateException("New IN movement quantity must be greater than zero, got: " + quantity);
        }
        
        // Unit price validation
        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Unit price cannot be negative");
        }
        
        // For IN movements, unit price is typically required (but can be flexible)
        if (movementType == MovementType.IN && id == null && unitPrice == null) {
            throw new IllegalStateException("Unit price is required for new IN movements");
        }
        
        // Supplier requirement is more flexible - not all IN movements need suppliers
        // (e.g., returns, transfers, adjustments)
    }

    /**
     * Validate for FIFO batch operations specifically
     */
    public void validateForFIFO() {
        if (product == null) {
            throw new IllegalStateException("Product is required for FIFO operations");
        }
        if (date == null) {
            throw new IllegalStateException("Date is required for FIFO operations");
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
        // More lenient validation on update to allow FIFO quantity changes
        if (quantity != null && quantity < 0) {
            throw new IllegalStateException("Quantity cannot be negative during update: " + quantity);
        }
        
        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Unit price cannot be negative during update");
        }
    }

    // Explicit Getters and Setters (ensuring proper access)
    
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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

    // Getters and setters for expiry date and minimum stock
    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    // Payment Tracking Getters and Setters
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public LocalDate getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(LocalDate checkDate) {
        this.checkDate = checkDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Utility methods for debugging
    @Override
    public String toString() {
        return "Inventory{" +
                "id=" + id +
                ", product=" + (product != null ? product.getName() : "null") +
                ", quantity=" + quantity +
                ", movementType=" + movementType +
                ", unitPrice=" + unitPrice +
                ", date=" + date +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inventory)) return false;
        Inventory inventory = (Inventory) o;
        return id != null && id.equals(inventory.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}