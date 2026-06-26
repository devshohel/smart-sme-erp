package com.sme.erp.inventory.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.inventory.dto.StockCardDTO;
import com.sme.erp.inventory.dto.StockDTO;
import com.sme.erp.inventory.dto.StockMovementDTO;
import com.sme.erp.inventory.dto.StockMovementPageDTO;
import com.sme.erp.inventory.dto.StockPageDTO;
import com.sme.erp.inventory.entity.*;
import com.sme.erp.enums.MovementType;
import com.sme.erp.inventory.mapper.StockMapper;
import com.sme.erp.inventory.mapper.StockMovementMapper;
import com.sme.erp.inventory.repository.*;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.notification.enums.NotificationSeverity;
import com.sme.erp.notification.enums.NotificationType;
import com.sme.erp.notification.service.NotificationService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository movementRepository;
    private final StockAdjustmentRepository adjustmentRepository;
    private final StockMapper stockMapper;
    private final StockMovementMapper stockMovementMapper;
    private final NotificationService notificationService;

    public StockServiceImpl(
            StockRepository stockRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            StockMovementRepository movementRepository,
            StockAdjustmentRepository adjustmentRepository,
            StockMapper stockMapper,
            StockMovementMapper stockMovementMapper,
            NotificationService notificationService) {

        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.movementRepository = movementRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.stockMapper = stockMapper;
        this.stockMovementMapper = stockMovementMapper;
        this.notificationService = notificationService;
    }

    // STOCK IN
    @Override
    @Transactional
    public StockDTO stockIn(Long productId, Long warehouseId, BigDecimal qty, BigDecimal unitCost) {
        return stockIn(productId, warehouseId, qty, unitCost, "PURCHASE", null);
    }

    @Override
    @Transactional
    public StockDTO stockIn(
            Long productId,
            Long warehouseId,
            BigDecimal qty,
            BigDecimal unitCost,
            String referenceType,
            String referenceNo) {
        validatePositiveId(productId, "Product id");
        validatePositiveId(warehouseId, "Warehouse id");
        validatePositiveAmount(qty, "Quantity");
        validatePositiveAmount(unitCost, "Unit cost");

        Stock stock = getOrCreateStock(productId, warehouseId);

        BigDecimal before = safe(stock.getQuantity());
        BigDecimal after = before.add(qty);
        stock.setQuantity(after);
        saveStockOrThrowConflict(stock);

        saveMovement(stock, qty, qty, before, after, MovementType.IN,
                normalizeReferenceType(referenceType, "PURCHASE"), referenceNo, unitCost);

        return stockMapper.toDTO(stock);
    }

    // STOCK OUT
    @Override
    @Transactional
    public StockDTO stockOut(Long productId, Long warehouseId, BigDecimal qty) {
        return stockOut(productId, warehouseId, qty, "SALE", null);
    }

    @Override
    @Transactional
    public StockDTO stockOut(Long productId, Long warehouseId, BigDecimal qty, String referenceType, String referenceNo) {
        validatePositiveId(productId, "Product id");
        validatePositiveId(warehouseId, "Warehouse id");
        validatePositiveAmount(qty, "Quantity");

        Stock stock = getStockEntityForUpdate(productId, warehouseId);

        if (stock.getQuantity().compareTo(qty) < 0) {
            notifyNegativeStockAttempt(stock, qty, referenceNo);
            throw new BadRequestException("Insufficient stock");
        }

        BigDecimal before = safe(stock.getQuantity());
        BigDecimal change = qty.negate();
        BigDecimal after = before.add(change);
        stock.setQuantity(after);
        saveStockOrThrowConflict(stock);

        saveMovement(stock, qty, change, before, after, MovementType.OUT,
                normalizeReferenceType(referenceType, "SALE"), referenceNo, null);
        notifyLowStock(stock, after);

        return stockMapper.toDTO(stock);
    }

    @Override
    @Transactional
    public StockDTO transferOut(Long productId, Long warehouseId, BigDecimal qty, String referenceNo) {
        validatePositiveId(productId, "Product id");
        validatePositiveId(warehouseId, "Warehouse id");
        validatePositiveAmount(qty, "Quantity");

        Stock stock = getStockEntityForUpdate(productId, warehouseId);
        if (stock.getQuantity().compareTo(qty) < 0) {
            notifyNegativeStockAttempt(stock, qty, referenceNo);
            throw new BadRequestException("Insufficient stock");
        }

        BigDecimal before = safe(stock.getQuantity());
        BigDecimal change = qty.negate();
        BigDecimal after = before.add(change);
        stock.setQuantity(after);
        saveStockOrThrowConflict(stock);

        saveMovement(stock, qty, change, before, after, MovementType.TRANSFER,
                "STOCK_TRANSFER_OUT", referenceNo, null);
        notifyLowStock(stock, after);

        return stockMapper.toDTO(stock);
    }

    @Override
    @Transactional
    public StockDTO transferIn(Long productId, Long warehouseId, BigDecimal qty, String referenceNo) {
        validatePositiveId(productId, "Product id");
        validatePositiveId(warehouseId, "Warehouse id");
        validatePositiveAmount(qty, "Quantity");

        Stock stock = getOrCreateStock(productId, warehouseId);
        BigDecimal before = safe(stock.getQuantity());
        BigDecimal after = before.add(qty);
        stock.setQuantity(after);
        saveStockOrThrowConflict(stock);

        saveMovement(stock, qty, qty, before, after, MovementType.TRANSFER,
                "STOCK_TRANSFER_IN", referenceNo, null);

        return stockMapper.toDTO(stock);
    }

    // ADJUSTMENT
    @Override
    @Transactional
    public StockDTO adjustStock(Long productId, Long warehouseId, BigDecimal qty, String reason) {
        validatePositiveId(productId, "Product id");
        validatePositiveId(warehouseId, "Warehouse id");
        validateAdjustment(qty, reason);

        Stock stock = getOrCreateStock(productId, warehouseId);

        BigDecimal before = safe(stock.getQuantity());
        BigDecimal after = before.add(qty);
        if (after.signum() < 0) {
            notifyNegativeStockAttempt(stock, qty.abs(), reason);
            throw new BadRequestException("Adjustment cannot make stock negative.");
        }

        stock.setQuantity(after);
        saveStockOrThrowConflict(stock);

        StockAdjustment adj = new StockAdjustment();
        adj.setProduct(stock.getProduct());
        adj.setWarehouse(stock.getWarehouse());
        adj.setQuantity(qty);
        adj.setReason(reason);

        adjustmentRepository.save(adj);

        saveMovement(stock, qty.abs(), qty, before, after, MovementType.ADJUSTMENT, "ADJUSTMENT", reason, null);
        notifyLowStock(stock, after);

        return stockMapper.toDTO(stock);
    }

    @Override
    public StockDTO getStock(Long productId, Long warehouseId) {
        validatePositiveId(productId, "Product id");
        validatePositiveId(warehouseId, "Warehouse id");
        return stockMapper.toDTO(getStockEntity(productId, warehouseId));
    }

    @Override
    public List<StockDTO> getAllStock() {
        return stockRepository.findAll()
                .stream()
                .map(stockMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementDTO> getAllMovements() {
        return movementRepository.findAll()
                .stream()
                .map(stockMovementMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockPageDTO searchStock(String keyword, Long warehouseId, Long categoryId, Boolean lowStockOnly,
                                    int page, int size, String sort, String direction) {
        Pageable pageable = PageRequest.of(safePage(page), safeSize(size), sortForStock(sort, direction));
        Page<Stock> result = stockRepository.findAll(stockSpec(keyword, warehouseId, categoryId, lowStockOnly), pageable);

        return new StockPageDTO(
                result.getContent().stream().map(stockMapper::toDTO).collect(Collectors.toList()),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public StockMovementPageDTO searchMovements(String keyword, Long productId, Long warehouseId, MovementType movementType,
                                                String referenceType, LocalDate fromDate, LocalDate toDate,
                                                int page, int size, String sort, String direction) {
        Pageable pageable = PageRequest.of(safePage(page), safeSize(size), sortForMovement(sort, direction));
        Page<StockMovement> result = movementRepository.findAll(
                movementSpec(keyword, productId, warehouseId, movementType, referenceType, fromDate, toDate), pageable);

        return new StockMovementPageDTO(
                result.getContent().stream().map(stockMovementMapper::toDTO).collect(Collectors.toList()),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public StockCardDTO getStockCard(Long productId, Long warehouseId) {
        validatePositiveId(productId, "Product id");
        validatePositiveId(warehouseId, "Warehouse id");

        Stock stock = getStockEntity(productId, warehouseId);
        List<StockMovementDTO> movements = movementRepository
                .findByProductIdAndWarehouseIdOrderByCreatedAtAscIdAsc(productId, warehouseId)
                .stream()
                .map(stockMovementMapper::toDTO)
                .collect(Collectors.toList());

        return new StockCardDTO(
                productId,
                stock.getProduct() != null ? stock.getProduct().getProductName() : null,
                warehouseId,
                stock.getWarehouse() != null ? stock.getWarehouse().getName() : null,
                safe(stock.getQuantity()),
                movements);
    }

    // HELPER METHODS

    private void saveMovement(
            Stock stock,
            BigDecimal qty,
            BigDecimal quantityChange,
            BigDecimal quantityBefore,
            BigDecimal quantityAfter,
            MovementType type,
            String referenceType,
            String referenceNo,
            BigDecimal cost) {

        StockMovement movement = new StockMovement();
        movement.setMovementCode(nextMovementCode());
        movement.setProduct(stock.getProduct());
        movement.setWarehouse(stock.getWarehouse());
        movement.setQuantity(qty);
        movement.setQuantityBefore(quantityBefore);
        movement.setQuantityChange(quantityChange);
        movement.setQuantityAfter(quantityAfter);
        movement.setMovementType(type);
        movement.setReferenceType(referenceType);
        movement.setReferenceNo(referenceNo);
        movement.setUnitCost(cost);

        movementRepository.save(movement);
    }

    private void notifyLowStock(Stock stock, BigDecimal quantityAfter) {
        Product product = stock.getProduct();
        Integer reorderLevel = product != null ? product.getReorderLevel() : null;
        if (product == null || reorderLevel == null || reorderLevel <= 0
                || quantityAfter.compareTo(BigDecimal.valueOf(reorderLevel)) > 0) {
            return;
        }

        String warehouseName = stock.getWarehouse() != null ? stock.getWarehouse().getName() : "selected warehouse";
        notificationService.notifyGlobalOnce(
                "Low stock warning",
                product.getProductName() + " is at or below reorder level in " + warehouseName
                        + ". Current quantity: " + quantityAfter + ".",
                NotificationType.WARNING,
                NotificationSeverity.HIGH,
                "STOCK",
                stock.getId(),
                "/inventory/stocks");
    }

    private void notifyNegativeStockAttempt(Stock stock, BigDecimal requestedQty, String referenceNo) {
        Product product = stock.getProduct();
        String productName = product != null ? product.getProductName() : "Selected product";
        String warehouseName = stock.getWarehouse() != null ? stock.getWarehouse().getName() : "selected warehouse";
        notificationService.notifyGlobal(
                "Negative stock prevented",
                productName + " in " + warehouseName + " could not be reduced by " + requestedQty
                        + " because available stock is " + safe(stock.getQuantity()) + ".",
                NotificationType.ERROR,
                NotificationSeverity.CRITICAL,
                "STOCK",
                stock.getId(),
                referenceNo != null && !referenceNo.isBlank() ? "/inventory/movements" : "/inventory/stocks");
    }

    private synchronized String nextMovementCode() {
        StockMovement lastMovement = movementRepository.findTopByOrderByIdDesc();
        long next = lastMovement == null || lastMovement.getId() == null ? 1L : lastMovement.getId() + 1L;
        return String.format("MOV-%06d", next);
    }

    private Stock getStockEntity(Long productId, Long warehouseId) {
        return stockRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found for product id: " + productId + " and warehouse id: " + warehouseId));
    }

    private Stock getStockEntityForUpdate(Long productId, Long warehouseId) {
        return stockRepository
                .findWithLockByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found for product id: " + productId + " and warehouse id: " + warehouseId));
    }

    private Stock getOrCreateStock(Long productId, Long warehouseId) {
        return stockRepository
                .findWithLockByProductIdAndWarehouseId(productId, warehouseId)
                .orElseGet(() -> createStockSafely(productId, warehouseId));
    }

    private Stock createStockSafely(Long productId, Long warehouseId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setWarehouse(warehouse);
        stock.setQuantity(BigDecimal.ZERO);

        try {
            return stockRepository.saveAndFlush(stock);
        } catch (DataIntegrityViolationException ex) {
            // Another transaction created the same stock row first.
            return stockRepository.findWithLockByProductIdAndWarehouseId(productId, warehouseId)
                    .orElseThrow(() -> ex);
        }
    }

    private void saveStockOrThrowConflict(Stock stock) {
        try {
            stockRepository.saveAndFlush(stock);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new BadRequestException("Stock was modified by another transaction. Please retry.");
        }
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

    private String normalizeReferenceType(String referenceType, String fallback) {
        if (referenceType == null || referenceType.trim().isEmpty()) {
            return fallback;
        }
        return referenceType.trim();
    }

    private Specification<Stock> stockSpec(String keyword, Long warehouseId, Long categoryId, Boolean lowStockOnly) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("product", jakarta.persistence.criteria.JoinType.LEFT)
                        .fetch("category", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("warehouse", jakarta.persistence.criteria.JoinType.LEFT);
                query.distinct(true);
            }

            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            jakarta.persistence.criteria.Join<Stock, Product> product =
                    root.join("product", jakarta.persistence.criteria.JoinType.LEFT);
            jakarta.persistence.criteria.Join<Stock, Warehouse> warehouse =
                    root.join("warehouse", jakarta.persistence.criteria.JoinType.LEFT);

            String normalizedKeyword = normalizeSearch(keyword);
            if (normalizedKeyword != null) {
                String pattern = "%" + normalizedKeyword + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(product.get("productName")), pattern),
                        cb.like(cb.lower(product.get("sku")), pattern),
                        cb.like(cb.lower(product.get("barcode")), pattern)));
            }
            if (warehouseId != null) {
                predicates.add(cb.equal(warehouse.get("id"), warehouseId));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(product.get("category").get("id"), categoryId));
            }
            if (Boolean.TRUE.equals(lowStockOnly)) {
                predicates.add(cb.gt(product.get("reorderLevel"), 0));
                predicates.add(cb.lessThanOrEqualTo(root.get("quantity"), product.get("reorderLevel").as(BigDecimal.class)));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Specification<StockMovement> movementSpec(String keyword, Long productId, Long warehouseId,
                                                      MovementType movementType, String referenceType,
                                                      LocalDate fromDate, LocalDate toDate) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("product", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("warehouse", jakarta.persistence.criteria.JoinType.LEFT);
                query.distinct(true);
            }

            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            jakarta.persistence.criteria.Join<StockMovement, Product> product =
                    root.join("product", jakarta.persistence.criteria.JoinType.LEFT);
            jakarta.persistence.criteria.Join<StockMovement, Warehouse> warehouse =
                    root.join("warehouse", jakarta.persistence.criteria.JoinType.LEFT);

            String normalizedKeyword = normalizeSearch(keyword);
            if (normalizedKeyword != null) {
                String pattern = "%" + normalizedKeyword + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(product.get("productName")), pattern),
                        cb.like(cb.lower(product.get("sku")), pattern),
                        cb.like(cb.lower(product.get("barcode")), pattern),
                        cb.like(cb.lower(root.get("referenceNo")), pattern)));
            }
            if (productId != null) {
                predicates.add(cb.equal(product.get("id"), productId));
            }
            if (warehouseId != null) {
                predicates.add(cb.equal(warehouse.get("id"), warehouseId));
            }
            if (movementType != null) {
                predicates.add(cb.equal(root.get("movementType"), movementType));
            }
            String normalizedReferenceType = normalizeSearch(referenceType);
            if (normalizedReferenceType != null) {
                predicates.add(cb.equal(cb.lower(root.get("referenceType")), normalizedReferenceType));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate.atStartOfDay()));
            }
            if (toDate != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), toDate.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Sort sortForStock(String sort, String direction) {
        String property = switch (sort == null ? "" : sort) {
            case "productName" -> "product.productName";
            case "sku" -> "product.sku";
            case "warehouseName" -> "warehouse.name";
            case "quantity" -> "quantity";
            case "reorderLevel" -> "reorderLevel";
            default -> "product.productName";
        };
        return Sort.by(sortDirection(direction), property).and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private Sort sortForMovement(String sort, String direction) {
        String property = switch (sort == null ? "" : sort) {
            case "productName" -> "product.productName";
            case "warehouseName" -> "warehouse.name";
            case "movementType" -> "movementType";
            case "quantityChange" -> "quantityChange";
            case "referenceType" -> "referenceType";
            case "createdAt" -> "createdAt";
            default -> "createdAt";
        };
        return Sort.by(sortDirection(direction), property).and(Sort.by(Sort.Direction.DESC, "id"));
    }

    private Sort.Direction sortDirection(String direction) {
        return "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safeSize(int size) {
        return size == 25 || size == 50 || size == 100 ? size : 10;
    }

    private String normalizeSearch(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
