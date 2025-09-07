package org.example.inventorymanagementbackend.repository;

import org.example.inventorymanagementbackend.entity.Inventory;
import org.example.inventorymanagementbackend.entity.Product;
import org.example.inventorymanagementbackend.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

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
}

