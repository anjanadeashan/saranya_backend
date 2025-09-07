package org.example.inventorymanagementbackend.repository;



import org.example.inventorymanagementbackend.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByUniqueSupplierCodeIgnoreCase(String code);

    boolean existsByUniqueSupplierCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByUniqueSupplierCodeIgnoreCase(String code);

    List<Supplier> findByIsActiveTrueOrderByNameAsc();

    @Query("SELECT s FROM Supplier s WHERE s.isActive = true AND " +
            "(UPPER(s.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
            "UPPER(s.uniqueSupplierCode) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
    List<Supplier> searchByNameOrCode(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.isActive = true")
    Long countActiveSuppliers();
}