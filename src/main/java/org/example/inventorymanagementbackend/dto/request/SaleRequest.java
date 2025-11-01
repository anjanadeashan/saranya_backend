package org.example.inventorymanagementbackend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.inventorymanagementbackend.entity.Sale;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SaleRequest {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Payment method is required")
    private Sale.PaymentMethod paymentMethod;

    // Check payment fields - conditionally validated in service layer
    @Size(max = 50, message = "Check number must not exceed 50 characters")
    private String checkNumber;

    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    private LocalDate checkDate;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Total amount field - optional since service calculates it
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid total amount format")
    private BigDecimal totalAmount;

    @NotEmpty(message = "Sale items are required")
    @Valid
    private List<SaleItemRequest> saleItems;

      @Column(name = "check_bounced")
private boolean checkBounced = false;

@Column(name = "check_bounced_date")
private LocalDateTime checkBouncedDate;

@Column(name = "check_bounced_notes", length = 500)
private String checkBouncedNotes;

    // Constructors
    public SaleRequest() {
    }

    public SaleRequest(Long customerId, Sale.PaymentMethod paymentMethod, List<SaleItemRequest> saleItems) {
        this.customerId = customerId;
        this.paymentMethod = paymentMethod;
        this.saleItems = saleItems;
    }

    // Getters and Setters


    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Sale.PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Sale.PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<SaleItemRequest> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(List<SaleItemRequest> saleItems) {
        this.saleItems = saleItems;
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



    @Override
    public String toString() {
        return "SaleRequest{" +
                "customerId=" + customerId +
                ", paymentMethod=" + paymentMethod +
                ", checkNumber='" + checkNumber + '\'' +
                ", bankName='" + bankName + '\'' +
                ", checkDate=" + checkDate +
                ", notes='" + notes + '\'' +
                ", totalAmount=" + totalAmount +
                ", saleItems=" + (saleItems != null ? saleItems.size() + " items" : "null") +
                '}';
    }
}