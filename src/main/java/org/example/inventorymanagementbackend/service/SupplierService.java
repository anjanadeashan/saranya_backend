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
        List<Supplier> suppliers = supplierRepository.findByIsActiveTrueOrderByNameAsc();
        return suppliers.stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
        return supplierMapper.toResponse(supplier);
    }

    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByUniqueSupplierCodeIgnoreCase(request.getUniqueSupplierCode())) {
            throw new IllegalArgumentException("Supplier code already exists: " + request.getUniqueSupplierCode());
        }

        Supplier supplier = supplierMapper.toEntity(request);
        supplier.setIsActive(true);

        Supplier savedSupplier = supplierRepository.save(supplier);
        logger.info("Supplier created successfully with id: {}", savedSupplier.getId());

        return supplierMapper.toResponse(savedSupplier);
    }

    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
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
        return supplierMapper.toResponse(savedSupplier);
    }

    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));

        supplier.setIsActive(false);
        supplierRepository.save(supplier);
        logger.info("Supplier deleted successfully with id: {}", id);
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierByCode(String code) {
        return supplierRepository.findByUniqueSupplierCodeIgnoreCase(code)
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> new RuntimeException("Supplier not found with code: " + code));
    }
}
