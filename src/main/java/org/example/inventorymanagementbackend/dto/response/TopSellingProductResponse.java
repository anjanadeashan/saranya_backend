package org.example.inventorymanagementbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductResponse {
    private Long productId;
    private String productCode;
    private String productName;
    private Long quantitySold;
}
