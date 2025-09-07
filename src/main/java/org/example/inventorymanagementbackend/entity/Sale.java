package org.example.inventorymanagementbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sale Entity
 * Represents sales transactions in the inventory system
 */
@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sale_customer", columnList = "customer_id"),
        @Index(name = "idx_sale_date", columnList = "saleDate"),
        @Index(name = "idx_sale_payment_method", columnList = "paymentMethod"),
        @Index(name = "idx_sale_check_date", columnList = "checkDate")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Customer is required")
    private Customer customer;

    @Column(name = "sale_date", nullable = false)
    @NotNull(message = "Sale date is required")
    private LocalDateTime saleDate;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid total amount format")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    // Check payment specific fields
    @Column(name = "check_number", length = 50)
    @Size(max = 50, message = "Check number must not exceed 50 characters")
    private String checkNumber;

    @Column(name = "bank_name", length = 100)
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @Column(name = "check_date")
    private LocalDate checkDate;

    @Column(length = 500)
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SaleItem> saleItems = new ArrayList<>();

    // Enums
    public enum PaymentMethod {
        CASH("Cash"),
        DEBIT_CARD("Debit Card"),
        CREDIT_CARD("Credit Card"),
        BANK_TRANSFER("Bank Transfer"),
        CREDIT_CHECK("Credit (Check)");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean requiresCheckInfo() {
            return this == CREDIT_CHECK;
        }

        public boolean isPaidImmediately() {
            return this != CREDIT_CHECK;
        }
    }

    // Business Logic Methods

    /**
     * Check if this is a check payment
     */
    public boolean isCheckPayment() {
        return PaymentMethod.CREDIT_CHECK.equals(paymentMethod);
    }

    /**
     * Check if payment is immediately processed
     */
    public boolean isImmediatePayment() {
        return paymentMethod != null && paymentMethod.isPaidImmediately();
    }

    /**
     * Check if check is overdue
     */
    public boolean isCheckOverdue() {
        if (!isCheckPayment() || checkDate == null) {
            return false;
        }
        return checkDate.isBefore(LocalDate.now()) && !isPaid;
    }

    /**
     * Check if check is due soon (within 7 days)
     */
    public boolean isCheckDueSoon() {
        if (!isCheckPayment() || checkDate == null) {
            return false;
        }
        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        return !checkDate.isBefore(LocalDate.now()) &&
                !checkDate.isAfter(sevenDaysFromNow) &&
                !isPaid;
    }

    /**
     * Calculate total from sale items
     */
    public BigDecimal calculateTotalFromItems() {
        if (saleItems == null || saleItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return saleItems.stream()
                .map(SaleItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Add sale item
     */
    public void addSaleItem(SaleItem saleItem) {
        if (saleItems == null) {
            saleItems = new ArrayList<>();
        }
        saleItem.setSale(this);
        saleItems.add(saleItem);
        updateTotalAmount();
    }

    /**
     * Remove sale item
     */
    public void removeSaleItem(SaleItem saleItem) {
        if (saleItems != null) {
            saleItems.remove(saleItem);
            saleItem.setSale(null);
            updateTotalAmount();
        }
    }

    /**
     * Update total amount based on sale items
     */
    public void updateTotalAmount() {
        this.totalAmount = calculateTotalFromItems();
    }

    /**
     * Mark sale as paid
     */
    public void markAsPaid() {
        this.isPaid = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if we're in data loading mode (sample data creation)
     */
    private boolean isDataLoadingMode() {
        // Check if this is during application startup/data loading
        return "true".equals(System.getProperty("spring.data.loading")) ||
                // Alternative: check if we're in test mode
                "test".equals(System.getProperty("spring.profiles.active"));
    }

    /**
     * Validate check payment fields
     */
    public void validateCheckPayment() {
        if (isCheckPayment()) {
            if (checkNumber == null || checkNumber.trim().isEmpty()) {
                throw new IllegalStateException("Check number is required for check payments");
            }
            if (bankName == null || bankName.trim().isEmpty()) {
                throw new IllegalStateException("Bank name is required for check payments");
            }
            if (checkDate == null) {
                throw new IllegalStateException("Check date is required for check payments");
            }

            // Only validate future dates in production mode (not during data loading)
            if (!isDataLoadingMode() && checkDate.isBefore(LocalDate.now())) {
                throw new IllegalStateException("Check date cannot be in the past");
            }
        } else {
            // Clear check fields for non-check payments
            this.checkNumber = null;
            this.bankName = null;
            this.checkDate = null;
        }
    }

    /**
     * Get sale description
     */
    public String getSaleDescription() {
        StringBuilder description = new StringBuilder();
        description.append("Sale #").append(id);

        if (customer != null) {
            description.append(" - ").append(customer.getName());
        }

        description.append(" - ").append(paymentMethod.getDisplayName());

        if (isCheckPayment() && checkNumber != null) {
            description.append(" (Check #").append(checkNumber).append(")");
        }

        return description.toString();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (saleDate == null) {
            saleDate = LocalDateTime.now();
        }
        if (isPaid == null) {
            isPaid = isImmediatePayment();
        }

        // Validate check payment requirements
        validateCheckPayment();

        // Calculate total if not set
        if (totalAmount == null) {
            updateTotalAmount();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateCheckPayment();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Boolean getPaid() {
        return isPaid;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
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

    public List<SaleItem> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(List<SaleItem> saleItems) {
        this.saleItems = saleItems;
    }
}