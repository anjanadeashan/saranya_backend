package org.example.inventorymanagementbackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardSummaryResponse {
    private Long totalProducts;
    private BigDecimal totalStockValue;
    private Long totalIssues;
    private BigDecimal unpaidSalesAmount;
    private Long totalCustomers;
    private Long totalSuppliers;
    private Long totalSales;
}
