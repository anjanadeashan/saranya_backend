package org.example.inventorymanagementbackend.repository;

import org.example.inventorymanagementbackend.entity.Sale;
import org.example.inventorymanagementbackend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByCustomerOrderBySaleDateDesc(Customer customer);

    List<Sale> findByPaymentMethodOrderBySaleDateDesc(Sale.PaymentMethod paymentMethod);

    List<Sale> findByIsPaidOrderBySaleDateDesc(Boolean isPaid);

    @Query("SELECT s FROM Sale s ORDER BY s.saleDate DESC")
    List<Sale> findAllOrderBySaleDateDesc();

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate ORDER BY s.saleDate DESC")
    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Sale s WHERE s.paymentMethod = 'CREDIT_CHECK' AND s.isPaid = false ORDER BY s.checkDate ASC")
    List<Sale> findUnpaidCheckSales();

    @Query("SELECT s FROM Sale s WHERE s.paymentMethod = 'CREDIT_CHECK' AND s.isPaid = false AND " +
            "s.checkDate BETWEEN :startDate AND :endDate ORDER BY s.checkDate ASC")
    List<Sale> findCheckSalesDueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Sale s WHERE s.paymentMethod = 'CREDIT_CHECK' AND s.isPaid = false AND " +
            "s.checkDate < :currentDate ORDER BY s.checkDate ASC")
    List<Sale> findOverdueCheckSales(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.isPaid = false")
    BigDecimal getTotalUnpaidAmount();

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSalesAmountBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    Long countSalesBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Sale s WHERE " +
            "(:customerId IS NULL OR s.customer.id = :customerId) AND " +
            "(:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod) AND " +
            "(:isPaid IS NULL OR s.isPaid = :isPaid) " +
            "ORDER BY s.saleDate DESC")
    List<Sale> findWithFilters(@Param("customerId") Long customerId,
                               @Param("paymentMethod") Sale.PaymentMethod paymentMethod,
                               @Param("isPaid") Boolean isPaid);
}

