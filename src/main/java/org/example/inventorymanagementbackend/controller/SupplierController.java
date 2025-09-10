package org.example.inventorymanagementbackend.controller;

import java.util.List;

import org.example.inventorymanagementbackend.dto.request.SupplierRequest;
import org.example.inventorymanagementbackend.dto.response.ApiResponse;
import org.example.inventorymanagementbackend.dto.response.SupplierResponse;
import org.example.inventorymanagementbackend.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAllSuppliers() {
        try {
            List<SupplierResponse> suppliers = supplierService.getAllSuppliers();
            return ResponseEntity.ok(ApiResponse.success(suppliers));
        } catch (Exception e) {
            logger.error("Error fetching suppliers", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch suppliers"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> getSupplierById(@PathVariable Long id) {
        try {
            SupplierResponse supplier = supplierService.getSupplierById(id);
            return ResponseEntity.ok(ApiResponse.success(supplier));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching supplier with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch supplier"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(@Valid @RequestBody SupplierRequest request) {
        try {
            SupplierResponse supplier = supplierService.createSupplier(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Supplier created successfully", supplier));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating supplier", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create supplier"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(@PathVariable Long id,
                                                                        @Valid @RequestBody SupplierRequest request) {
        try {
            SupplierResponse supplier = supplierService.updateSupplier(id, request);
            return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", supplier));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating supplier with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update supplier"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSupplier(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok(ApiResponse.success("Supplier deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting supplier with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete supplier"));
        }
    }
}