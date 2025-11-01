package org.example.inventorymanagementbackend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SaleItemResponse {

    private Long id;
    private Long saleId;
    private Long productId;
    private String productCode;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal lineTotal;
    
    // New field for FIFO tracking
    private Long inventoryId;
    private BigDecimal inventoryUnitPrice; // Purchase price from inventory batch
    private LocalDateTime inventoryDate; // Date when this batch was received
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public SaleItemResponse() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

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

    // Business methods
    public BigDecimal getGrossProfit() {
        if (inventoryUnitPrice != null && unitPrice != null && quantity != null) {
            BigDecimal costOfGoodsSold = inventoryUnitPrice.multiply(BigDecimal.valueOf(quantity));
            return lineTotal.subtract(costOfGoodsSold);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getGrossProfitMargin() {
        if (lineTotal != null && lineTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal grossProfit = getGrossProfit();
            return grossProfit.divide(lineTotal, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "SaleItemResponse{" +
                "id=" + id +
                ", saleId=" + saleId +
                ", productId=" + productId +
                ", productCode='" + productCode + '\'' +
                ", productName='" + productName + '\'' +
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
}