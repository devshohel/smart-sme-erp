package com.sme.erp.inventory.service;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.inventory.dto.StockTransferDTO;
import com.sme.erp.inventory.dto.StockTransferItemDTO;
import com.sme.erp.inventory.entity.StockTransfer;
import com.sme.erp.inventory.entity.StockTransferItem;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.enums.StockTransferStatus;
import com.sme.erp.inventory.repository.StockTransferRepository;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.inventory.service.impl.StockTransferServiceImpl;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockTransferServiceImplTest {

    @Mock private StockTransferRepository transferRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private ProductRepository productRepository;
    @Mock private StockService stockService;

    private StockTransferServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StockTransferServiceImpl(transferRepository, warehouseRepository, productRepository, stockService);
        lenient().when(transferRepository.save(any(StockTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createTransfer_doesNotChangeStock() {
        mockMasterData();
        when(transferRepository.findTopByOrderByIdDesc()).thenReturn(null);

        StockTransferDTO result = service.create(request(StockTransferStatus.PENDING));

        assertThat(result.getStatus()).isEqualTo(StockTransferStatus.PENDING);
        assertThat(result.getTransferNo()).isEqualTo("ST-000001");
        verifyNoInteractions(stockService);
    }

    @Test
    void approveTransfer_doesNotChangeStock() {
        StockTransfer transfer = transfer(StockTransferStatus.PENDING);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        StockTransferDTO result = service.approve(1L);

        assertThat(result.getStatus()).isEqualTo(StockTransferStatus.APPROVED);
        verifyNoInteractions(stockService);
    }

    @Test
    void sendTransfer_deductsSourceStockOnce() {
        StockTransfer transfer = transfer(StockTransferStatus.APPROVED);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        StockTransferDTO sent = service.send(1L);
        assertThat(sent.getStatus()).isEqualTo(StockTransferStatus.IN_TRANSIT);
        verify(stockService).transferOut(10L, 1L, new BigDecimal("2.00"), "ST-000010");

        assertThatThrownBy(() -> service.send(1L))
                .isInstanceOf(BadRequestException.class);
        verify(stockService, times(1)).transferOut(10L, 1L, new BigDecimal("2.00"), "ST-000010");
    }

    @Test
    void receiveTransfer_addsDestinationStockOnce() {
        StockTransfer transfer = transfer(StockTransferStatus.IN_TRANSIT);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        StockTransferDTO received = service.receive(1L);
        assertThat(received.getStatus()).isEqualTo(StockTransferStatus.RECEIVED);
        verify(stockService).transferIn(10L, 2L, new BigDecimal("2.00"), "ST-000010");

        assertThatThrownBy(() -> service.receive(1L))
                .isInstanceOf(BadRequestException.class);
        verify(stockService, times(1)).transferIn(10L, 2L, new BigDecimal("2.00"), "ST-000010");
    }

    @Test
    void sendTransfer_failsIfInsufficientStock() {
        StockTransfer transfer = transfer(StockTransferStatus.APPROVED);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        doThrow(new BadRequestException("Insufficient stock"))
                .when(stockService).transferOut(10L, 1L, new BigDecimal("2.00"), "ST-000010");

        assertThatThrownBy(() -> service.send(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Insufficient stock");
        assertThat(transfer.getStatus()).isEqualTo(StockTransferStatus.APPROVED);
    }

    @Test
    void cancelBeforeSend_doesNotChangeStock() {
        StockTransfer transfer = transfer(StockTransferStatus.APPROVED);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        StockTransferDTO cancelled = service.cancel(1L);

        assertThat(cancelled.getStatus()).isEqualTo(StockTransferStatus.CANCELLED);
        verifyNoInteractions(stockService);
    }

    @Test
    void cannotEditOrCancelAfterInTransit() {
        StockTransfer transfer = transfer(StockTransferStatus.IN_TRANSIT);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThatThrownBy(() -> service.update(1L, request(StockTransferStatus.PENDING)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Transfer cannot be edited after in transit.");
        assertThatThrownBy(() -> service.cancel(1L))
                .isInstanceOf(BadRequestException.class);
    }

    private StockTransferDTO request(StockTransferStatus status) {
        StockTransferDTO dto = new StockTransferDTO();
        dto.setFromWarehouseId(1L);
        dto.setToWarehouseId(2L);
        dto.setTransferDate(LocalDate.now());
        dto.setStatus(status);
        StockTransferItemDTO item = new StockTransferItemDTO();
        item.setProductId(10L);
        item.setQuantity(new BigDecimal("2.00"));
        dto.getItems().add(item);
        return dto;
    }

    private void mockMasterData() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse(1L, "Main")));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse(2L, "Branch")));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product()));
    }

    private StockTransfer transfer(StockTransferStatus status) {
        StockTransfer transfer = new StockTransfer();
        transfer.setId(1L);
        transfer.setTransferNo("ST-000010");
        transfer.setStatus(status);
        transfer.setTransferDate(LocalDate.now());
        transfer.setFromWarehouse(warehouse(1L, "Main"));
        transfer.setToWarehouse(warehouse(2L, "Branch"));
        StockTransferItem item = new StockTransferItem();
        item.setTransfer(transfer);
        item.setProduct(product());
        item.setQuantity(new BigDecimal("2.00"));
        transfer.getItems().add(item);
        return transfer;
    }

    private Warehouse warehouse(Long id, String name) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        warehouse.setWarehouseCode("WH-" + id);
        warehouse.setName(name);
        return warehouse;
    }

    private Product product() {
        Product product = new Product();
        product.setId(10L);
        product.setProductName("Product A");
        product.setSku("SKU-A");
        return product;
    }
}
