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
import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer Entity
 * Represents customers in the inventory system
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_name", columnList = "name"),
        @Index(name = "idx_customer_email", columnList = "email")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Customer name is required")
    @Size(max = 200, message = "Customer name must not exceed 200 characters")
    private String name;

    @Column(length = 100)
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Column(length = 20)
    @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    @Column(length = 500)
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Column(length = 100)
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Column(length = 100)
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Column(name = "credit_limit", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Credit limit cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid credit limit format")
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "outstanding_balance", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Outstanding balance cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid outstanding balance format")
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sale> sales;

    // Business Logic Methods

    /**
     * Get available credit
     */
    public BigDecimal getAvailableCredit() {
        if (creditLimit == null) {
            return BigDecimal.ZERO;
        }
        if (outstandingBalance == null) {
            return creditLimit;
        }
        return creditLimit.subtract(outstandingBalance);
    }

    /**
     * Get credit utilization percentage
     */
    public BigDecimal getCreditUtilizationPercentage() {
        if (creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (outstandingBalance == null) {
            return BigDecimal.ZERO;
        }
        return outstandingBalance.divide(creditLimit, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Check if customer has credit risk
     */
    public boolean hasCreditRisk() {
        BigDecimal utilization = getCreditUtilizationPercentage();
        return utilization.compareTo(BigDecimal.valueOf(90)) >= 0;
    }

    /**
     * Check if customer has high credit usage
     */
    public boolean hasHighCreditUsage() {
        BigDecimal utilization = getCreditUtilizationPercentage();
        return utilization.compareTo(BigDecimal.valueOf(70)) >= 0 &&
                utilization.compareTo(BigDecimal.valueOf(90)) < 0;
    }

    /**
     * Check if customer can make a purchase with given amount
     */
    public boolean canMakePurchase(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return getAvailableCredit().compareTo(amount) >= 0;
    }

    /**
     * Add to outstanding balance
     */
    public void addToOutstandingBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (outstandingBalance == null) {
            outstandingBalance = BigDecimal.ZERO;
        }
        outstandingBalance = outstandingBalance.add(amount);
    }

    /**
     * Reduce outstanding balance (when payment is made)
     */
    public void reduceOutstandingBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (outstandingBalance == null) {
            outstandingBalance = BigDecimal.ZERO;
        }
        if (outstandingBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Cannot reduce balance below zero");
        }
        outstandingBalance = outstandingBalance.subtract(amount);
    }

    /**
     * Get full contact information
     */
    public String getFullContactInfo() {
        StringBuilder contact = new StringBuilder();

        if (email != null && !email.trim().isEmpty()) {
            contact.append("Email: ").append(email);
        }

        if (phone != null && !phone.trim().isEmpty()) {
            if (contact.length() > 0) contact.append(" | ");
            contact.append("Phone: ").append(phone);
        }

        return contact.toString();
    }

    /**
     * Get full address
     */
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();

        if (this.address != null && !this.address.trim().isEmpty()) {
            address.append(this.address);
        }

        if (city != null && !city.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }

        if (country != null && !country.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(country);
        }

        return address.toString();
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
        if (creditLimit == null) {
            creditLimit = BigDecimal.ZERO;
        }
        if (outstandingBalance == null) {
            outstandingBalance = BigDecimal.ZERO;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
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

    public List<Sale> getSales() {
        return sales;
    }

    public void setSales(List<Sale> sales) {
        this.sales = sales;
    }
}