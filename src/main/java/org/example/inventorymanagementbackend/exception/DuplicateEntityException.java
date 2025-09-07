package org.example.inventorymanagementbackend.exception;

/**
 * Duplicate Entity Exception
 * Thrown when trying to create an entity that already exists
 */
public class DuplicateEntityException extends RuntimeException {

    private final String entityType;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateEntityException(String entityType, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: %s", entityType, fieldName, fieldValue));
        this.entityType = entityType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public DuplicateEntityException(String message) {
        super(message);
        this.entityType = "Unknown";
        this.fieldName = "Unknown";
        this.fieldValue = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}

