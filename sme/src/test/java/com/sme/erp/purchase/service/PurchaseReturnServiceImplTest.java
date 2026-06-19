package com.sme.erp.purchase.service;

import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.purchase.dto.PurchaseReturnDTO;
import com.sme.erp.purchase.dto.PurchaseReturnItemDTO;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.entity.PurchaseItem;
import com.sme.erp.purchase.entity.PurchaseReturn;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.mapper.PurchaseReturnItemMapper;
import com.sme.erp.purchase.mapper.PurchaseReturnMapper;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.repository.PurchaseReturnRepository;
import com.sme.erp.purchase.service.impl.PurchaseReturnServiceImpl;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.repository.SupplierRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseReturnServiceImplTest {

    @Mock
    private PurchaseReturnRepository purchaseReturnRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockService stockService;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private AccountingPostingService accountingPostingService;

    private PurchaseReturnServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PurchaseReturnServiceImpl(
                purchaseReturnRepository,
                purchaseOrderRepository,
                supplierRepository,
                productRepository,
                new PurchaseReturnMapper(new PurchaseReturnItemMapper()),
                stockService,
                activityLogService,
                auditLogService,
                accountingPostingService);
    }

    @Test
    void post_deductsReturnedStockFromPurchaseWarehouse() {
        Supplier supplier = supplier();
        PurchaseOrder purchaseOrder = purchaseOrder(supplier);
        PurchaseReturn entity = new PurchaseReturn();
        entity.setId(15L);
        entity.setReturnCode("PR-0001");
        entity.setPurchase(purchaseOrder);
        entity.setSupplier(supplier);
        entity.setReturnDate(LocalDateTime.of(2026, 6, 6, 0, 0));
        entity.setStatus(PurchaseStatus.APPROVED);
        entity.getItems().addAll(serviceCreateItems(entity));
        entity.setTotalAmount(new BigDecimal("14.00"));

        when(purchaseReturnRepository.findById(15L)).thenReturn(Optional.of(entity));
        when(purchaseReturnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.post(15L);

        verify(stockService).stockOut(4L, 3L, new BigDecimal("2.00"), "PURCHASE_RETURN", "PR-0001");
        verify(accountingPostingService).postPurchaseReturn(entity);
        assertThat(entity.getStatus()).isEqualTo(PurchaseStatus.POSTED);
    }

    private PurchaseReturnDTO returnDto() {
        PurchaseReturnItemDTO item = new PurchaseReturnItemDTO();
        item.setProductId(4L);
        item.setQuantity(new BigDecimal("2.00"));
        item.setUnitPrice(new BigDecimal("7.00"));

        PurchaseReturnDTO dto = new PurchaseReturnDTO();
        dto.setPurchaseId(10L);
        dto.setSupplierId(2L);
        dto.setReturnDate(LocalDateTime.of(2026, 6, 6, 0, 0));
        dto.getItems().add(item);
        return dto;
    }

    private PurchaseOrder purchaseOrder(Supplier supplier) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(3L);
        warehouse.setName("Main Warehouse");

        PurchaseOrder order = new PurchaseOrder();
        order.setId(10L);
        order.setPurchaseCode("PO-0010");
        order.setSupplier(supplier);
        order.setWarehouse(warehouse);
        order.setStatus(PurchaseStatus.RECEIVED);
        order.setDueAmount(new BigDecimal("20.00"));
        order.setPaidAmount(BigDecimal.ZERO);

        PurchaseItem item = new PurchaseItem();
        item.setId(40L);
        item.setPurchase(order);
        item.setProduct(product());
        item.setQuantity(new BigDecimal("5.00"));
        item.setReceivedQuantity(new BigDecimal("5.00"));
        item.setReturnedQuantity(BigDecimal.ZERO);
        item.setUnitPrice(new BigDecimal("7.00"));
        order.getItems().add(item);
        return order;
    }

    private Supplier supplier() {
        Supplier supplier = new Supplier();
        supplier.setId(2L);
        supplier.setName("Supplier 2");
        return supplier;
    }

    private Product product() {
        Product product = new Product();
        product.setId(4L);
        product.setProductName("Product 4");
        return product;
    }

    private java.util.List<com.sme.erp.purchase.entity.PurchaseReturnItem> serviceCreateItems(PurchaseReturn entity) {
        com.sme.erp.purchase.entity.PurchaseReturnItem item = new com.sme.erp.purchase.entity.PurchaseReturnItem();
        item.setReturnEntity(entity);
        item.setProduct(product());
        item.setQuantity(new BigDecimal("2.00"));
        item.setUnitPrice(new BigDecimal("7.00"));
        item.setTotal(new BigDecimal("14.00"));
        return java.util.List.of(item);
    }
}
