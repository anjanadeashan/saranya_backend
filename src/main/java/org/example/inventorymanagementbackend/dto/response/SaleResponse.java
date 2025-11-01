package org.example.inventorymanagementbackend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.inventorymanagementbackend.entity.Sale;

import lombok.Data;

@Data
public class SaleResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private LocalDateTime saleDate;
    private BigDecimal totalAmount;
    private Sale.PaymentMethod paymentMethod;
    private Boolean isPaid;
    private String checkNumber;
    private String bankName;
    private LocalDate checkDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SaleItemResponse> saleItems;
    private Boolean isCheckPayment;
    private Boolean isCheckOverdue;
    private Boolean isCheckDueSoon;
    private String saleDescription;
    private Boolean checkBounced;
    private LocalDateTime checkBouncedDate;
    private String checkBouncedNotes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public Sale.PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Sale.PaymentMethod paymentMethod) {
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

    public List<SaleItemResponse> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(List<SaleItemResponse> saleItems) {
        this.saleItems = saleItems;
    }

    public Boolean getIsCheckPayment() {
        return isCheckPayment;
    }

    public void setIsCheckPayment(Boolean checkPayment) {
        isCheckPayment = checkPayment;
    }

    public Boolean getIsCheckOverdue() {
        return isCheckOverdue;
    }

    public void setIsCheckOverdue(Boolean checkOverdue) {
        isCheckOverdue = checkOverdue;
    }

    public Boolean getIsCheckDueSoon() {
        return isCheckDueSoon;
    }

    public void setIsCheckDueSoon(Boolean checkDueSoon) {
        isCheckDueSoon = checkDueSoon;
    }

    public String getSaleDescription() {
        return saleDescription;
    }

    public void setSaleDescription(String saleDescription) {
        this.saleDescription = saleDescription;
    }


public void setCheckBounced(boolean checkBounced) {
    this.checkBounced = checkBounced;
}

public LocalDateTime getCheckBouncedDate() {
    return checkBouncedDate;
}

public void setCheckBouncedDate(LocalDateTime checkBouncedDate) {
    this.checkBouncedDate = checkBouncedDate;
}

public String getCheckBouncedNotes() {
    return checkBouncedNotes;
}

public void setCheckBouncedNotes(String checkBouncedNotes) {
    this.checkBouncedNotes = checkBouncedNotes;
}


}
