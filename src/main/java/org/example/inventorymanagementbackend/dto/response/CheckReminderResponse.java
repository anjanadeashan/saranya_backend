package org.example.inventorymanagementbackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CheckReminderResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private String checkNumber;
    private String bankName;
    private LocalDate checkDate;
    private Boolean isOverdue;
    private Boolean isDueSoon;
}
