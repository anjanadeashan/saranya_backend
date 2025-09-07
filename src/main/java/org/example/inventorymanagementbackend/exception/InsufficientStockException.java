package org.example.inventorymanagementbackend.exception;

/**
 * Insufficient Stock Exception
 * Thrown when there's not enough stock for a requested operation
 */
public class InsufficientStockException extends RuntimeException {

    private final String productCode;
    private final Integer availableStock;
    private final Integer requestedStock;

    public InsufficientStockException(String productCode, Integer availableStock, Integer requestedStock) {
        super(String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                productCode, availableStock, requestedStock));
        this.productCode = productCode;
        this.availableStock = availableStock;
        this.requestedStock = requestedStock;
    }

    public InsufficientStockException(String message, String productCode, Integer availableStock, Integer requestedStock) {
        super(message);
        this.productCode = productCode;
        this.availableStock = availableStock;
        this.requestedStock = requestedStock;
    }

    public String getProductCode() {
        return productCode;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public Integer getRequestedStock() {
        return requestedStock;
    }
}