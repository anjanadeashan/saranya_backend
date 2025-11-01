package org.example.inventorymanagementbackend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "sale_items")
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "Unit price cannot be null")
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    // FIFO inventory tracking fields
    @Column(name = "inventory_id")
    private Long inventoryId;

    // NEW: Track the unit price from the inventory batch (FIFO cost tracking)
    @Column(name = "inventory_unit_price", precision = 10, scale = 2)
    private BigDecimal inventoryUnitPrice;

    // NEW: Track the date from the inventory batch (FIFO batch date)
    @Column(name = "inventory_date")
    private LocalDateTime inventoryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public SaleItem() {
    }

    public SaleItem(Sale sale, Product product, Integer quantity, BigDecimal unitPrice) {
        this.sale = sale;
        this.product = product;
        setQuantity(quantity);  // Use setter for validation
        setUnitPrice(unitPrice);  // Use setter for validation
        this.discount = BigDecimal.ZERO;
        updateLineTotal();
    }

    // Enhanced constructor with inventory tracking
    public SaleItem(Sale sale, Product product, Integer quantity, BigDecimal unitPrice, 
                   Long inventoryId, BigDecimal inventoryUnitPrice, LocalDateTime inventoryDate) {
        this(sale, product, quantity, unitPrice);
        this.inventoryId = inventoryId;
        this.inventoryUnitPrice = inventoryUnitPrice;
        this.inventoryDate = inventoryDate;
    }

    // Lifecycle methods
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        validateAndUpdateLineTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        validateAndUpdateLineTotal();
    }

    // Enhanced business methods with validation
    public void updateLineTotal() {
        validateAndUpdateLineTotal();
    }

    private void validateAndUpdateLineTotal() {
        // Validate before calculating
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero, got: " + quantity);
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero, got: " + unitPrice);
        }

        try {
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal effectiveDiscount = (discount != null) ? discount : BigDecimal.ZERO;
            this.lineTotal = subtotal.subtract(effectiveDiscount);

            // Ensure line total is not negative
            if (this.lineTotal.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Line total cannot be negative. Check discount amount.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error calculating line total: " + e.getMessage(), e);
        }
    }

    // FIFO Business Methods
    
    /**
     * Calculate the cost of goods sold (COGS) for this sale item using FIFO inventory price
     */
    public BigDecimal calculateCOGS() {
        if (inventoryUnitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return inventoryUnitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Calculate gross profit for this sale item (Sale Price - FIFO Cost)
     */
    public BigDecimal calculateGrossProfit() {
        if (lineTotal == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal cogs = calculateCOGS();
        return lineTotal.subtract(cogs);
    }

    /**
     * Check if this sale item has inventory tracking information
     */
    public boolean hasInventoryTracking() {
        return inventoryId != null;
    }

    // Getters and Setters with validation
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
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
        // Validate quantity before setting
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero, got: " + quantity);
        }
        
        this.quantity = quantity;
        
        // Only update line total if other required fields are set
        if (this.unitPrice != null) {
            updateLineTotal();
        }
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        // Validate unit price before setting
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero, got: " + unitPrice);
        }
        
        this.unitPrice = unitPrice;
        
        // Only update line total if other required fields are set
        if (this.quantity != null && this.quantity > 0) {
            updateLineTotal();
        }
    }

    public BigDecimal getDiscount() {
        return discount != null ? discount : BigDecimal.ZERO;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = (discount != null) ? discount : BigDecimal.ZERO;
        
        // Validate discount is not negative
        if (this.discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount cannot be negative, got: " + discount);
        }
        
        // Update line total if other fields are set
        if (this.unitPrice != null && this.quantity != null && this.quantity > 0) {
            updateLineTotal();
        }
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    // NEW: Getters and setters for inventory tracking fields
    public BigDecimal getInventoryUnitPrice() {
        return inventoryUnitPrice;
    }

    public void setInventoryUnitPrice(BigDecimal inventoryUnitPrice) {
        this.inventoryUnitPrice = inventoryUnitPrice;
    }

    public LocalDateTime getInventoryDate() {
        return inventoryDate;
    }

    public void setInventoryDate(LocalDateTime inventoryDate) {
        this.inventoryDate = inventoryDate;
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

    // Utility methods
    public boolean isValid() {
        return quantity != null && quantity > 0 &&
               unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0 &&
               (discount == null || discount.compareTo(BigDecimal.ZERO) >= 0);
    }

    /**
     * Get a summary description for this sale item
     */
    public String getItemSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (product != null) {
            summary.append(product.getName()).append(" ");
        }
        
        summary.append("(Qty: ").append(quantity)
               .append(", Unit Price: ").append(unitPrice);
        
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            summary.append(", Discount: ").append(discount);
        }
        
        summary.append(", Total: ").append(lineTotal).append(")");
        
        if (hasInventoryTracking()) {
            summary.append(" [FIFO Batch: ").append(inventoryId).append("]");
        }
        
        return summary.toString();
    }

    @Override
    public String toString() {
        return "SaleItem{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", discount=" + discount +
                ", lineTotal=" + lineTotal +
                ", inventoryId=" + inventoryId +
                ", inventoryUnitPrice=" + inventoryUnitPrice +
                ", inventoryDate=" + inventoryDate +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SaleItem)) return false;
        SaleItem saleItem = (SaleItem) o;
        return id != null && id.equals(saleItem.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}