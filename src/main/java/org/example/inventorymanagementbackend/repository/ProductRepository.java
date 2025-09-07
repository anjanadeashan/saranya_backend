package org.example.inventorymanagementbackend.repository;



import org.example.inventorymanagementbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by unique code
     */
    Optional<Product> findByCodeIgnoreCase(String code);

    /**
     * Check if product exists by code (excluding specific id)
     */
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    /**
     * Check if product exists by code
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Find all active products
     */
    List<Product> findByIsActiveTrueOrderByNameAsc();

    /**
     * Find products with low stock
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.currentStock <= p.lowStockThreshold")
    List<Product> findLowStockProducts();

    /**
     * Search products by code or name
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
            "(UPPER(p.code) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
            "UPPER(p.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    List<Product> searchByCodeOrName(@Param("searchTerm") String searchTerm);

    /**
     * Find products by price range
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.fixedPrice BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find products with stock below given quantity
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.currentStock < :quantity")
    List<Product> findProductsWithStockBelow(@Param("quantity") Integer quantity);

    /**
     * Get total stock value
     */
    @Query("SELECT COALESCE(SUM(p.currentStock * p.fixedPrice), 0) FROM Product p WHERE p.isActive = true")
    BigDecimal getTotalStockValue();

    /**
     * Count active products
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true")
    Long countActiveProducts();

    /**
     * Count low stock products
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND p.currentStock <= p.lowStockThreshold")
    Long countLowStockProducts();

    /**
     * Find products by category (if we add category field later)
     */
    List<Product> findByIsActiveTrueOrderByCodeAsc();

    /**
     * Find products that need restocking (current stock is 0)
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.currentStock = 0")
    List<Product> findOutOfStockProducts();

    /**
     * Find top products by current stock
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.currentStock DESC")
    List<Product> findTopProductsByStock();

    /**
     * Update product stock
     */
    @Query("UPDATE Product p SET p.currentStock = p.currentStock + :quantity WHERE p.id = :productId")
    void updateProductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Find products with discount
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.discount > 0")
    List<Product> findProductsWithDiscount();

    /**
     * Search products with advanced criteria
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
            "(:code IS NULL OR UPPER(p.code) LIKE UPPER(CONCAT('%', :code, '%'))) AND " +
            "(:name IS NULL OR UPPER(p.name) LIKE UPPER(CONCAT('%', :name, '%'))) AND " +
            "(:minPrice IS NULL OR p.fixedPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.fixedPrice <= :maxPrice)")
    List<Product> findProductsWithCriteria(@Param("code") String code,
                                           @Param("name") String name,
                                           @Param("minPrice") BigDecimal minPrice,
                                           @Param("maxPrice") BigDecimal maxPrice);
}