package org.example.inventorymanagementbackend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.example.inventorymanagementbackend.entity.Inventory;
import org.example.inventorymanagementbackend.entity.Product;
import org.example.inventorymanagementbackend.entity.Supplier;
import org.example.inventorymanagementbackend.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Existing methods
    List<Inventory> findByProductOrderByDateDesc(Product product);

    List<Inventory> findBySupplierOrderByDateDesc(Supplier supplier);

    List<Inventory> findByMovementTypeOrderByDateDesc(Inventory.MovementType movementType);

    @Query("SELECT i FROM Inventory i ORDER BY i.date DESC")
    List<Inventory> findAllOrderByDateDesc();

    @Query("SELECT i FROM Inventory i WHERE i.date BETWEEN :startDate AND :endDate ORDER BY i.date DESC")
    List<Inventory> findByDateRangeOrderByDateDesc(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM Inventory i WHERE " +
            "(:productId IS NULL OR i.product.id = :productId) AND " +
            "(:supplierId IS NULL OR i.supplier.id = :supplierId) AND " +
            "(:movementType IS NULL OR i.movementType = :movementType) " +
            "ORDER BY i.date DESC")
    List<Inventory> findWithFilters(@Param("productId") Long productId,
                                    @Param("supplierId") Long supplierId,
                                    @Param("movementType") Inventory.MovementType movementType);

    @Query("SELECT COALESCE(SUM(CASE WHEN i.movementType = 'IN' THEN i.quantity ELSE -i.quantity END), 0) " +
            "FROM Inventory i WHERE i.product.id = :productId")
    Integer calculateStockForProduct(@Param("productId") Long productId);

    // New methods for FIFO management
    
    /**
     * Find available stock batches for a product in FIFO order (oldest first)
     * Only returns IN movements with quantity > 0
     */
    @Query("SELECT i FROM Inventory i " +
           "WHERE i.product.id = :productId " +
           "AND i.movementType = 'IN' " +
           "AND i.quantity > 0 " +
           "ORDER BY i.date ASC")
    List<Inventory> findAvailableStockForProductFIFO(@Param("productId") Long productId);

    /**
     * Find all IN movements for a product (for stock tracking)
     */
    @Query("SELECT i FROM Inventory i " +
           "WHERE i.product.id = :productId " +
           "AND i.movementType = 'IN' " +
           "ORDER BY i.date DESC")
    List<Inventory> findInMovementsByProduct(@Param("productId") Long productId);

    /**
     * Get total available quantity for a product (sum of remaining quantities in batches)
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i " +
           "WHERE i.product.id = :productId " +
           "AND i.movementType = 'IN' " +
           "AND i.quantity > 0")
    Integer getTotalAvailableQuantity(@Param("productId") Long productId);

    /**
     * Find inventory batch by ID for FIFO processing
     */
    @Query("SELECT i FROM Inventory i " +
           "WHERE i.id = :id " +
           "AND i.movementType = 'IN' " +
           "AND i.quantity > 0")
    Inventory findAvailableBatchById(@Param("id") Long id);

    /**
     * Get oldest available batches across all products for aging report
     */
    @Query("SELECT i FROM Inventory i " +
           "WHERE i.movementType = 'IN' " +
           "AND i.quantity > 0 " +
           "ORDER BY i.date ASC")
    List<Inventory> findOldestAvailableStock();

    /**
     * Get all IN batches for a specific product ordered by date (for batch tracking)
     */
    @Query("SELECT i FROM Inventory i " +
           "WHERE i.product.id = :productId " +
           "AND i.movementType = 'IN' " +
           "ORDER BY i.date ASC")
    List<Inventory> findAllBatchesForProduct(@Param("productId") Long productId);

   /**
     * Alternative safe method using Spring Data naming convention
     */
    List<Inventory> findByQuantityGreaterThanOrderByDateAscIdAsc(Integer quantity);

/**
     * Find all inventory with quantity > 0 (this method is needed)
     */
    List<Inventory> findByQuantityGreaterThan(Integer quantity);


    // Add this method to your InventoryRepository interface
List<Inventory> findByProductIdAndMovementTypeOrderByDateAsc(Long productId, Inventory.MovementType movementType);
    

@Query("SELECT CASE WHEN COALESCE(SUM(i.quantity), 0) >= :requiredQuantity THEN true ELSE false END " +
       "FROM Inventory i WHERE i.product.id = :productId AND i.quantity > 0")
Boolean hasAvailableStock(@Param("productId") Long productId, @Param("requiredQuantity") Integer requiredQuantity);

// Add these methods to your InventoryRepository interface

/**
 * Get total available stock for a product (sum of positive quantities only)
 */
@Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.product.id = :productId AND i.quantity > 0")
Integer getTotalAvailableStockForProduct(@Param("productId") Long productId);

/**
 * Find inventory by product ID ordered by date ascending
 */
@Query("SELECT i FROM Inventory i WHERE i.product.id = :productId ORDER BY i.date ASC, i.id ASC")
List<Inventory> findByProductIdOrderByDateAsc(@Param("productId") Long productId);

/**
 * Find all inventory records with low stock (you'll need to implement this based on your business logic)
 * This is a placeholder - adjust according to your needs
 */
@Query("SELECT i FROM Inventory i WHERE i.quantity > 0 AND i.quantity <= 10")
List<Inventory> findLowStockInventories();

/**
 * Get inventory summary (you'll need to implement this based on your database schema)
 * This is a placeholder - adjust the query according to your needs
 */
@Query("SELECT i.product.id as productId, " +
       "i.product.name as productName, " +
       "SUM(CASE WHEN i.quantity > 0 THEN i.quantity ELSE 0 END) as totalStock, " +
       "COUNT(CASE WHEN i.quantity > 0 THEN 1 END) as activeBatches, " +
       "COUNT(CASE WHEN i.quantity = 0 THEN 1 END) as depletedBatches " +
       "FROM Inventory i " +
       "GROUP BY i.product.id, i.product.name " +
       "ORDER BY i.product.name")
List<Object[]> getInventorySummary();

/**
 * Find all inventory records that are depleted (quantity = 0)
 */
@Query("SELECT i FROM Inventory i WHERE i.quantity = 0")
List<Inventory> findDepletedInventories();

/**
 * Delete all inventory records with zero quantity (cleanup method)
 */
@Modifying
@Query("DELETE FROM Inventory i WHERE i.quantity = 0")
void deleteDepletedInventories();

    /**
     * Find inventory movements by payment status and movement type
     * Used to find pending payments
     */
    List<Inventory> findByPaymentStatusAndMovementType(PaymentStatus paymentStatus, Inventory.MovementType movementType);

}
