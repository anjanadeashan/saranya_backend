package org.example.inventorymanagementbackend.service;

import org.example.inventorymanagementbackend.dto.request.SupplierRequest;
import org.example.inventorymanagementbackend.dto.response.SupplierResponse;
import org.example.inventorymanagementbackend.entity.Supplier;
import org.example.inventorymanagementbackend.mapper.SupplierMapper;
import org.example.inventorymanagementbackend.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplierMapper supplierMapper;

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        try {
            List<Supplier> suppliers = supplierRepository.findByIsActiveTrueOrderByNameAsc();
            return suppliers.stream()
                    .map(supplierMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all suppliers: ", e);
            throw new RuntimeException("Failed to fetch suppliers", e);
        }
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
        return supplierMapper.toResponse(supplier);
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierEntityById(Long id) {
        logger.debug("Getting supplier entity by ID: {}", id);
        return supplierRepository.findById(id)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> {
                    logger.error("Supplier not found with ID: {}", id);
                    return new IllegalArgumentException("Supplier not found with ID: " + id);
                });
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierByCode(String code) {
        logger.debug("Getting supplier entity by code: {}", code);
        return supplierRepository.findByUniqueSupplierCodeIgnoreCase(code)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> {
                    logger.error("Supplier not found with code: {}", code);
                    return new IllegalArgumentException("Supplier not found with code: " + code);
                });
    }

    public SupplierResponse createSupplier(SupplierRequest request) {
        try {
            if (supplierRepository.existsByUniqueSupplierCodeIgnoreCase(request.getUniqueSupplierCode())) {
                throw new IllegalArgumentException("Supplier code already exists: " + request.getUniqueSupplierCode());
            }

            Supplier supplier = supplierMapper.toEntity(request);
            supplier.setIsActive(true);

            Supplier savedSupplier = supplierRepository.save(supplier);
            logger.info("Supplier created successfully with id: {}", savedSupplier.getId());

            return supplierMapper.toResponse(savedSupplier);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating supplier: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating supplier: ", e);
            throw new RuntimeException("Failed to create supplier", e);
        }
    }

    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        try {
            Supplier existingSupplier = supplierRepository.findById(id)
                    .filter(s -> s.getIsActive())
                    .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));

            if (supplierRepository.existsByUniqueSupplierCodeIgnoreCaseAndIdNot(request.getUniqueSupplierCode(), id)) {
                throw new IllegalArgumentException("Supplier code already exists: " + request.getUniqueSupplierCode());
            }

            existingSupplier.setUniqueSupplierCode(request.getUniqueSupplierCode());
            existingSupplier.setName(request.getName());
            existingSupplier.setContactPerson(request.getContactPerson());
            existingSupplier.setEmail(request.getEmail());
            existingSupplier.setPhone(request.getPhone());
            existingSupplier.setAddress(request.getAddress());
            existingSupplier.setCity(request.getCity());
            existingSupplier.setCountry(request.getCountry());

            Supplier savedSupplier = supplierRepository.save(existingSupplier);
            logger.info("Supplier updated successfully with id: {}", savedSupplier.getId());

            return supplierMapper.toResponse(savedSupplier);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating supplier: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating supplier with id {}: ", id, e);
            throw new RuntimeException("Failed to update supplier", e);
        }
    }

    public void deleteSupplier(Long id) {
        try {
            Supplier supplier = supplierRepository.findById(id)
                    .filter(s -> s.getIsActive())
                    .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));

            supplier.setIsActive(false);
            supplierRepository.save(supplier);
            logger.info("Supplier deleted successfully with id: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting supplier with id {}: ", id, e);
            throw new RuntimeException("Failed to delete supplier", e);
        }
    }
}