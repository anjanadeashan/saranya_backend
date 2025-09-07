package org.example.inventorymanagementbackend.exception;

/**
 * Invalid Payment Exception
 * Thrown when payment processing fails or payment details are invalid
 */
public class InvalidPaymentException extends RuntimeException {

    private final String paymentMethod;
    private final String reason;

    public InvalidPaymentException(String message) {
        super(message);
        this.paymentMethod = "Unknown";
        this.reason = message;
    }

    public InvalidPaymentException(String message, String paymentMethod) {
        super(message);
        this.paymentMethod = paymentMethod;
        this.reason = message;
    }

    public InvalidPaymentException(String message, String paymentMethod, String reason) {
        super(message);
        this.paymentMethod = paymentMethod;
        this.reason = reason;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getReason() {
        return reason;
    }
}
