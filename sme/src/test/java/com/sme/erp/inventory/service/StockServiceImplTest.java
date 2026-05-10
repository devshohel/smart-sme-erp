package com.sme.erp.inventory.service;

import com.sme.erp.inventory.dto.StockDTO;
import com.sme.erp.inventory.entity.Stock;
import com.sme.erp.inventory.entity.StockMovement;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.mapper.StockMovementMapper;
import com.sme.erp.inventory.repository.StockAdjustmentRepository;
import com.sme.erp.inventory.repository.StockMovementRepository;
import com.sme.erp.inventory.repository.StockRepository;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.inventory.service.impl.StockServiceImpl;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private StockRepository stockRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private StockMovementRepository movementRepository;
    @Mock
    private StockAdjustmentRepository adjustmentRepository;

    private StockServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StockServiceImpl(
                stockRepository,
                productRepository,
                warehouseRepository,
                movementRepository,
                adjustmentRepository,
                new StockMovementMapper());
    }

    @Test
    void stockIn_updatesStockAndCreatesMovement() {
        Product product = new Product();
        product.setId(1L);
        product.setProductName("Product A");

        Warehouse warehouse = new Warehouse();
        warehouse.setId(2L);
        warehouse.setWarehouseCode("WH-01");
        warehouse.setName("Main Warehouse");

        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setWarehouse(warehouse);
        stock.setQuantity(new BigDecimal("5.00"));

        when(stockRepository.findByProductIdAndWarehouseId(1L, 2L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockDTO result = service.stockIn(1L, 2L, new BigDecimal("3.00"), new BigDecimal("4.50"));

        assertThat(result.getQuantity()).isEqualByComparingTo("8.00");

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movementRepository).save(movementCaptor.capture());

        StockMovement savedMovement = movementCaptor.getValue();
        assertThat(savedMovement.getProduct()).isSameAs(product);
        assertThat(savedMovement.getWarehouse()).isSameAs(warehouse);
        assertThat(savedMovement.getQuantity()).isEqualByComparingTo("3.00");
        assertThat(savedMovement.getUnitCost()).isEqualByComparingTo("4.50");
        assertThat(savedMovement.getMovementType().name()).isEqualTo("IN");
    }
}
