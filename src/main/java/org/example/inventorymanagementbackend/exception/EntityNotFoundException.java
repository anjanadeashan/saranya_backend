package org.example.inventorymanagementbackend.exception;

/**
 * Entity Not Found Exception
 * Thrown when a requested entity is not found in the database
 */
public class EntityNotFoundException extends RuntimeException {

    private final String entityType;
    private final Object entityId;

    public EntityNotFoundException(String entityType, Object entityId) {
        super(String.format("%s not found with id: %s", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public EntityNotFoundException(String message) {
        super(message);
        this.entityType = "Unknown";
        this.entityId = null;
    }

    public EntityNotFoundException(String message, String entityType, Object entityId) {
        super(message);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public Object getEntityId() {
        return entityId;
    }
}

