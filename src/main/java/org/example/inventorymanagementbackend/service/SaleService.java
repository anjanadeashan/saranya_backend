package org.example.inventorymanagementbackend.service;

import org.example.inventorymanagementbackend.dto.request.SaleRequest;
import org.example.inventorymanagementbackend.dto.request.SaleItemRequest;
import org.example.inventorymanagementbackend.dto.response.SaleResponse;
import org.example.inventorymanagementbackend.entity.*;
import org.example.inventorymanagementbackend.mapper.SaleMapper;
import org.example.inventorymanagementbackend.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SaleService {

    private static final Logger logger = LoggerFactory.getLogger(SaleService.class);

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleMapper saleMapper;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<SaleResponse> getAllSales() {
        List<Sale> sales = saleRepository.findAllOrderBySaleDateDesc();
        return sales.stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));
        return saleMapper.toResponse(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getUnpaidSales() {
        List<Sale> sales = saleRepository.findByIsPaidOrderBySaleDateDesc(false);
        return sales.stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    public SaleResponse createSale(SaleRequest request) {
        logger.debug("Creating sale for customer: {}", request.getCustomerId());

        // Validate customer
        Customer customer = customerService.getCustomerEntityById(request.getCustomerId());

        // Validate sale items
        if (request.getSaleItems() == null || request.getSaleItems().isEmpty()) {
            throw new IllegalArgumentException("Sale must have at least one item");
        }

        // Validate stock availability for all items
        for (SaleItemRequest itemRequest : request.getSaleItems()) {
            if (!productService.hasProductSufficientStock(itemRequest.getProductId(), itemRequest.getQuantity())) {
                Product product = productService.getProductEntityById(itemRequest.getProductId());
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getCurrentStock() +
                        ", Requested: " + itemRequest.getQuantity());
            }
        }

        // Create sale entity
        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setSaleDate(LocalDateTime.now());
        sale.setCheckNumber(request.getCheckNumber());
        sale.setBankName(request.getBankName());
        sale.setCheckDate(request.getCheckDate());
        sale.setNotes(request.getNotes());

        // Set payment status based on payment method
        if (request.getPaymentMethod().isPaidImmediately()) {
            sale.setIsPaid(true);
        } else {
            sale.setIsPaid(false);
        }

        // Create sale items
        List<SaleItem> saleItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SaleItemRequest itemRequest : request.getSaleItems()) {
            Product product = productService.getProductEntityById(itemRequest.getProductId());

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(itemRequest.getQuantity());
            saleItem.setUnitPrice(itemRequest.getUnitPrice());
            saleItem.setDiscount(itemRequest.getDiscount() != null ? itemRequest.getDiscount() : BigDecimal.ZERO);
            saleItem.updateLineTotal();

            saleItems.add(saleItem);
            totalAmount = totalAmount.add(saleItem.getLineTotal());
        }

        sale.setSaleItems(saleItems);
        sale.setTotalAmount(totalAmount);

        // Save sale
        Sale savedSale = saleRepository.save(sale);

        // Update inventory for non-check payments or process inventory immediately
        // For check payments, inventory is updated when payment is marked as paid
        if (sale.getPaymentMethod() != Sale.PaymentMethod.CREDIT_CHECK) {
            updateInventoryForSale(savedSale);
        }

        // Update customer outstanding balance for unpaid sales
        if (!sale.getIsPaid()) {
            customer.addToOutstandingBalance(totalAmount);
            // Note: Customer will be saved automatically due to transaction
        }

        logger.info("Sale created successfully with id: {}", savedSale.getId());
        return saleMapper.toResponse(savedSale);
    }

    public SaleResponse markSaleAsPaid(Long saleId) {
        logger.debug("Marking sale as paid: {}", saleId);

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + saleId));

        if (sale.getIsPaid()) {
            throw new IllegalArgumentException("Sale is already marked as paid");
        }

        sale.markAsPaid();

        // Update inventory if it's a check payment (inventory is updated when check is cleared)
        if (sale.getPaymentMethod() == Sale.PaymentMethod.CREDIT_CHECK) {
            updateInventoryForSale(sale);
        }

        // Update customer outstanding balance
        Customer customer = sale.getCustomer();
        customer.reduceOutstandingBalance(sale.getTotalAmount());

        Sale savedSale = saleRepository.save(sale);
        logger.info("Sale marked as paid successfully with id: {}", saleId);

        return saleMapper.toResponse(savedSale);
    }

    public void deleteSale(Long id) {
        logger.debug("Deleting sale with id: {}", id);

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));

        // Reverse inventory movements if sale was processed
        if (sale.getIsPaid() || sale.getPaymentMethod() != Sale.PaymentMethod.CREDIT_CHECK) {
            reverseInventoryForSale(sale);
        }

        // Update customer outstanding balance
        if (!sale.getIsPaid()) {
            Customer customer = sale.getCustomer();
            customer.reduceOutstandingBalance(sale.getTotalAmount());
        }

        saleRepository.delete(sale);
        logger.info("Sale deleted successfully with id: {}", id);
    }

    private void updateInventoryForSale(Sale sale) {
        logger.debug("Updating inventory for sale: {}", sale.getId());

        for (SaleItem saleItem : sale.getSaleItems()) {
            // Create inventory OUT movement
            productService.updateProductStock(
                    saleItem.getProduct().getId(),
                    saleItem.getQuantity(),
                    false // false = OUT movement
            );
        }
    }

    private void reverseInventoryForSale(Sale sale) {
        logger.debug("Reversing inventory for sale: {}", sale.getId());

        for (SaleItem saleItem : sale.getSaleItems()) {
            // Reverse by adding stock back
            productService.updateProductStock(
                    saleItem.getProduct().getId(),
                    saleItem.getQuantity(),
                    true // true = IN movement (reversing the OUT)
            );
        }
    }
}