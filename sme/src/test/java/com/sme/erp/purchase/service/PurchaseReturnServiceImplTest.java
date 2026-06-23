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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
        PurchaseReturn entity = returnEntity(purchaseOrder, supplier, 4L, "2.00", "14.00");

        when(purchaseReturnRepository.findById(15L)).thenReturn(Optional.of(entity));
        when(purchaseReturnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.post(15L);

        verify(stockService).stockOut(4L, 3L, new BigDecimal("2.00"), "PURCHASE_RETURN", "PR-0001");
        verify(accountingPostingService).postPurchaseReturn(entity);
        assertThat(entity.getStatus()).isEqualTo(PurchaseStatus.POSTED);
    }

    @Test
    void post_overReceivedQuantityIsRejectedBeforePosting() {
        Supplier supplier = supplier();
        PurchaseOrder purchaseOrder = purchaseOrder(supplier);
        PurchaseReturn entity = returnEntity(purchaseOrder, supplier, 4L, "6.00", "42.00");

        when(purchaseReturnRepository.findById(15L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.post(15L))
                .hasMessage("Return quantity cannot exceed received quantity");
        verify(stockService, never()).stockOut(any(), any(), any(), any(), any());
        verify(accountingPostingService, never()).postPurchaseReturn(any());
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    void post_multipleReturnsCannotExceedRemainingQuantity() {
        Supplier supplier = supplier();
        PurchaseOrder purchaseOrder = purchaseOrder(supplier);
        purchaseOrder.getItems().get(0).setReturnedQuantity(new BigDecimal("4.00"));
        PurchaseReturn entity = returnEntity(purchaseOrder, supplier, 4L, "2.00", "14.00");

        when(purchaseReturnRepository.findById(15L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.post(15L))
                .hasMessage("Return quantity cannot exceed remaining unreturned quantity");
        verify(stockService, never()).stockOut(any(), any(), any(), any(), any());
        verify(accountingPostingService, never()).postPurchaseReturn(any());
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    void post_invalidProductIsRejectedBeforePosting() {
        Supplier supplier = supplier();
        PurchaseOrder purchaseOrder = purchaseOrder(supplier);
        PurchaseReturn entity = returnEntity(purchaseOrder, supplier, 99L, "1.00", "7.00");

        when(purchaseReturnRepository.findById(15L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.post(15L))
                .hasMessage("Returned product does not belong to the purchase order");
        verify(stockService, never()).stockOut(any(), any(), any(), any(), any());
        verify(accountingPostingService, never()).postPurchaseReturn(any());
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    void post_partialReceivedPurchaseIsRejected() {
        Supplier supplier = supplier();
        PurchaseOrder purchaseOrder = purchaseOrder(supplier);
        purchaseOrder.setStatus(PurchaseStatus.PARTIAL_RECEIVED);
        PurchaseReturn entity = returnEntity(purchaseOrder, supplier, 4L, "1.00", "7.00");

        when(purchaseReturnRepository.findById(15L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.post(15L))
                .hasMessage("Purchase return can only be posted against a received purchase order");
        verify(stockService, never()).stockOut(any(), any(), any(), any(), any());
        verify(accountingPostingService, never()).postPurchaseReturn(any());
        verify(purchaseOrderRepository, never()).save(any());
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

    private PurchaseReturn returnEntity(PurchaseOrder purchaseOrder, Supplier supplier, Long productId, String quantity, String total) {
        PurchaseReturn entity = new PurchaseReturn();
        entity.setId(15L);
        entity.setReturnCode("PR-0001");
        entity.setPurchase(purchaseOrder);
        entity.setSupplier(supplier);
        entity.setReturnDate(LocalDateTime.of(2026, 6, 6, 0, 0));
        entity.setStatus(PurchaseStatus.APPROVED);
        entity.setTotalAmount(new BigDecimal(total));
        entity.getItems().add(returnItem(entity, productId, quantity, total));
        return entity;
    }

    private com.sme.erp.purchase.entity.PurchaseReturnItem returnItem(PurchaseReturn entity, Long productId, String quantity, String total) {
        Product product = new Product();
        product.setId(productId);
        product.setProductName("Product " + productId);

        com.sme.erp.purchase.entity.PurchaseReturnItem item = new com.sme.erp.purchase.entity.PurchaseReturnItem();
        item.setReturnEntity(entity);
        item.setProduct(product);
        item.setQuantity(new BigDecimal(quantity));
        item.setUnitPrice(new BigDecimal("7.00"));
        item.setTotal(new BigDecimal(total));
        return item;
    }
}
