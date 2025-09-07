package org.example.inventorymanagementbackend.controller;

import org.example.inventorymanagementbackend.dto.request.SaleRequest;
import org.example.inventorymanagementbackend.dto.response.ApiResponse;
import org.example.inventorymanagementbackend.dto.response.SaleResponse;
import org.example.inventorymanagementbackend.service.SaleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SaleController {

    private static final Logger logger = LoggerFactory.getLogger(SaleController.class);

    @Autowired
    private SaleService saleService;




                @GetMapping
                public ResponseEntity<ApiResponse<List<SaleResponse>>> getAllSales() {
                    try {
                        List<SaleResponse> sales = saleService.getAllSales();
                        return ResponseEntity.ok(ApiResponse.success(sales));
                    } catch (Exception e) {
                        logger.error("Error fetching sales", e);
                        return ResponseEntity.internalServerError()
                                .body(ApiResponse.error("Failed to fetch sales"));
                    }
                }

                @GetMapping("/{id}")
                public ResponseEntity<ApiResponse<SaleResponse>> getSaleById(@PathVariable Long id) {
                    try {
                        SaleResponse sale = saleService.getSaleById(id);
                        return ResponseEntity.ok(ApiResponse.success(sale));
                    } catch (RuntimeException e) {
                        return ResponseEntity.notFound().build();
                    } catch (Exception e) {
                        logger.error("Error fetching sale with id: {}", id, e);
                        return ResponseEntity.internalServerError()
                                .body(ApiResponse.error("Failed to fetch sale"));
                    }
                }

                @PostMapping
                public ResponseEntity<ApiResponse<SaleResponse>> createSale(@Valid @RequestBody SaleRequest request) {
                    try {
                        SaleResponse sale = saleService.createSale(request);
                        logger.info("Sale created successfully with id: {}", sale.getId());
                        return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Sale created successfully", sale));
                    } catch (IllegalArgumentException e) {
                        logger.error("Invalid sale data: {}", e.getMessage());
                        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
                    } catch (Exception e) {
                        logger.error("Error creating sale", e);
                        return ResponseEntity.internalServerError()
                                .body(ApiResponse.error("Failed to create sale"));
                    }
                }

                @DeleteMapping("/{id}")
                public ResponseEntity<ApiResponse<String>> deleteSale(@PathVariable Long id) {
                    try {
                        saleService.deleteSale(id);
                        return ResponseEntity.ok(ApiResponse.success("Sale deleted successfully", null));
                    } catch (RuntimeException e) {
                        return ResponseEntity.notFound().build();
                    } catch (Exception e) {
                        logger.error("Error deleting sale with id: {}", id, e);
                        return ResponseEntity.internalServerError()
                                .body(ApiResponse.error("Failed to delete sale"));
                    }
                }

                @GetMapping("/unpaid")
                public ResponseEntity<ApiResponse<List<SaleResponse>>> getUnpaidSales() {
                    try {
                        List<SaleResponse> sales = saleService.getUnpaidSales();
                        return ResponseEntity.ok(ApiResponse.success(sales));
                    } catch (Exception e) {
                        logger.error("Error fetching unpaid sales", e);
                        return ResponseEntity.internalServerError()
                                .body(ApiResponse.error("Failed to fetch unpaid sales"));
                    }
                }

                @PutMapping("/{id}/mark-paid")
                public ResponseEntity<ApiResponse<SaleResponse>> markSaleAsPaid(@PathVariable Long id) {
                    try {
                        SaleResponse sale = saleService.markSaleAsPaid(id);
                        return ResponseEntity.ok(ApiResponse.success("Sale marked as paid", sale));
                    } catch (RuntimeException e) {
                        return ResponseEntity.notFound().build();
                    } catch (Exception e) {
                        logger.error("Error marking sale as paid with id: {}", id, e);
                        return ResponseEntity.internalServerError()
                                .body(ApiResponse.error("Failed to mark sale as paid"));
                    }
                }
            }