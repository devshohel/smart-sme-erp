package com.sme.erp.purchase.service;

import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.product.repository.UomRepository;
import com.sme.erp.purchase.dto.PurchaseItemDTO;
import com.sme.erp.purchase.dto.PurchaseOrderDTO;
import com.sme.erp.purchase.entity.PurchaseItem;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.mapper.PurchaseItemMapper;
import com.sme.erp.purchase.mapper.PurchaseOrderMapper;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.service.impl.PurchaseOrderServiceImpl;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UomRepository uomRepository;
    @Mock
    private StockService stockService;

    private PurchaseOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PurchaseOrderServiceImpl(
                purchaseOrderRepository,
                supplierRepository,
                warehouseRepository,
                productRepository,
                uomRepository,
                new PurchaseOrderMapper(new PurchaseItemMapper()),
                stockService);
    }

    @Test
    void update_replacesManagedItemsAndPersistsEditedTotals() {
        PurchaseOrder existing = purchaseOrder(PurchaseStatus.PENDING);
        PurchaseItem oldItem = new PurchaseItem();
        oldItem.setPurchase(existing);
        oldItem.setProduct(product(99L));
        oldItem.setQuantity(new BigDecimal("1.00"));
        existing.getItems().add(oldItem);

        PurchaseOrderDTO dto = purchaseOrderDto(PurchaseStatus.APPROVED);

        mockReferences(existing, dto);
        when(purchaseOrderRepository.existsByPurchaseCodeAndIdNot("PO-0010", 10L)).thenReturn(false);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrderDTO result = service.update(10L, dto);

        assertThat(existing.getItems()).hasSize(1);
        assertThat(existing.getItems().get(0).getProduct().getId()).isEqualTo(4L);
        assertThat(existing.getItems().get(0).getPurchase()).isSameAs(existing);
        assertThat(result.getStatus()).isEqualTo(PurchaseStatus.APPROVED);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("14.00");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("1.00");
        assertThat(result.getTaxAmount()).isEqualByComparingTo("0.50");
        assertThat(result.getNetTotal()).isEqualByComparingTo("13.50");
        assertThat(result.getDueAmount()).isEqualByComparingTo("13.50");
        verify(stockService, never()).stockIn(any(), any(), any(), any());
    }

    @Test
    void update_receivesStockOnlyOnFirstTransitionToReceived() {
        PurchaseOrder existing = purchaseOrder(PurchaseStatus.APPROVED);
        PurchaseOrderDTO dto = purchaseOrderDto(PurchaseStatus.RECEIVED);

        mockReferences(existing, dto);
        when(purchaseOrderRepository.existsByPurchaseCodeAndIdNot("PO-0010", 10L)).thenReturn(false);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(10L, dto);

        verify(stockService).stockIn(4L, 3L, new BigDecimal("2.00"), new BigDecimal("7.00"));
    }

    @Test
    void update_doesNotReceiveStockAgainWhenAlreadyReceived() {
        PurchaseOrder existing = purchaseOrder(PurchaseStatus.RECEIVED);
        PurchaseOrderDTO dto = purchaseOrderDto(PurchaseStatus.RECEIVED);

        mockReferences(existing, dto);
        when(purchaseOrderRepository.existsByPurchaseCodeAndIdNot("PO-0010", 10L)).thenReturn(false);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(10L, dto);

        verify(stockService, never()).stockIn(any(), any(), any(), any());
    }

    private void mockReferences(PurchaseOrder existing, PurchaseOrderDTO dto) {
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(supplierRepository.findById(dto.getSupplierId())).thenReturn(Optional.of(supplier(dto.getSupplierId())));
        when(warehouseRepository.findById(dto.getWarehouseId())).thenReturn(Optional.of(warehouse(dto.getWarehouseId())));
        when(productRepository.findById(4L)).thenReturn(Optional.of(product(4L)));
    }

    private PurchaseOrder purchaseOrder(PurchaseStatus status) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(10L);
        order.setPurchaseCode("PO-0010");
        order.setSupplier(supplier(2L));
        order.setWarehouse(warehouse(3L));
        order.setPurchaseDate(LocalDateTime.of(2026, 6, 6, 0, 0));
        order.setStatus(status);
        return order;
    }

    private PurchaseOrderDTO purchaseOrderDto(PurchaseStatus status) {
        PurchaseItemDTO item = new PurchaseItemDTO();
        item.setProductId(4L);
        item.setQuantity(new BigDecimal("2.00"));
        item.setUnitPrice(new BigDecimal("7.00"));
        item.setDiscount(new BigDecimal("1.00"));
        item.setTax(new BigDecimal("0.50"));

        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setPurchaseCode("PO-0010");
        dto.setSupplierId(2L);
        dto.setWarehouseId(3L);
        dto.setPurchaseDate(LocalDateTime.of(2026, 6, 6, 0, 0));
        dto.setPaidAmount(BigDecimal.ZERO);
        dto.setStatus(status);
        dto.getItems().add(item);
        return dto;
    }

    private Supplier supplier(Long id) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setName("Supplier " + id);
        return supplier;
    }

    private Warehouse warehouse(Long id) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        warehouse.setName("Warehouse " + id);
        return warehouse;
    }

    private Product product(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setProductName("Product " + id);
        return product;
    }
}
