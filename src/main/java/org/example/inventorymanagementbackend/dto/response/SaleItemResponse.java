package org.example.inventorymanagementbackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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
    private LocalDateTime createdAt;
    private BigDecimal discountAmount;
    private BigDecimal discountedUnitPrice;
    private BigDecimal subtotalBeforeDiscount;
    private String saleItemDescription;

    public SaleItemResponse(Long id, Long saleId, Long productId, String productCode, String productName, Integer quantity, BigDecimal unitPrice, BigDecimal discount, BigDecimal lineTotal, LocalDateTime createdAt, BigDecimal discountAmount, BigDecimal discountedUnitPrice, BigDecimal subtotalBeforeDiscount, String saleItemDescription) {
        this.id = id;
        this.saleId = saleId;
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.lineTotal = lineTotal;
        this.createdAt = createdAt;
        this.discountAmount = discountAmount;
        this.discountedUnitPrice = discountedUnitPrice;
        this.subtotalBeforeDiscount = subtotalBeforeDiscount;
        this.saleItemDescription = saleItemDescription;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountedUnitPrice() {
        return discountedUnitPrice;
    }

    public void setDiscountedUnitPrice(BigDecimal discountedUnitPrice) {
        this.discountedUnitPrice = discountedUnitPrice;
    }

    public BigDecimal getSubtotalBeforeDiscount() {
        return subtotalBeforeDiscount;
    }

    public void setSubtotalBeforeDiscount(BigDecimal subtotalBeforeDiscount) {
        this.subtotalBeforeDiscount = subtotalBeforeDiscount;
    }

    public String getSaleItemDescription() {
        return saleItemDescription;
    }

    public void setSaleItemDescription(String saleItemDescription) {
        this.saleItemDescription = saleItemDescription;
    }
}
