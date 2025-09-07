package org.example.inventorymanagementbackend.repository;

import org.example.inventorymanagementbackend.entity.SaleItem;
import org.example.inventorymanagementbackend.entity.Sale;
import org.example.inventorymanagementbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

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

    @Query("SELECT COALESCE(SUM(si.quantity), 0) FROM SaleItem si WHERE si.product.id = :productId")
    Long getTotalQuantitySoldForProduct(@Param("productId") Long productId);
}
