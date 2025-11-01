package org.example.inventorymanagementbackend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.example.inventorymanagementbackend.entity.Product;
import org.example.inventorymanagementbackend.entity.Sale;
import org.example.inventorymanagementbackend.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    // Existing methods
    List<SaleItem> findBySaleOrderByIdAsc(Sale sale);

    List<SaleItem> findByProductOrderByCreatedAtDesc(Product product);

    @Query("SELECT si FROM SaleItem si WHERE si.sale.saleDate BETWEEN :startDate AND :endDate")
    List<SaleItem> findBySaleDateBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT si.product.id, si.product.code, si.product.name, SUM(si.quantity) as totalQuantity " +
            "FROM SaleItem si WHERE si.sale.saleDate BETWEEN :startDate AND :endDate " +
            "GROUP BY si.product.id, si.product.code, si.product.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProductsBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    // Fixed: Use consistent return type (Long) for the original method
    @Query("SELECT COALESCE(SUM(si.quantity), 0) FROM SaleItem si WHERE si.product.id = :productId")
    Long getTotalQuantitySoldForProduct(@Param("productId") Long productId);

    // New methods for FIFO tracking

    /**
     * Find all sale items for a specific sale
     */
    List<SaleItem> findBySaleId(Long saleId);

    /**
     * Find sale items for a specific product by product ID
     */
    @Query("SELECT si FROM SaleItem si WHERE si.product.id = :productId ORDER BY si.createdAt DESC")
    List<SaleItem> findByProductId(@Param("productId") Long productId);

    /**
     * Find sale items linked to a specific inventory batch
     */
    @Query("SELECT si FROM SaleItem si WHERE si.inventoryId = :inventoryId")
    List<SaleItem> findByInventoryId(@Param("inventoryId") Long inventoryId);

    /**
     * Get total quantity sold for a product as Integer (alternative method)
     */
    @Query("SELECT COALESCE(SUM(si.quantity), 0) FROM SaleItem si WHERE si.product.id = :productId")
    Integer getTotalQuantitySoldAsInteger(@Param("productId") Long productId);

    /**
     * Find sale items with inventory batch details for COGS calculation
     */
    @Query("SELECT si FROM SaleItem si " +
           "JOIN si.sale s " +
           "WHERE si.product.id = :productId " +
           "ORDER BY s.saleDate DESC")
    List<SaleItem> findSaleItemsWithInventoryByProduct(@Param("productId") Long productId);

    /**
     * Get all sale items that used a specific inventory batch
     */
    @Query("SELECT si FROM SaleItem si " +
           "WHERE si.inventoryId = :inventoryId " +
           "ORDER BY si.createdAt ASC")
    List<SaleItem> findAllByInventoryBatch(@Param("inventoryId") Long inventoryId);

    /**
     * Get sale items by sale with inventory tracking
     */
    @Query("SELECT si FROM SaleItem si " +
           "WHERE si.sale.id = :saleId " +
           "ORDER BY si.id ASC")
    List<SaleItem> findBySaleIdWithInventory(@Param("saleId") Long saleId);

    // Add this method to your existing SaleRepository.java interface


}