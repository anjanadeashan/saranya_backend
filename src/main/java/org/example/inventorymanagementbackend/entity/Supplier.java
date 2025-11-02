package org.example.inventorymanagementbackend.entity;



import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Supplier Entity
 * Represents suppliers in the inventory system
 */
@Entity
@Table(name = "suppliers", indexes = {
        @Index(name = "idx_supplier_code", columnList = "uniqueSupplierCode", unique = true),
        @Index(name = "idx_supplier_name", columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unique_supplier_code", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Supplier code is required")
    @Size(max = 50, message = "Supplier code must not exceed 50 characters")
    private String uniqueSupplierCode;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Supplier name is required")
    @Size(max = 200, message = "Supplier name must not exceed 200 characters")
    private String name;

    @Column(name = "contact_person", length = 100)
    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    private String contactPerson;

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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Financial Tracking Fields
    @Column(name = "outstanding_balance", precision = 10, scale = 2)
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(name = "total_purchases", precision = 10, scale = 2)
    private BigDecimal totalPurchases = BigDecimal.ZERO;

    @Column(name = "total_paid", precision = 10, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(name = "last_purchase_date")
    private LocalDateTime lastPurchaseDate;

    // Relationships
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Inventory> inventoryMovements;

    // Business Logic Methods

    /**
     * Get full contact information
     */
    public String getFullContactInfo() {
        StringBuilder contact = new StringBuilder();

        if (contactPerson != null && !contactPerson.trim().isEmpty()) {
            contact.append("Contact: ").append(contactPerson);
        }

        if (email != null && !email.trim().isEmpty()) {
            if (contact.length() > 0) contact.append(" | ");
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

    /**
     * Check if supplier has complete contact information
     */
    public boolean hasCompleteContactInfo() {
        return email != null && !email.trim().isEmpty() &&
                phone != null && !phone.trim().isEmpty();
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

    public String getUniqueSupplierCode() {
        return uniqueSupplierCode;
    }

    public void setUniqueSupplierCode(String uniqueSupplierCode) {
        this.uniqueSupplierCode = uniqueSupplierCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
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

    // Financial Tracking Getters and Setters
    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public BigDecimal getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(BigDecimal totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public LocalDateTime getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setLastPurchaseDate(LocalDateTime lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }

    // Financial Tracking Business Logic Methods

    /**
     * Add a purchase to this supplier's financial tracking
     * @param amount Total purchase amount
     * @param isPaid Whether the purchase was paid immediately
     */
    public void addPurchase(BigDecimal amount, boolean isPaid) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Purchase amount cannot be null or negative");
        }

        this.totalPurchases = this.totalPurchases.add(amount);

        if (isPaid) {
            this.totalPaid = this.totalPaid.add(amount);
        } else {
            this.outstandingBalance = this.outstandingBalance.add(amount);
        }

        this.lastPurchaseDate = LocalDateTime.now();
    }

    /**
     * Record a payment made to this supplier
     * @param amount Payment amount
     */
    public void recordPayment(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        if (amount.compareTo(this.outstandingBalance) > 0) {
            throw new IllegalArgumentException("Payment amount cannot exceed outstanding balance");
        }

        this.totalPaid = this.totalPaid.add(amount);
        this.outstandingBalance = this.outstandingBalance.subtract(amount);

        // Ensure outstanding balance doesn't go negative due to rounding
        if (this.outstandingBalance.compareTo(BigDecimal.ZERO) < 0) {
            this.outstandingBalance = BigDecimal.ZERO;
        }
    }
}
