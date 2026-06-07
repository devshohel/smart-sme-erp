package com.sme.erp.purchase.service;

import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.purchase.dto.PurchaseReturnDTO;
import com.sme.erp.purchase.dto.PurchaseReturnItemDTO;
import com.sme.erp.purchase.entity.PurchaseOrder;
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
                accountingPostingService);
    }

    @Test
    void create_deductsReturnedStockFromPurchaseWarehouse() {
        Supplier supplier = supplier();
        PurchaseOrder purchaseOrder = purchaseOrder(supplier);
        PurchaseReturnDTO dto = returnDto();

        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(purchaseOrder));
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(4L)).thenReturn(Optional.of(product()));
        when(purchaseReturnRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(purchaseReturnRepository.existsByReturnCode("PR-0001")).thenReturn(false);
        when(purchaseReturnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(dto);

        verify(stockService).stockOut(4L, 3L, new BigDecimal("2.00"), "PURCHASE_RETURN", "PR-0001");
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
}
