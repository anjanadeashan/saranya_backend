package org.example.inventorymanagementbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesVsPurchasesResponse {
    private String month;
    private BigDecimal sales;
    private BigDecimal purchases;
}
