package org.example.inventorymanagementbackend.repository;

import org.example.inventorymanagementbackend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByIsActiveTrueOrderByNameAsc();

    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND " +
            "(UPPER(c.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
            "UPPER(c.email) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    List<Customer> searchByNameOrEmail(@Param("searchTerm") String searchTerm);

    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND c.outstandingBalance > 0")
    List<Customer> findCustomersWithOutstandingBalance();

    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND " +
            "c.creditLimit > 0 AND (c.outstandingBalance / c.creditLimit) >= 0.9")
    List<Customer> findCustomersWithCreditRisk();

    @Query("SELECT COALESCE(SUM(c.outstandingBalance), 0) FROM Customer c WHERE c.isActive = true")
    BigDecimal getTotalOutstandingBalance();

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.isActive = true")
    Long countActiveCustomers();
}
