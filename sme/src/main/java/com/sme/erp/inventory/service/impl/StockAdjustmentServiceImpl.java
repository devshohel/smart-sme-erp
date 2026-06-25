package com.sme.erp.inventory.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.inventory.dto.StockAdjustmentDTO;
import com.sme.erp.inventory.entity.StockAdjustment;
import com.sme.erp.inventory.enums.StockAdjustmentStatus;
import com.sme.erp.inventory.repository.StockAdjustmentRepository;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.inventory.service.StockAdjustmentService;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockAdjustmentServiceImpl implements StockAdjustmentService {
    private final StockAdjustmentRepository repository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;

    public StockAdjustmentServiceImpl(StockAdjustmentRepository repository,
                                      ProductRepository productRepository,
                                      WarehouseRepository warehouseRepository,
                                      StockService stockService) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockService = stockService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockAdjustmentDTO> getAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StockAdjustmentDTO getById(Long id) {
        return toDto(findById(id));
    }

    @Override
    @Transactional
    public StockAdjustmentDTO create(Long productId, Long warehouseId, BigDecimal quantity, String reason) {
        validate(productId, warehouseId, quantity, reason);
        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setProduct(productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId)));
        adjustment.setWarehouse(warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId)));
        adjustment.setQuantity(quantity);
        adjustment.setReason(reason.trim());
        adjustment.setStatus(StockAdjustmentStatus.DRAFT);
        return toDto(repository.save(adjustment));
    }

    @Override
    @Transactional
    public StockAdjustmentDTO approve(Long id) {
        StockAdjustment adjustment = findById(id);
        requireStatus(adjustment, StockAdjustmentStatus.DRAFT);
        adjustment.setStatus(StockAdjustmentStatus.APPROVED);
        adjustment.setApprovedAt(LocalDateTime.now());
        adjustment.setApprovedBy(currentUsername());
        return toDto(repository.save(adjustment));
    }

    @Override
    @Transactional
    public StockAdjustmentDTO post(Long id) {
        StockAdjustment adjustment = findById(id);
        requireStatus(adjustment, StockAdjustmentStatus.APPROVED);
        stockService.adjustStock(adjustment.getProduct().getId(), adjustment.getWarehouse().getId(),
                adjustment.getQuantity(), adjustment.getReason());
        adjustment.setStatus(StockAdjustmentStatus.POSTED);
        adjustment.setPostedAt(LocalDateTime.now());
        adjustment.setPostedBy(currentUsername());
        return toDto(repository.save(adjustment));
    }

    @Override
    @Transactional
    public StockAdjustmentDTO cancel(Long id) {
        StockAdjustment adjustment = findById(id);
        requireStatus(adjustment, StockAdjustmentStatus.DRAFT, StockAdjustmentStatus.APPROVED);
        adjustment.setStatus(StockAdjustmentStatus.CANCELLED);
        adjustment.setCancelledAt(LocalDateTime.now());
        adjustment.setCancelledBy(currentUsername());
        return toDto(repository.save(adjustment));
    }

    @Override
    @Transactional
    public StockAdjustmentDTO reverse(Long id, String reversalReason) {
        StockAdjustment adjustment = findById(id);
        requireStatus(adjustment, StockAdjustmentStatus.POSTED);
        String reason = requiredReason(reversalReason);
        stockService.adjustStock(adjustment.getProduct().getId(), adjustment.getWarehouse().getId(),
                adjustment.getQuantity().negate(), "REVERSAL: " + reason);
        adjustment.setStatus(StockAdjustmentStatus.REVERSED);
        adjustment.setReversedAt(LocalDateTime.now());
        adjustment.setReversedBy(currentUsername());
        adjustment.setReversalReason(reason);
        return toDto(repository.save(adjustment));
    }

    private StockAdjustment findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock adjustment not found with id: " + id));
    }

    private void validate(Long productId, Long warehouseId, BigDecimal quantity, String reason) {
        if (productId == null || productId <= 0) throw new BadRequestException("Product id must be positive");
        if (warehouseId == null || warehouseId <= 0) throw new BadRequestException("Warehouse id must be positive");
        if (quantity == null || quantity.signum() == 0) throw new BadRequestException("Quantity must not be zero");
        requiredReason(reason);
    }

    private void requireStatus(StockAdjustment adjustment, StockAdjustmentStatus... allowed) {
        for (StockAdjustmentStatus status : allowed) {
            if (adjustment.getStatus() == status) return;
        }
        throw new BadRequestException("Action is not allowed for stock adjustment status: " + adjustment.getStatus());
    }

    private String requiredReason(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("Reason is required");
        }
        return value.trim();
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    private StockAdjustmentDTO toDto(StockAdjustment adjustment) {
        StockAdjustmentDTO dto = new StockAdjustmentDTO();
        dto.setId(adjustment.getId());
        if (adjustment.getProduct() != null) {
            dto.setProductId(adjustment.getProduct().getId());
            dto.setProductName(adjustment.getProduct().getProductName());
        }
        if (adjustment.getWarehouse() != null) {
            dto.setWarehouseId(adjustment.getWarehouse().getId());
            dto.setWarehouseName(adjustment.getWarehouse().getName());
        }
        dto.setQuantity(adjustment.getQuantity());
        dto.setReason(adjustment.getReason());
        dto.setNote(adjustment.getNote());
        dto.setStatus(adjustment.getStatus());
        dto.setCreatedAt(adjustment.getCreatedAt());
        dto.setApprovedAt(adjustment.getApprovedAt());
        dto.setApprovedBy(adjustment.getApprovedBy());
        dto.setPostedAt(adjustment.getPostedAt());
        dto.setPostedBy(adjustment.getPostedBy());
        dto.setCancelledAt(adjustment.getCancelledAt());
        dto.setCancelledBy(adjustment.getCancelledBy());
        dto.setReversedAt(adjustment.getReversedAt());
        dto.setReversedBy(adjustment.getReversedBy());
        dto.setReversalReason(adjustment.getReversalReason());
        return dto;
    }
}
