package org.example.inventorymanagementbackend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
    /**
 * Create new product with manual mapping to ensure all fields are set
 */
public ProductResponse createProduct(ProductRequest request) {
    logger.debug("Creating product with code: {}", request.getCode());

    // Validate unique code
    if (productRepository.existsByCodeIgnoreCase(request.getCode())) {
        throw new IllegalArgumentException("Product code already exists: " + request.getCode());
    }

    // Manual mapping to ensure all fields are properly set
    Product product = new Product();
    product.setCode(request.getCode());
    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setFixedPrice(request.getFixedPrice());
    product.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
    product.setCurrentStock(request.getCurrentStock() != null ? request.getCurrentStock() : 0);
    product.setLowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 0);
    product.setIsActive(true);
    
    logger.debug("Product being created - currentStock: {}, lowStockThreshold: {}", 
            product.getCurrentStock(), product.getLowStockThreshold());

    Product savedProduct = productRepository.save(product);
    logger.info("Product created successfully with id: {} and code: {}, initial stock: {}, threshold: {}",
            savedProduct.getId(), savedProduct.getCode(), savedProduct.getCurrentStock(), savedProduct.getLowStockThreshold());

    return productMapper.toResponse(savedProduct);
}

    /**
     * Update product
     */
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

    // Update fields - INCLUDING currentStock
    existingProduct.setCode(request.getCode());
    existingProduct.setName(request.getName());
    existingProduct.setDescription(request.getDescription());
    existingProduct.setFixedPrice(request.getFixedPrice());
    existingProduct.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
    existingProduct.setLowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 0);
    
    // ✅ ADD THIS LINE - Update currentStock
    if (request.getCurrentStock() != null) {
        existingProduct.setCurrentStock(request.getCurrentStock());
        logger.debug("Updating product stock to: {}", request.getCurrentStock());
    }

    Product savedProduct = productRepository.save(existingProduct);
    logger.info("Product updated successfully with id: {}, new stock: {}", 
            savedProduct.getId(), savedProduct.getCurrentStock());

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

    /**
     * Save product entity (used by other services for FIFO inventory management)
     * This method is required by SaleService for updating product stock during FIFO processing
     */
    public Product saveProduct(Product product) {
        logger.debug("Saving product entity with id: {}", product.getId());
        Product savedProduct = productRepository.save(product);
        logger.debug("Product saved successfully with id: {}, current stock: {}", 
                savedProduct.getId(), savedProduct.getCurrentStock());
        return savedProduct;
    }

    /**
     * Get product entity by ID without active filter (for internal repository operations)
     */
    @Transactional(readOnly = true)
    public Product getProductEntityByIdUnfiltered(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Check if product exists by ID
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    /**
     * Get current stock for a product
     */
    @Transactional(readOnly = true)
    public Integer getCurrentStock(Long productId) {
        Product product = getProductEntityById(productId);
        return product.getCurrentStock();
    }

    /**
     * Update only the current stock of a product (optimized for frequent stock updates)
     */
    public void updateCurrentStock(Long productId, Integer newStock) {
        logger.debug("Updating current stock for product id: {} to: {}", productId, newStock);
        
        Product product = getProductEntityById(productId);
        product.setCurrentStock(newStock);
        
        productRepository.save(product);
        logger.debug("Current stock updated for product id: {}, new stock: {}", productId, newStock);
    }
    /**
 * Update only product stock without other fields (optimized for performance)
 */
public void updateProductStockOnly(Long productId, Integer newStock) {
    logger.debug("Updating only stock for product id: {} to: {}", productId, newStock);
    
    // Use a direct query for better performance
    productRepository.updateProductStockOnly(productId, newStock);
    logger.debug("Stock only updated for product id: {}", productId);
}
}