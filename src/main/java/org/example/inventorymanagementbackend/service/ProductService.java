package org.example.inventorymanagementbackend.service;



import org.example.inventorymanagementbackend.dto.request.ProductRequest;
import org.example.inventorymanagementbackend.dto.response.ProductResponse;
import org.example.inventorymanagementbackend.entity.Product;
import org.example.inventorymanagementbackend.mapper.ProductMapper;
import org.example.inventorymanagementbackend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service
 * Handles business logic for product operations
 */
@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        logger.debug("Fetching all products");
        List<Product> products = productRepository.findByIsActiveTrueOrderByNameAsc();
        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        logger.debug("Fetching product with id: {}", id);
        Product product = productRepository.findById(id)
                .filter(p -> p.getIsActive())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    /**
     * Get product by code
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductByCode(String code) {
        logger.debug("Fetching product with code: {}", code);
        Product product = productRepository.findByCodeIgnoreCase(code)
                .filter(p -> p.getIsActive())
                .orElseThrow(() -> new RuntimeException("Product not found with code: " + code));
        return productMapper.toResponse(product);
    }

    /**
     * Search products by code or name
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String searchTerm) {
        logger.debug("Searching products with term: {}", searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts();
        }

        List<Product> products = productRepository.searchByCodeOrName(searchTerm.trim());
        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get low stock products
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        logger.debug("Fetching low stock products");
        List<Product> products = productRepository.findLowStockProducts();
        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create new product
     */
    public ProductResponse createProduct(ProductRequest request) {
        logger.debug("Creating product with code: {}", request.getCode());

        // Validate unique code
        if (productRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new IllegalArgumentException("Product code already exists: " + request.getCode());
        }

        Product product = productMapper.toEntity(request);
        product.setIsActive(true);
        product.setCurrentStock(0); // New products start with 0 stock

        Product savedProduct = productRepository.save(product);
        logger.info("Product created successfully with id: {} and code: {}",
                savedProduct.getId(), savedProduct.getCode());

        return productMapper.toResponse(savedProduct);
    }

    /**
     * Update product
     */
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        logger.debug("Updating product with id: {}", id);

        Product existingProduct = productRepository.findById(id)
                .filter(p -> p.getIsActive())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Validate unique code (excluding current product)
        if (productRepository.existsByCodeIgnoreCaseAndIdNot(request.getCode(), id)) {
            throw new IllegalArgumentException("Product code already exists: " + request.getCode());
        }

        // Update fields
        existingProduct.setCode(request.getCode());
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setFixedPrice(request.getFixedPrice());
        existingProduct.setDiscount(request.getDiscount());
        existingProduct.setLowStockThreshold(request.getLowStockThreshold());

        Product savedProduct = productRepository.save(existingProduct);
        logger.info("Product updated successfully with id: {}", savedProduct.getId());

        return productMapper.toResponse(savedProduct);
    }

    /**
     * Delete product (soft delete)
     */
    public void deleteProduct(Long id) {
        logger.debug("Deleting product with id: {}", id);

        Product product = productRepository.findById(id)
                .filter(p -> p.getIsActive())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Soft delete
        product.setIsActive(false);
        productRepository.save(product);

        logger.info("Product deleted successfully with id: {}", id);
    }

    /**
     * Update product stock
     */
    public void updateProductStock(Long productId, Integer quantity, boolean isAddition) {
        logger.debug("Updating stock for product id: {}, quantity: {}, addition: {}",
                productId, quantity, isAddition);

        Product product = productRepository.findById(productId)
                .filter(p -> p.getIsActive())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        product.updateStock(quantity, isAddition);
        productRepository.save(product);

        logger.info("Stock updated for product id: {}, new stock: {}",
                productId, product.getCurrentStock());
    }

    /**
     * Check if product has sufficient stock
     */
    @Transactional(readOnly = true)
    public boolean hasProductSufficientStock(Long productId, Integer requiredQuantity) {
        Product product = productRepository.findById(productId)
                .filter(p -> p.getIsActive())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        return product.hasSufficientStock(requiredQuantity);
    }

    /**
     * Get product entity by ID (for internal use)
     */
    @Transactional(readOnly = true)
    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .filter(p -> p.getIsActive())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
}
