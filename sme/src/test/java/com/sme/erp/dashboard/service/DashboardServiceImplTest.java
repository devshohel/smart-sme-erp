package com.sme.erp.dashboard.service;

import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO;
import com.sme.erp.dashboard.service.impl.DashboardServiceImpl;
import com.sme.erp.inventory.entity.Stock;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.repository.StockRepository;
import com.sme.erp.product.entity.Product;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.entity.SalesItem;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.enums.SalesPaymentStatus;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesItemRepository;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private SalesInvoiceRepository salesInvoiceRepository;
    @Mock private SalesItemRepository salesItemRepository;
    @Mock private PurchaseOrderRepository purchaseOrderRepository;
    @Mock private StockRepository stockRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private SupplierRepository supplierRepository;

    private DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DashboardServiceImpl(
                salesInvoiceRepository,
                salesItemRepository,
                purchaseOrderRepository,
                stockRepository,
                customerRepository,
                supplierRepository);
    }

    @Test
    void getSummary_calculatesCoreDashboardTotalsFromStableModules() {
        SalesInvoice invoice = salesInvoice("INV-001", "Customer A", new BigDecimal("100.00"), new BigDecimal("25.00"));
        PurchaseOrder purchase = purchaseOrder("PO-001", "Supplier A", new BigDecimal("40.00"), new BigDecimal("10.00"));
        Stock stock = stock("Product A", "Main Warehouse", new BigDecimal("3.00"), new BigDecimal("7.50"), 5);
        SalesItem item = salesItem(invoice, stock.getProduct(), new BigDecimal("2.00"), new BigDecimal("100.00"));

        when(salesInvoiceRepository.findAll()).thenReturn(List.of(invoice));
        when(purchaseOrderRepository.findAll()).thenReturn(List.of(purchase));
        when(stockRepository.findAll()).thenReturn(List.of(stock));
        when(salesItemRepository.findAll()).thenReturn(List.of(item));
        when(customerRepository.count()).thenReturn(1L);
        when(supplierRepository.count()).thenReturn(1L);

        DashboardSummaryDTO summary = service.getSummary();

        assertThat(summary.getTodaySales()).isEqualByComparingTo("100.00");
        assertThat(summary.getTodayPurchase()).isEqualByComparingTo("40.00");
        assertThat(summary.getTodayExpense()).isEqualByComparingTo("0.00");
        assertThat(summary.getTodayProfit()).isEqualByComparingTo("60.00");
        assertThat(summary.getTotalStockValue()).isEqualByComparingTo("22.50");
        assertThat(summary.getCustomerDue()).isEqualByComparingTo("25.00");
        assertThat(summary.getSupplierDue()).isEqualByComparingTo("10.00");
        assertThat(summary.getNetProfit()).isEqualByComparingTo("60.00");
        assertThat(summary.getTotalCustomers()).isEqualTo(1L);
        assertThat(summary.getTotalSuppliers()).isEqualTo(1L);
        assertThat(summary.getLowStockItemsCount()).isEqualTo(1L);
        assertThat(summary.getLowStockAlerts()).hasSize(1);
        assertThat(summary.getDueAlerts()).hasSize(2);
        assertThat(summary.getRecentTransactions()).hasSize(2);
        assertThat(summary.getTopSellingProducts()).hasSize(1);
        assertThat(summary.getTopSellingProducts().get(0).getAmount()).isEqualByComparingTo("100.00");
    }

    private SalesInvoice salesInvoice(String invoiceNo, String customerName, BigDecimal netTotal, BigDecimal dueAmount) {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName(customerName);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(10L);
        invoice.setInvoiceNo(invoiceNo);
        invoice.setCustomer(customer);
        invoice.setSaleDate(LocalDate.now().atStartOfDay());
        invoice.setNetTotal(netTotal);
        invoice.setDueAmount(dueAmount);
        invoice.setPaymentStatus(SalesPaymentStatus.PARTIAL);
        invoice.setStatus(SalesInvoiceStatus.CONFIRMED);
        return invoice;
    }

    private PurchaseOrder purchaseOrder(String purchaseCode, String supplierName, BigDecimal netTotal, BigDecimal dueAmount) {
        Supplier supplier = new Supplier();
        supplier.setId(2L);
        supplier.setName(supplierName);

        PurchaseOrder order = new PurchaseOrder();
        order.setId(20L);
        order.setPurchaseCode(purchaseCode);
        order.setSupplier(supplier);
        order.setPurchaseDate(LocalDateTime.now());
        order.setNetTotal(netTotal);
        order.setDueAmount(dueAmount);
        order.setStatus(PurchaseStatus.RECEIVED);
        return order;
    }

    private Stock stock(
            String productName,
            String warehouseName,
            BigDecimal quantity,
            BigDecimal purchasePrice,
            int reorderLevel) {
        Product product = new Product();
        product.setId(3L);
        product.setProductName(productName);
        product.setPurchasePrice(purchasePrice);
        product.setReorderLevel(reorderLevel);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(4L);
        warehouse.setName(warehouseName);

        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setWarehouse(warehouse);
        stock.setQuantity(quantity);
        return stock;
    }

    private SalesItem salesItem(SalesInvoice invoice, Product product, BigDecimal quantity, BigDecimal subTotal) {
        SalesItem item = new SalesItem();
        item.setInvoice(invoice);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setSubTotal(subTotal);
        return item;
    }
}
