package com.sme.erp.sales.service;

import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.notification.service.NotificationService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.product.repository.UomRepository;
import com.sme.erp.sales.dto.SalesInvoiceDTO;
import com.sme.erp.sales.dto.SalesItemDTO;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.entity.SalesOrder;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.enums.SalesOrderStatus;
import com.sme.erp.sales.enums.SalesPaymentStatus;
import com.sme.erp.sales.mapper.SalesInvoiceMapper;
import com.sme.erp.sales.mapper.SalesItemMapper;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesOrderRepository;
import com.sme.erp.sales.repository.SalesReturnRepository;
import com.sme.erp.sales.service.impl.SalesInvoiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesInvoiceServiceImplTest {

    @Mock private SalesInvoiceRepository salesInvoiceRepository;
    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UomRepository uomRepository;
    @Mock private SalesReturnRepository salesReturnRepository;
    @Mock private StockService stockService;
    @Mock private ActivityLogService activityLogService;
    @Mock private AuditLogService auditLogService;
    @Mock private AccountingPostingService accountingPostingService;
    @Mock private NotificationService notificationService;

    private SalesInvoiceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SalesInvoiceServiceImpl(
                salesInvoiceRepository,
                salesOrderRepository,
                customerRepository,
                warehouseRepository,
                productRepository,
                uomRepository,
                new SalesInvoiceMapper(new SalesItemMapper()),
                salesReturnRepository,
                stockService,
                activityLogService,
                auditLogService,
                accountingPostingService,
                notificationService);
    }

    @Test
    void create_draftInvoiceDoesNotDeductStock() {
        SalesInvoiceDTO dto = invoiceDto(SalesInvoiceStatus.DRAFT);

        mockReferences();
        when(salesInvoiceRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(salesInvoiceRepository.existsByInvoiceNo("INV-0001")).thenReturn(false);
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(dto);

        verify(stockService, never()).stockOut(4L, 3L, new BigDecimal("2.00"), "SALES_INVOICE", "INV-0001");
    }

    @Test
    void create_persistsDraftStatusAndDuePaymentStatus() {
        SalesInvoiceDTO dto = invoiceDto(SalesInvoiceStatus.DRAFT);

        mockReferences();
        when(salesInvoiceRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(salesInvoiceRepository.existsByInvoiceNo("INV-0001")).thenReturn(false);
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalesInvoiceDTO result = service.create(dto);

        assertThat(result.getStatus()).isEqualTo(SalesInvoiceStatus.DRAFT);
        assertThat(result.getPaymentStatus()).isEqualTo(SalesPaymentStatus.DUE);
        assertThat(result.getDueAmount()).isEqualByComparingTo("12.00");
    }

    @Test
    void update_postedInvoiceIsRejected() {
        SalesInvoice existing = new SalesInvoice();
        existing.setId(8L);
        existing.setInvoiceNo("INV-0008");
        existing.setStatus(SalesInvoiceStatus.POSTED);

        SalesInvoiceDTO dto = invoiceDto(SalesInvoiceStatus.DRAFT);
        dto.setInvoiceNo("INV-0008");

        when(salesInvoiceRepository.findById(8L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(8L, dto))
                .hasMessage("Only draft sales invoices can be edited");

        verify(stockService, never()).stockOut(any(), any(), any());
        verify(stockService, never()).stockOut(any(), any(), any(), any(), any());
    }

    @Test
    void create_invoiceWithNonApprovedOrderIsRejected() {
        SalesInvoiceDTO dto = invoiceDto(SalesInvoiceStatus.DRAFT);
        dto.setOrderId(9L);

        SalesOrder order = new SalesOrder();
        order.setId(9L);
        order.setStatus(SalesOrderStatus.SUBMITTED);

        when(salesOrderRepository.findById(9L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.create(dto))
                .hasMessage("Only approved sales orders can be invoiced");
    }

    @Test
    void reverse_clearsPaymentStatusFromApiState() {
        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(12L);
        invoice.setInvoiceNo("INV-0012");
        invoice.setStatus(SalesInvoiceStatus.POSTED);
        invoice.setPaymentStatus(SalesPaymentStatus.DUE);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setDueAmount(new BigDecimal("100.00"));
        invoice.setNetTotal(new BigDecimal("100.00"));
        invoice.setCustomer(customer());
        invoice.setWarehouse(warehouse());
        invoice.setSaleDate(LocalDateTime.of(2026, 6, 6, 0, 0));
        invoice.setItems(List.of());

        when(salesInvoiceRepository.findById(12L)).thenReturn(Optional.of(invoice));
        when(salesReturnRepository.existsByInvoiceIdAndStatus(12L, com.sme.erp.sales.enums.SalesReturnStatus.POSTED)).thenReturn(false);
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalesInvoiceDTO result = service.reverse(12L, "Duplicate posting");

        assertThat(result.getStatus()).isEqualTo(SalesInvoiceStatus.REVERSED);
        assertThat(result.getPaymentStatus()).isNull();
        verify(accountingPostingService).reverseSalesInvoice(eq(invoice), eq("Duplicate posting"));
    }

    private SalesInvoiceDTO invoiceDto(SalesInvoiceStatus status) {
        SalesItemDTO item = new SalesItemDTO();
        item.setProductId(4L);
        item.setQuantity(new BigDecimal("2.00"));
        item.setUnitPrice(new BigDecimal("6.00"));
        item.setDiscount(BigDecimal.ZERO);
        item.setTax(BigDecimal.ZERO);

        SalesInvoiceDTO dto = new SalesInvoiceDTO();
        dto.setCustomerId(2L);
        dto.setWarehouseId(3L);
        dto.setSaleDate(LocalDateTime.of(2026, 6, 6, 0, 0));
        dto.setPaidAmount(BigDecimal.ZERO);
        dto.setStatus(status);
        dto.getItems().add(item);
        return dto;
    }

    private void mockReferences() {
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer()));
        when(warehouseRepository.findById(3L)).thenReturn(Optional.of(warehouse()));
        when(productRepository.findById(4L)).thenReturn(Optional.of(product()));
    }

    private Customer customer() {
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setName("Customer 2");
        return customer;
    }

    private Warehouse warehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(3L);
        warehouse.setName("Warehouse 3");
        return warehouse;
    }

    private Product product() {
        Product product = new Product();
        product.setId(4L);
        product.setProductName("Product 4");
        return product;
    }
}
