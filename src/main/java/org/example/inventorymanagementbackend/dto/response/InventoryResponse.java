package org.example.inventorymanagementbackend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    
    // Flat product fields
    private Long productId;
    private String productCode;
    private String productName;
    private Integer currentStock; // Add this for product stock info
    
    private Integer quantity;
    private String movementType;
    private BigDecimal unitPrice;
    
    // Flat supplier fields
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    
    private LocalDateTime date;
    private String reference;
    private LocalDateTime createdAt;
    private String movementDescription;

    // Payment Tracking Fields
    private BigDecimal purchasePrice;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal paidAmount;
    private BigDecimal totalCost;  // Calculated: quantity * purchasePrice
    private BigDecimal remainingAmount;  // Calculated: totalCost - paidAmount
    private String checkNumber;
    private LocalDate checkDate;
    private String notes;

    public BigDecimal getTotalValue() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
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

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
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