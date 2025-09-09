package org.example.inventorymanagementbackend.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.example.inventorymanagementbackend.entity.Customer;
import org.example.inventorymanagementbackend.entity.Inventory;
import org.example.inventorymanagementbackend.entity.Product;
import org.example.inventorymanagementbackend.entity.Sale;
import org.example.inventorymanagementbackend.entity.SaleItem;
import org.example.inventorymanagementbackend.entity.Supplier;
import org.example.inventorymanagementbackend.repository.CustomerRepository;
import org.example.inventorymanagementbackend.repository.InventoryRepository;
import org.example.inventorymanagementbackend.repository.ProductRepository;
import org.example.inventorymanagementbackend.repository.SaleRepository;
import org.example.inventorymanagementbackend.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

/**
 * Data Loader
 * Sample data loading is DISABLED
 * To re-enable: uncomment @Component annotation and the code in run() method
 */
// @Component  // <-- COMMENTED OUT TO DISABLE SAMPLE DATA LOADING
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("DataLoader is disabled - no sample data will be loaded. Database will remain empty.");
        
        /* 
         * SAMPLE DATA LOADING CODE - COMMENTED OUT
         * To re-enable sample data loading:
         * 1. Uncomment the @Component annotation above
         * 2. Uncomment this entire code block below
         */
        /*
        if (productRepository.count() == 0) {
            // Set system property to allow past check dates during data loading
            System.setProperty("spring.data.loading", "true");

            try {
                logger.info("Loading sample data...");
                loadSampleData();
                logger.info("Sample data loaded successfully!");
            } finally {
                // Clear the system property after data loading
                System.clearProperty("spring.data.loading");
            }
        } else {
            logger.info("Sample data already exists, skipping data loading.");
        }
        */
    }

    private void loadSampleData() {
        // Create Suppliers
        Supplier supplier1 = createSupplier("SUP001", "TechCorp Supplies", "John Smith",
                "john@techcorp.com", "+1234567890",
                "123 Tech Street", "San Francisco", "USA");

        Supplier supplier2 = createSupplier("SUP002", "Global Electronics", "Mary Johnson",
                "mary@globalelec.com", "+1987654321",
                "456 Electronics Ave", "New York", "USA");

        Supplier supplier3 = createSupplier("SUP003", "Office Plus", "David Wilson",
                "david@officeplus.com", "+1122334455",
                "789 Office Blvd", "Chicago", "USA");

        // Create Customers
        Customer customer1 = createCustomer("Alice Brown", "alice@example.com", "+1555666777",
                "321 Customer Lane", "Los Angeles", "USA",
                new BigDecimal("5000.00"), new BigDecimal("1200.00"));

        Customer customer2 = createCustomer("Bob Davis", "bob@example.com", "+1999888777",
                "654 Client Street", "Miami", "USA",
                new BigDecimal("3000.00"), new BigDecimal("500.00"));

        Customer customer3 = createCustomer("Carol White", "carol@example.com", "+1777555333",
                "987 Buyer Road", "Seattle", "USA",
                new BigDecimal("10000.00"), new BigDecimal("0.00"));

        // Create Products
        Product product1 = createProduct("LAPTOP001", "Gaming Laptop",
                "High-performance gaming laptop with RTX graphics",
                new BigDecimal("1299.99"), new BigDecimal("5.0"), 15, 5);

        Product product2 = createProduct("MOUSE001", "Wireless Gaming Mouse",
                "Ergonomic wireless gaming mouse with RGB lighting",
                new BigDecimal("79.99"), new BigDecimal("10.0"), 50, 10);

        Product product3 = createProduct("KEYBOARD001", "Mechanical Keyboard",
                "Cherry MX mechanical keyboard with backlight",
                new BigDecimal("159.99"), new BigDecimal("0.0"), 25, 8);

        Product product4 = createProduct("MONITOR001", "4K Gaming Monitor",
                "27-inch 4K gaming monitor with 144Hz refresh rate",
                new BigDecimal("599.99"), new BigDecimal("8.0"), 12, 3);

        Product product5 = createProduct("HEADSET001", "Gaming Headset",
                "Surround sound gaming headset with noise cancellation",
                new BigDecimal("199.99"), new BigDecimal("15.0"), 30, 5);

        Product product6 = createProduct("WEBCAM001", "HD Webcam",
                "1080p HD webcam for streaming and video calls",
                new BigDecimal("89.99"), new BigDecimal("5.0"), 8, 15); // Low stock

        Product product7 = createProduct("SPEAKER001", "Bluetooth Speaker",
                "Portable Bluetooth speaker with bass boost",
                new BigDecimal("129.99"), new BigDecimal("12.0"), 40, 10);

        Product product8 = createProduct("TABLET001", "10-inch Tablet",
                "Android tablet with 128GB storage",
                new BigDecimal("299.99"), new BigDecimal("7.0"), 20, 5);

        // Create Inventory Movements (Stock IN)
        createInventoryMovement(product1, 20, Inventory.MovementType.IN, supplier1, "PO-2024-001");
        createInventoryMovement(product2, 60, Inventory.MovementType.IN, supplier2, "PO-2024-002");
        createInventoryMovement(product3, 35, Inventory.MovementType.IN, supplier1, "PO-2024-003");
        createInventoryMovement(product4, 15, Inventory.MovementType.IN, supplier2, "PO-2024-004");
        createInventoryMovement(product5, 40, Inventory.MovementType.IN, supplier3, "PO-2024-005");
        createInventoryMovement(product6, 25, Inventory.MovementType.IN, supplier2, "PO-2024-006");
        createInventoryMovement(product7, 50, Inventory.MovementType.IN, supplier3, "PO-2024-007");
        createInventoryMovement(product8, 30, Inventory.MovementType.IN, supplier1, "PO-2024-008");

        // Create some stock OUT movements (simulating previous sales)
        createInventoryMovement(product1, 5, Inventory.MovementType.OUT, null, "SALE-001");
        createInventoryMovement(product2, 10, Inventory.MovementType.OUT, null, "SALE-002");
        createInventoryMovement(product3, 10, Inventory.MovementType.OUT, null, "SALE-003");
        createInventoryMovement(product4, 3, Inventory.MovementType.OUT, null, "SALE-004");
        createInventoryMovement(product5, 10, Inventory.MovementType.OUT, null, "SALE-005");
        createInventoryMovement(product6, 17, Inventory.MovementType.OUT, null, "SALE-006"); // Makes it low stock
        createInventoryMovement(product7, 10, Inventory.MovementType.OUT, null, "SALE-007");
        createInventoryMovement(product8, 10, Inventory.MovementType.OUT, null, "SALE-008");

        // Create Sample Sales with different payment methods
        createSampleSale(customer1, Sale.PaymentMethod.CASH, Arrays.asList(
                new SaleItemData(product1, 1, product1.getFixedPrice(), product1.getDiscount()),
                new SaleItemData(product2, 2, product2.getFixedPrice(), product2.getDiscount())
        ), "Completed cash sale");

        createSampleSale(customer2, Sale.PaymentMethod.CREDIT_CARD, Arrays.asList(
                new SaleItemData(product3, 1, product3.getFixedPrice(), product3.getDiscount()),
                new SaleItemData(product5, 1, product5.getFixedPrice(), product5.getDiscount())
        ), "Credit card payment");

        // Create a CHECK payment sale (future date - unpaid)
        createSampleCheckSale(customer1, Arrays.asList(
                new SaleItemData(product4, 1, product4.getFixedPrice(), product4.getDiscount())
        ), "CHK001", "First National Bank", LocalDate.now().plusDays(30), "Check payment - due in 30 days");

        // Create another CHECK payment (due soon)
        createSampleCheckSale(customer2, Arrays.asList(
                new SaleItemData(product7, 1, product7.getFixedPrice(), product7.getDiscount())
        ), "CHK002", "City Bank", LocalDate.now().plusDays(5), "Check payment due soon");

        // Create a paid CHECK payment (past sale with future check date)
        createPaidCheckSale(customer3, Arrays.asList(
                new SaleItemData(product8, 1, product8.getFixedPrice(), product8.getDiscount())
        ), "CHK003", "Regional Bank", LocalDate.now().plusDays(15), "Paid check payment");

        logger.info("Sample data creation completed");
    }

    private Supplier createSupplier(String code, String name, String contactPerson,
                                    String email, String phone, String address,
                                    String city, String country) {
        Supplier supplier = new Supplier();
        supplier.setUniqueSupplierCode(code);
        supplier.setName(name);
        supplier.setContactPerson(contactPerson);
        supplier.setEmail(email);
        supplier.setPhone(phone);
        supplier.setAddress(address);
        supplier.setCity(city);
        supplier.setCountry(country);
        supplier.setIsActive(true);
        return supplierRepository.save(supplier);
    }

    private Customer createCustomer(String name, String email, String phone,
                                    String address, String city, String country,
                                    BigDecimal creditLimit, BigDecimal outstandingBalance) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);
        customer.setCity(city);
        customer.setCountry(country);
        customer.setCreditLimit(creditLimit);
        customer.setOutstandingBalance(outstandingBalance);
        customer.setIsActive(true);
        return customerRepository.save(customer);
    }

    private Product createProduct(String code, String name, String description,
                                  BigDecimal price, BigDecimal discount,
                                  Integer currentStock, Integer lowStockThreshold) {
        Product product = new Product();
        product.setCode(code);
        product.setName(name);
        product.setDescription(description);
        product.setFixedPrice(price);
        product.setDiscount(discount);
        product.setCurrentStock(currentStock);
        product.setLowStockThreshold(lowStockThreshold);
        product.setIsActive(true);
        return productRepository.save(product);
    }

    private void createInventoryMovement(Product product, Integer quantity,
                                         Inventory.MovementType movementType,
                                         Supplier supplier, String reference) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(quantity);
        inventory.setMovementType(movementType);
        inventory.setSupplier(supplier);
        inventory.setDate(LocalDateTime.now().minusDays((long) (Math.random() * 30)));
        inventory.setReference(reference);
        inventoryRepository.save(inventory);
    }

    private void createSampleSale(Customer customer, Sale.PaymentMethod paymentMethod,
                                  java.util.List<SaleItemData> items, String notes) {
        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setPaymentMethod(paymentMethod);
        sale.setSaleDate(LocalDateTime.now().minusDays((long) (Math.random() * 10)));
        sale.setPaid(paymentMethod.isPaidImmediately());
        sale.setNotes(notes);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleItemData itemData : items) {
            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(itemData.product);
            saleItem.setQuantity(itemData.quantity);
            saleItem.setUnitPrice(itemData.unitPrice);
            saleItem.setDiscount(itemData.discount);
            saleItem.updateLineTotal();

            sale.getSaleItems().add(saleItem);
            totalAmount = totalAmount.add(saleItem.getLineTotal());
        }

        sale.setTotalAmount(totalAmount);
        saleRepository.save(sale);
    }

    private void createSampleCheckSale(Customer customer, java.util.List<SaleItemData> items,
                                       String checkNumber, String bankName, LocalDate checkDate,
                                       String notes) {
        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setPaymentMethod(Sale.PaymentMethod.CREDIT_CHECK);
        sale.setSaleDate(LocalDateTime.now().minusDays((long) (Math.random() * 5)));
        sale.setPaid(false); // Check payments start as unpaid
        sale.setCheckNumber(checkNumber);
        sale.setBankName(bankName);
        sale.setCheckDate(checkDate);
        sale.setNotes(notes);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleItemData itemData : items) {
            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(itemData.product);
            saleItem.setQuantity(itemData.quantity);
            saleItem.setUnitPrice(itemData.unitPrice);
            saleItem.setDiscount(itemData.discount);
            saleItem.updateLineTotal();

            sale.getSaleItems().add(saleItem);
            totalAmount = totalAmount.add(saleItem.getLineTotal());
        }

        sale.setTotalAmount(totalAmount);
        saleRepository.save(sale);
    }

    private void createPaidCheckSale(Customer customer, java.util.List<SaleItemData> items,
                                     String checkNumber, String bankName, LocalDate checkDate,
                                     String notes) {
        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setPaymentMethod(Sale.PaymentMethod.CREDIT_CHECK);
        sale.setSaleDate(LocalDateTime.now().minusDays((long) (Math.random() * 15)));
        sale.setPaid(true); // This one is already paid
        sale.setCheckNumber(checkNumber);
        sale.setBankName(bankName);
        sale.setCheckDate(checkDate);
        sale.setNotes(notes);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleItemData itemData : items) {
            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(itemData.product);
            saleItem.setQuantity(itemData.quantity);
            saleItem.setUnitPrice(itemData.unitPrice);
            saleItem.setDiscount(itemData.discount);
            saleItem.updateLineTotal();

            sale.getSaleItems().add(saleItem);
            totalAmount = totalAmount.add(saleItem.getLineTotal());
        }

        sale.setTotalAmount(totalAmount);
        saleRepository.save(sale);
    }

    // Helper class for sale item data
    private static class SaleItemData {
        Product product;
        Integer quantity;
        BigDecimal unitPrice;
        BigDecimal discount;

        SaleItemData(Product product, Integer quantity, BigDecimal unitPrice, BigDecimal discount) {
            this.product = product;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.discount = discount;
        }
    }
}