package com.sme.erp.sales.service;

import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.product.repository.UomRepository;
import com.sme.erp.sales.dto.SalesItemDTO;
import com.sme.erp.sales.dto.SalesOrderDTO;
import com.sme.erp.sales.entity.SalesOrder;
import com.sme.erp.sales.enums.SalesOrderStatus;
import com.sme.erp.sales.mapper.SalesItemMapper;
import com.sme.erp.sales.mapper.SalesOrderMapper;
import com.sme.erp.sales.repository.SalesOrderRepository;
import com.sme.erp.sales.service.impl.SalesOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceImplTest {

    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UomRepository uomRepository;
    @Mock private ActivityLogService activityLogService;
    @Mock private AuditLogService auditLogService;

    private SalesOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SalesOrderServiceImpl(
                salesOrderRepository,
                customerRepository,
                warehouseRepository,
                productRepository,
                uomRepository,
                new SalesOrderMapper(new SalesItemMapper()),
                activityLogService,
                auditLogService);
    }

    @Test
    void update_persistsOrderItemsNotesAndGrandTotal() {
        SalesOrder existing = new SalesOrder();
        existing.setId(7L);
        existing.setOrderNo("SO-0007");

        SalesOrderDTO dto = orderDto();

        when(salesOrderRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(salesOrderRepository.existsByOrderNoAndIdNot("SO-0007", 7L)).thenReturn(false);
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer()));
        when(warehouseRepository.findById(3L)).thenReturn(Optional.of(warehouse()));
        when(productRepository.findById(4L)).thenReturn(Optional.of(product()));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalesOrderDTO result = service.update(7L, dto);

        assertThat(result.getStatus()).isEqualTo(SalesOrderStatus.APPROVED);
        assertThat(result.getNotes()).isEqualTo("Urgent delivery");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductId()).isEqualTo(4L);
        assertThat(result.getGrandTotal()).isEqualByComparingTo("15.00");
    }

    private SalesOrderDTO orderDto() {
        SalesItemDTO item = new SalesItemDTO();
        item.setProductId(4L);
        item.setQuantity(new BigDecimal("3.00"));
        item.setUnitPrice(new BigDecimal("5.00"));

        SalesOrderDTO dto = new SalesOrderDTO();
        dto.setOrderNo("SO-0007");
        dto.setCustomerId(2L);
        dto.setWarehouseId(3L);
        dto.setOrderDate(LocalDateTime.of(2026, 6, 6, 0, 0));
        dto.setStatus(SalesOrderStatus.APPROVED);
        dto.setNotes("Urgent delivery");
        dto.getItems().add(item);
        return dto;
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
