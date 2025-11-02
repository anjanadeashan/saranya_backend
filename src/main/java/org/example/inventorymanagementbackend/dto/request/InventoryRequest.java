package org.example.inventorymanagementbackend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Movement type is required")
    @Pattern(regexp = "^(IN|OUT)$", message = "Movement type must be either IN or OUT")
    private String movementType;

    // ADD THIS FIELD
    @DecimalMin(value = "0.0", inclusive = true, message = "Unit price must be non-negative")
    private Double unitPrice;

    private Long supplierId;

    @NotNull(message = "Date is required")
    private LocalDateTime date;

    @Size(max = 200, message = "Reference must not exceed 200 characters")
    private String reference;

    // Payment Tracking Fields
    private BigDecimal purchasePrice;
    private String paymentMethod;  // String in DTO, will be converted to enum
    private String paymentStatus;  // String in DTO, will be converted to enum
    private BigDecimal paidAmount;
    private String checkNumber;
    private LocalDate checkDate;
    private String notes;

    // Custom validation method
    @AssertTrue(message = "Supplier is required for stock IN movements")
    public boolean isSupplierValidForStockIn() {
        return !"IN".equals(movementType) || supplierId != null;
    }

    @AssertTrue(message = "Supplier should not be specified for stock OUT movements")
    public boolean isSupplierValidForStockOut() {
        return !"OUT".equals(movementType) || supplierId == null;
    }

    // Payment Tracking Getters and Setters
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
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
}