package com.sme.erp.inventory.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.inventory.dto.StockDTO;
import com.sme.erp.inventory.dto.StockMovementDTO;
import com.sme.erp.inventory.entity.*;
import com.sme.erp.enums.MovementType;
import com.sme.erp.inventory.mapper.StockMovementMapper;
import com.sme.erp.inventory.repository.*;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository movementRepository;
    private final StockAdjustmentRepository adjustmentRepository;
    private final StockMovementMapper stockMovementMapper;

    public StockServiceImpl(
            StockRepository stockRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            StockMovementRepository movementRepository,
            StockAdjustmentRepository adjustmentRepository,
            StockMovementMapper stockMovementMapper) {

        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.movementRepository = movementRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.stockMovementMapper = stockMovementMapper;
    }

    // STOCK IN
    @Override
    @Transactional
    public StockDTO stockIn(Long productId, Long warehouseId, BigDecimal qty, BigDecimal unitCost) {
        validatePositiveId(productId, "Product id");
        validatePositiveId(warehouseId, "Warehouse id");
        validatePositiveAmount(qty, "Quantity");
        validatePositiveAmount(unitCost, "Unit cost");

        Stock stock = getOrCreateStock(productId, warehouseId);

        stock.setQuantity(stock.getQuantity().add(qty));
        stockRepository.save(stock);

        saveMovement(stock, qty, MovementType.IN, "PURCHASE", unitCost);

        return toDTO(stock);
    }

    // STOCK OUT
    @Override
    @Transactional
    public StockDTO stockOut(Long productId, Long warehouseId, BigDecimal qty) {
        validatePositiveId(productId, "Product id");
        validatePositiveId(warehouseId, "Warehouse id");
        validatePositiveAmount(qty, "Quantity");

        Stock stock = getStockEntity(productId, warehouseId);

        if (stock.getQuantity().compareTo(qty) < 0) {
            throw new BadRequestException("Insufficient stock");
        }

        stock.setQuantity(stock.getQuantity().subtract(qty));
        stockRepository.save(stock);

        saveMovement(stock, qty, MovementType.OUT, "SALE", null);

        return toDTO(stock);
    }

    // ADJUSTMENT
    @Override
    @Transactional
    public StockDTO adjustStock(Long productId, Long warehouseId, BigDecimal qty, String reason) {
        validatePositiveId(productId, "Product id");
        validatePositiveId(warehouseId, "Warehouse id");
        validateAdjustment(qty, reason);

        Stock stock = getOrCreateStock(productId, warehouseId);

        stock.setQuantity(stock.getQuantity().add(qty));
        stockRepository.save(stock);

        StockAdjustment adj = new StockAdjustment();
        adj.setProduct(stock.getProduct());
        adj.setWarehouse(stock.getWarehouse());
        adj.setQuantity(qty);
        adj.setReason(reason);

        adjustmentRepository.save(adj);

        saveMovement(stock, qty.abs(), MovementType.ADJUSTMENT, reason, null);

        return toDTO(stock);
    }

    @Override
    public StockDTO getStock(Long productId, Long warehouseId) {
        validatePositiveId(productId, "Product id");
        validatePositiveId(warehouseId, "Warehouse id");
        return toDTO(getStockEntity(productId, warehouseId));
    }

    @Override
    public List<StockDTO> getAllStock() {
        return stockRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementDTO> getAllMovements() {
        return movementRepository.findAll()
                .stream()
                .map(stockMovementMapper::toDTO)
                .collect(Collectors.toList());
    }

    // HELPER METHODS

    private void saveMovement(Stock stock, BigDecimal qty, MovementType type, String ref, BigDecimal cost) {

        StockMovement movement = new StockMovement();
        movement.setProduct(stock.getProduct());
        movement.setWarehouse(stock.getWarehouse());
        movement.setQuantity(qty);
        movement.setMovementType(type);
        movement.setReferenceNo(ref);
        movement.setUnitCost(cost);

        movementRepository.save(movement);
        
        

    }

    private Stock getStockEntity(Long productId, Long warehouseId) {
        return stockRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found for product id: " + productId + " and warehouse id: " + warehouseId));
    }

    private Stock getOrCreateStock(Long productId, Long warehouseId) {
        return stockRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseGet(() -> {

                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

                    Warehouse warehouse = warehouseRepository.findById(warehouseId)
                            .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

                    Stock stock = new Stock();
                    stock.setProduct(product);
                    stock.setWarehouse(warehouse);
                    stock.setQuantity(BigDecimal.ZERO);

                    return stock;
                });
    }

    private StockDTO toDTO(Stock stock) {
        StockDTO dto = new StockDTO();

        dto.setId(stock.getId());
        dto.setProductId(stock.getProduct().getId());
        dto.setProductName(stock.getProduct().getProductName());
        dto.setWarehouseId(stock.getWarehouse().getId());
        dto.setWarehouseName(stock.getWarehouse().getName());
        dto.setQuantity(stock.getQuantity());
        dto.setReorderLevel(stock.getReorderLevel());

        return dto;
    }

    private void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new BadRequestException(fieldName + " must be positive");
        }
    }

    private void validatePositiveAmount(BigDecimal amount, String fieldName) {
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException(fieldName + " must be positive");
        }
    }

    private void validateAdjustment(BigDecimal qty, String reason) {
        if (qty == null || qty.signum() == 0) {
            throw new BadRequestException("Quantity must not be zero");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Reason is required");
        }
    }
}
