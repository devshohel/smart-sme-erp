package com.sme.erp.sales.service;

import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.repository.StockMovementRepository;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.sales.dto.SalesReturnDTO;
import com.sme.erp.sales.dto.SalesReturnItemDTO;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.entity.SalesItem;
import com.sme.erp.sales.entity.SalesReturn;
import com.sme.erp.sales.entity.SalesReturnItem;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.enums.SalesPaymentStatus;
import com.sme.erp.sales.enums.SalesReturnStatus;
import com.sme.erp.sales.enums.SalesReturnCondition;
import com.sme.erp.sales.mapper.SalesReturnItemMapper;
import com.sme.erp.sales.mapper.SalesReturnMapper;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesReturnRepository;
import com.sme.erp.sales.service.impl.SalesReturnServiceImpl;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesReturnServiceImplTest {

    @Mock private SalesReturnRepository salesReturnRepository;
    @Mock private SalesInvoiceRepository salesInvoiceRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private StockService stockService;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private ActivityLogService activityLogService;
    @Mock private AuditLogService auditLogService;
    @Mock private AccountingPostingService accountingPostingService;

    private SalesReturnServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SalesReturnServiceImpl(
                salesReturnRepository,
                salesInvoiceRepository,
                customerRepository,
                productRepository,
                new SalesReturnMapper(new SalesReturnItemMapper()),
                stockService,
                stockMovementRepository,
                activityLogService,
                auditLogService,
                accountingPostingService);
    }

    @Test
    void createDraftReturnDoesNotRestockItems() {
        Customer customer = customer();
        SalesReturnDTO dto = returnDto();

        when(salesInvoiceRepository.findById(9L)).thenReturn(Optional.of(invoice(customer)));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(salesReturnRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(salesReturnRepository.existsByReturnCode("SR-0001")).thenReturn(false);
        when(salesReturnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(dto);

        verify(stockService, org.mockito.Mockito.never()).stockIn(
                4L,
                3L,
                new BigDecimal("2.00"),
                new BigDecimal("6.00"),
                "SALES_RETURN",
                "SR-0001");
    }

    @Test
    void approve_validReturnRestocksAndPostsAccounting() {
        Customer customer = customer();
        SalesInvoice invoice = invoice(customer);
        SalesReturn entity = returnEntity(invoice, customer, 4L, "2.00", "12.00");

        when(salesReturnRepository.findById(15L)).thenReturn(Optional.of(entity));
        when(salesReturnRepository.findPostedByInvoiceIdExcluding(9L, 15L)).thenReturn(List.of());
        when(salesReturnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(salesInvoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.approve(15L);

        verify(stockService).stockIn(4L, 3L, new BigDecimal("2.00"), new BigDecimal("6.00"), "SALES_RETURN", "SR-0001");
        verify(accountingPostingService).postSalesReturn(entity);
        assertThat(entity.getStatus()).isEqualTo(SalesReturnStatus.APPROVED);
        assertThat(invoice.getDueAmount()).isEqualByComparingTo("88.00");
    }

    @Test
    void approve_damagedReturnDoesNotRestoreSellableStock() {
        Customer customer = customer();
        SalesInvoice invoice = invoice(customer);
        SalesReturn entity = returnEntity(invoice, customer, 4L, "1.00", "6.00");
        entity.getItems().get(0).setCondition(SalesReturnCondition.DAMAGED);
        entity.getItems().get(0).setRestock(false);

        when(salesReturnRepository.findById(15L)).thenReturn(Optional.of(entity));
        when(salesReturnRepository.findPostedByInvoiceIdExcluding(9L, 15L)).thenReturn(List.of());
        when(salesReturnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(salesInvoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.approve(15L);

        verify(stockService, never()).stockIn(any(), any(), any(), any(), any(), any());
        verify(accountingPostingService).postSalesReturn(entity);
        assertThat(invoice.getDueAmount()).isEqualByComparingTo("94.00");
    }

    @Test
    void approve_productNotInInvoiceIsRejectedBeforePosting() {
        Customer customer = customer();
        SalesInvoice invoice = invoice(customer);
        SalesReturn entity = returnEntity(invoice, customer, 99L, "1.00", "6.00");

        when(salesReturnRepository.findById(15L)).thenReturn(Optional.of(entity));
        when(salesReturnRepository.findPostedByInvoiceIdExcluding(9L, 15L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.approve(15L))
                .hasMessage("Returned product does not belong to the invoice");
        verify(stockService, never()).stockIn(any(), any(), any(), any(), any(), any());
        verify(accountingPostingService, never()).postSalesReturn(any());
    }

    @Test
    void approve_overSoldQuantityIsRejectedBeforePosting() {
        Customer customer = customer();
        SalesInvoice invoice = invoice(customer);
        SalesReturn entity = returnEntity(invoice, customer, 4L, "6.00", "36.00");

        when(salesReturnRepository.findById(15L)).thenReturn(Optional.of(entity));
        when(salesReturnRepository.findPostedByInvoiceIdExcluding(9L, 15L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.approve(15L))
                .hasMessage("Return quantity cannot exceed sold quantity");
        verify(stockService, never()).stockIn(any(), any(), any(), any(), any(), any());
        verify(accountingPostingService, never()).postSalesReturn(any());
    }

    @Test
    void approve_multipleReturnAttemptsCannotExceedRemainingQuantity() {
        Customer customer = customer();
        SalesInvoice invoice = invoice(customer);
        SalesReturn priorReturn = returnEntity(invoice, customer, 4L, "4.00", "24.00");
        priorReturn.setId(14L);
        priorReturn.setStatus(SalesReturnStatus.APPROVED);
        SalesReturn entity = returnEntity(invoice, customer, 4L, "2.00", "12.00");

        when(salesReturnRepository.findById(15L)).thenReturn(Optional.of(entity));
        when(salesReturnRepository.findPostedByInvoiceIdExcluding(9L, 15L)).thenReturn(List.of(priorReturn));

        assertThatThrownBy(() -> service.approve(15L))
                .hasMessage("Return quantity cannot exceed remaining unreturned quantity");
        verify(stockService, never()).stockIn(any(), any(), any(), any(), any(), any());
        verify(accountingPostingService, never()).postSalesReturn(any());
    }

    private SalesReturnDTO returnDto() {
        SalesReturnItemDTO item = new SalesReturnItemDTO();
        item.setProductId(4L);
        item.setQuantity(new BigDecimal("2.00"));
        item.setUnitPrice(new BigDecimal("6.00"));

        SalesReturnDTO dto = new SalesReturnDTO();
        dto.setInvoiceId(9L);
        dto.setCustomerId(2L);
        dto.setReturnDate(LocalDateTime.of(2026, 6, 6, 0, 0));
        dto.setStatus(SalesReturnStatus.PENDING);
        dto.getItems().add(item);
        return dto;
    }

    private SalesInvoice invoice(Customer customer) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(3L);
        warehouse.setName("Warehouse 3");

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(9L);
        invoice.setInvoiceNo("INV-0009");
        invoice.setCustomer(customer);
        invoice.setWarehouse(warehouse);
        invoice.setStatus(SalesInvoiceStatus.POSTED);
        invoice.setPaymentStatus(SalesPaymentStatus.DUE);
        invoice.setNetTotal(new BigDecimal("100.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setDueAmount(new BigDecimal("100.00"));
        SalesItem item = new SalesItem();
        item.setInvoice(invoice);
        item.setProduct(product());
        item.setQuantity(new BigDecimal("5.00"));
        item.setUnitPrice(new BigDecimal("6.00"));
        item.setSubTotal(new BigDecimal("30.00"));
        invoice.getItems().add(item);
        return invoice;
    }

    private SalesReturn returnEntity(SalesInvoice invoice, Customer customer, Long productId, String quantity, String total) {
        Product product = new Product();
        product.setId(productId);
        product.setProductName("Product " + productId);

        SalesReturn entity = new SalesReturn();
        entity.setId(15L);
        entity.setReturnCode("SR-0001");
        entity.setInvoice(invoice);
        entity.setCustomer(customer);
        entity.setReturnDate(LocalDateTime.of(2026, 6, 6, 0, 0));
        entity.setStatus(SalesReturnStatus.PENDING);
        entity.setTotalAmount(new BigDecimal(total));

        SalesReturnItem item = new SalesReturnItem();
        item.setReturnEntity(entity);
        item.setProduct(product);
        item.setQuantity(new BigDecimal(quantity));
        item.setUnitPrice(new BigDecimal("6.00"));
        item.setTotal(new BigDecimal(total));
        entity.getItems().add(item);
        return entity;
    }

    private Customer customer() {
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setName("Customer 2");
        return customer;
    }

    private Product product() {
        Product product = new Product();
        product.setId(4L);
        product.setProductName("Product 4");
        return product;
    }
}
