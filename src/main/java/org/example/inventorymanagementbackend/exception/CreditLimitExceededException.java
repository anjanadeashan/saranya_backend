package org.example.inventorymanagementbackend.exception;

import java.math.BigDecimal;

/**
 * Credit Limit Exceeded Exception
 * Thrown when a customer's credit limit would be exceeded
 */
public class CreditLimitExceededException extends RuntimeException {

    private final String customerName;
    private final BigDecimal creditLimit;
    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;

    public CreditLimitExceededException(String customerName, BigDecimal creditLimit,
                                        BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(String.format("Credit limit exceeded for customer %s. Limit: %s, Current: %s, Requested: %s",
                customerName, creditLimit, currentBalance, requestedAmount));
        this.customerName = customerName;
        this.creditLimit = creditLimit;
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
