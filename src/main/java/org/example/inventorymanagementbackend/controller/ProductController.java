package org.example.inventorymanagementbackend.controller;

import org.example.inventorymanagementbackend.dto.request.ProductRequest;
import org.example.inventorymanagementbackend.dto.response.ApiResponse;
import org.example.inventorymanagementbackend.dto.response.ProductResponse;
import org.example.inventorymanagementbackend.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Product Controller
 * Handles all product-related operations
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    /**
     * Get all products
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        try {
            List<ProductResponse> products = productService.getAllProducts();
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            logger.error("Error fetching products", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch products"));
        }
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        try {
            ProductResponse product = productService.getProductById(id);
            return ResponseEntity.ok(ApiResponse.success(product));
        } catch (RuntimeException e) {
            logger.error("Product not found with id: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching product with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch product"));
        }
    }

    /**
     * Search products by code or name
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(@RequestParam String q) {
        try {
            List<ProductResponse> products = productService.searchProducts(q);
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            logger.error("Error searching products with query: {}", q, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to search products"));
        }
    }

    /**
     * Get low stock products
     */
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts() {
        try {
            List<ProductResponse> products = productService.getLowStockProducts();
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            logger.error("Error fetching low stock products", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch low stock products"));
        }
    }

    /**
     * Create new product
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse product = productService.createProduct(request);
            logger.info("Product created successfully with code: {}", request.getCode());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Product created successfully", product));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid product data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating product", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create product"));
        }
    }

    /**
     * Update product
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id,
                                                                      @Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse product = productService.updateProduct(id, request);
            logger.info("Product updated successfully with id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
        } catch (IllegalArgumentException e) {
            // ✅ පළමුව specific exception එක catch කරන්න
            logger.error("Invalid product data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            // ✅ පසුව general RuntimeException එක catch කරන්න
            logger.error("Product not found with id: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating product with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update product"));
        }
    }

    /**
     * Delete product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            logger.info("Product deleted successfully with id: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
        } catch (RuntimeException e) {
            logger.error("Product not found with id: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting product with id: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete product"));
        }
    }

    /**
     * Get product by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductByCode(@PathVariable String code) {
        try {
            ProductResponse product = productService.getProductByCode(code);
            return ResponseEntity.ok(ApiResponse.success(product));
        } catch (RuntimeException e) {
            logger.error("Product not found with code: {}", code, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching product with code: {}", code, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch product"));
        }
    }
}