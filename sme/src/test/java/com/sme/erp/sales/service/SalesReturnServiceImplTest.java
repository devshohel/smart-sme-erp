package com.sme.erp.sales.service;

import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.sales.dto.SalesReturnDTO;
import com.sme.erp.sales.dto.SalesReturnItemDTO;
import com.sme.erp.sales.entity.SalesInvoice;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesReturnServiceImplTest {

    @Mock private SalesReturnRepository salesReturnRepository;
    @Mock private SalesInvoiceRepository salesInvoiceRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private StockService stockService;
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
                accountingPostingService);
    }

    @Test
    void createRestocksReturnedItems() {
        Customer customer = customer();
        SalesReturnDTO dto = returnDto();

        when(salesInvoiceRepository.findById(9L)).thenReturn(Optional.of(invoice(customer)));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(4L)).thenReturn(Optional.of(product()));
        when(salesReturnRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(salesReturnRepository.existsByReturnCode("SR-0001")).thenReturn(false);
        when(salesReturnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(dto);

        verify(stockService).stockIn(
                4L,
                3L,
                new BigDecimal("2.00"),
                new BigDecimal("6.00"),
                "SALES_RETURN",
                "SR-0001");
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
        return invoice;
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
