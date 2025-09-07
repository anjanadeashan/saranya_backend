package org.example.inventorymanagementbackend.entity;



import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sale Item Entity
 * Represents individual items in a sale transaction
 */
@Entity
@Table(name = "sale_items", indexes = {
        @Index(name = "idx_sale_item_sale", columnList = "sale_id"),
        @Index(name = "idx_sale_item_product", columnList = "product_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @NotNull(message = "Sale is required")
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid unit price format")
    private BigDecimal unitPrice;

    @Column(precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Line total is required")
    @DecimalMin(value = "0.0", message = "Line total cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid line total format")
    private BigDecimal lineTotal;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Business Logic Methods

    /**
     * Calculate line total based on quantity, unit price, and discount
     */
    public BigDecimal calculateLineTotal() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmount = subtotal.multiply(discount)
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            subtotal = subtotal.subtract(discountAmount);
        }

        return subtotal;
    }

    /**
     * Calculate discount amount
     */
    public BigDecimal getDiscountAmount() {
        if (quantity == null || unitPrice == null || discount == null ||
                discount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return subtotal.multiply(discount)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Get unit price after discount
     */
    public BigDecimal getDiscountedUnitPrice() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }

        if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) {
            return unitPrice;
        }

        BigDecimal discountAmount = unitPrice.multiply(discount)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        return unitPrice.subtract(discountAmount);
    }

    /**
     * Get subtotal before discount
     */
    public BigDecimal getSubtotalBeforeDiscount() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Update line total
     */
    public void updateLineTotal() {
        this.lineTotal = calculateLineTotal();
    }

    /**
     * Validate sale item
     */
    public void validateSaleItem() {
        if (product == null) {
            throw new IllegalStateException("Product is required");
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalStateException("Quantity must be positive");
        }

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Unit price must be positive");
        }

        if (discount != null && (discount.compareTo(BigDecimal.ZERO) < 0 ||
                discount.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalStateException("Discount must be between 0 and 100");
        }
    }

    /**
     * Check if product has sufficient stock
     */
    public boolean hasProductSufficientStock() {
        return product != null && product.hasSufficientStock(quantity);
    }

    /**
     * Get sale item description
     */
    public String getSaleItemDescription() {
        StringBuilder description = new StringBuilder();

        if (product != null) {
            description.append(product.getName())
                    .append(" (").append(product.getCode()).append(")");
        }

        description.append(" - Qty: ").append(quantity);
        description.append(" @ $").append(unitPrice);

        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            description.append(" (").append(discount).append("% off)");
        }

        description.append(" = $").append(lineTotal);

        return description.toString();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (discount == null) {
            discount = BigDecimal.ZERO;
        }

        // Validate business rules
        validateSaleItem();

        // Calculate line total if not set
        if (lineTotal == null) {
            updateLineTotal();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Validate business rules on update
        validateSaleItem();

        // Recalculate line total
        updateLineTotal();
    }

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
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
