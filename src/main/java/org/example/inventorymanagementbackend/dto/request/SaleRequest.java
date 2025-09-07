package org.example.inventorymanagementbackend.dto.request;

import org.example.inventorymanagementbackend.entity.Sale;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SaleRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Payment method is required")
    private Sale.PaymentMethod paymentMethod;

    // Check payment fields
    @Size(max = 50, message = "Check number must not exceed 50 characters")
    private String checkNumber;

    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    private LocalDate checkDate;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @NotEmpty(message = "Sale items are required")
    @Valid
    private List<SaleItemRequest> saleItems;
}

