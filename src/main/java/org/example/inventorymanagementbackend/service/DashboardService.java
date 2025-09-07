package org.example.inventorymanagementbackend.service;

import org.example.inventorymanagementbackend.dto.response.*;
import org.example.inventorymanagementbackend.entity.Sale;
import org.example.inventorymanagementbackend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleService saleService;

    public DashboardSummaryResponse getDashboardSummary() {
        logger.debug("Generating dashboard summary");

        DashboardSummaryResponse summary = new DashboardSummaryResponse();

        // Basic counts
        summary.setTotalProducts(productRepository.countActiveProducts());
        summary.setTotalCustomers(customerRepository.countActiveCustomers());
        summary.setTotalSuppliers(supplierRepository.countActiveSuppliers());

        // Stock value
        summary.setTotalStockValue(productRepository.getTotalStockValue());

        // Issues (low stock products)
        summary.setTotalIssues(productRepository.countLowStockProducts());

        // Unpaid sales amount
        summary.setUnpaidSalesAmount(saleRepository.getTotalUnpaidAmount());

        // Total sales count
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        summary.setTotalSales(saleRepository.countSalesBetween(startOfMonth, LocalDateTime.now()));

        return summary;
    }

    public List<ProductResponse> getLowStockAlerts() {
        logger.debug("Fetching low stock alerts");
        return productService.getLowStockProducts();
    }

    public List<CheckReminderResponse> getCheckReminders() {
        logger.debug("Fetching check reminders");

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysFromNow = today.plusDays(7);

        List<Sale> checkSales = saleRepository.findCheckSalesDueBetween(today.minusDays(30), sevenDaysFromNow);

        return checkSales.stream()
                .map(this::mapToCheckReminder)
                .collect(Collectors.toList());
    }

    public List<TopSellingProductResponse> getTopSellingProducts() {
        logger.debug("Fetching top selling products");

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now();

        List<Object[]> results = saleItemRepository.findTopSellingProductsBetween(startOfMonth, endOfMonth);

        return results.stream()
                .limit(10) // Top 10 products
                .map(result -> new TopSellingProductResponse(
                        (Long) result[0],     // productId
                        (String) result[1],   // productCode
                        (String) result[2],   // productName
                        (Long) result[3]      // totalQuantity
                ))
                .collect(Collectors.toList());
    }

    public List<SalesVsPurchasesResponse> getSalesVsPurchasesData() {
        logger.debug("Fetching sales vs purchases data");

        List<SalesVsPurchasesResponse> data = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime startDate = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endDate = startDate.plusMonths(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);

            String monthName = startDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) +
                    " " + startDate.getYear();

            BigDecimal salesAmount = saleRepository.getTotalSalesAmountBetween(startDate, endDate);

            // For purchases, we could track inventory IN movements with costs
            // For now, using a simple calculation based on stock movements
            BigDecimal purchasesAmount = calculatePurchasesForPeriod(startDate, endDate);

            data.add(new SalesVsPurchasesResponse(monthName, salesAmount, purchasesAmount));
        }

        return data;
    }

    private CheckReminderResponse mapToCheckReminder(Sale sale) {
        CheckReminderResponse reminder = new CheckReminderResponse();
        reminder.setId(sale.getId());
        reminder.setCustomerId(sale.getCustomer().getId());
        reminder.setCustomerName(sale.getCustomer().getName());
        reminder.setTotalAmount(sale.getTotalAmount());
        reminder.setCheckNumber(sale.getCheckNumber());
        reminder.setBankName(sale.getBankName());
        reminder.setCheckDate(sale.getCheckDate());
        reminder.setIsOverdue(sale.isCheckOverdue());
        reminder.setIsDueSoon(sale.isCheckDueSoon());
        return reminder;
    }

    private BigDecimal calculatePurchasesForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        // This is a simplified calculation
        // In a real system, you might track purchase orders and their costs
        // For now, we'll estimate based on inventory movements and average product costs

        // You could implement actual purchase tracking here
        // For demonstration, returning a calculated estimate
        BigDecimal salesAmount = saleRepository.getTotalSalesAmountBetween(startDate, endDate);

        // Estimate purchases as 70% of sales (simplified business logic)
        return salesAmount.multiply(BigDecimal.valueOf(0.7));
    }
}
