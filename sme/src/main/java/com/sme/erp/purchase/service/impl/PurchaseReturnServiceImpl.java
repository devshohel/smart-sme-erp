package com.sme.erp.purchase.service.impl;

import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.purchase.dto.PurchaseReturnDTO;
import com.sme.erp.purchase.dto.PurchaseReturnItemDTO;
import com.sme.erp.purchase.entity.PurchaseItem;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.entity.PurchaseReturn;
import com.sme.erp.purchase.entity.PurchaseReturnItem;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.mapper.PurchaseReturnMapper;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.repository.PurchaseReturnRepository;
import com.sme.erp.purchase.service.PurchaseReturnService;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.repository.SupplierRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseReturnServiceImpl implements PurchaseReturnService {

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PurchaseReturnMapper purchaseReturnMapper;
    private final StockService stockService;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private final AccountingPostingService accountingPostingService;

    public PurchaseReturnServiceImpl(
            PurchaseReturnRepository purchaseReturnRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            PurchaseReturnMapper purchaseReturnMapper,
            StockService stockService,
            ActivityLogService activityLogService,
            AuditLogService auditLogService,
            AccountingPostingService accountingPostingService) {
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.purchaseReturnMapper = purchaseReturnMapper;
        this.stockService = stockService;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.accountingPostingService = accountingPostingService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseReturnDTO> getAll() {
        return purchaseReturnRepository.findAll().stream()
                .map(purchaseReturnMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseReturnDTO getById(Long id) {
        return purchaseReturnMapper.toDTO(findReturnById(id));
    }

    @Override
    @Transactional
    public PurchaseReturnDTO create(PurchaseReturnDTO dto) {
        PurchaseReturn entity = new PurchaseReturn();
        PurchaseReturnDTO saved = save(dto, entity, true);
        activityLogService.log("PURCHASE_RETURN_CREATE", "PURCHASE", "purchase_returns", saved.getId(), "Created purchase return " + saved.getReturnCode());
        auditLogService.log("purchase_returns", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseReturnDTO update(Long id, PurchaseReturnDTO dto) {
        PurchaseReturn entity = findReturnById(id);
        ensureEditable(entity);
        PurchaseReturnDTO oldData = purchaseReturnMapper.toDTO(entity);
        PurchaseReturnDTO saved = save(dto, entity, false);
        activityLogService.log("PURCHASE_RETURN_UPDATE", "PURCHASE", "purchase_returns", saved.getId(), "Updated purchase return " + saved.getReturnCode());
        auditLogService.log("purchase_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseReturnDTO submit(Long id) {
        PurchaseReturn entity = findReturnById(id);
        PurchaseStatus status = normalizeStatus(entity.getStatus());
        if (status != PurchaseStatus.DRAFT && status != PurchaseStatus.REJECTED) {
            throw new BadRequestException("Only draft or rejected purchase returns can be submitted");
        }
        PurchaseReturnDTO oldData = purchaseReturnMapper.toDTO(entity);
        entity.setStatus(PurchaseStatus.SUBMITTED);
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setSubmittedBy(currentUsername());
        PurchaseReturnDTO saved = purchaseReturnMapper.toDTO(purchaseReturnRepository.save(entity));
        activityLogService.log("PURCHASE_RETURN_SUBMIT", "PURCHASE", "purchase_returns", saved.getId(), "Submitted purchase return " + saved.getReturnCode());
        auditLogService.log("purchase_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "SUBMIT");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseReturnDTO approve(Long id) {
        PurchaseReturn entity = findReturnById(id);
        if (normalizeStatus(entity.getStatus()) != PurchaseStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted purchase returns can be approved");
        }
        PurchaseReturnDTO oldData = purchaseReturnMapper.toDTO(entity);
        entity.setStatus(PurchaseStatus.APPROVED);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setApprovedBy(currentUsername());
        PurchaseReturnDTO saved = purchaseReturnMapper.toDTO(purchaseReturnRepository.save(entity));
        activityLogService.log("PURCHASE_RETURN_APPROVE", "PURCHASE", "purchase_returns", saved.getId(), "Approved purchase return " + saved.getReturnCode());
        auditLogService.log("purchase_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "APPROVE");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseReturnDTO reject(Long id, String reason) {
        PurchaseReturn entity = findReturnById(id);
        if (normalizeStatus(entity.getStatus()) != PurchaseStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted purchase returns can be rejected");
        }
        PurchaseReturnDTO oldData = purchaseReturnMapper.toDTO(entity);
        entity.setStatus(PurchaseStatus.REJECTED);
        entity.setRejectedAt(LocalDateTime.now());
        entity.setRejectedBy(currentUsername());
        entity.setRejectionReason(RequestValueUtils.normalizeRequired(reason, "Rejection reason"));
        PurchaseReturnDTO saved = purchaseReturnMapper.toDTO(purchaseReturnRepository.save(entity));
        activityLogService.log("PURCHASE_RETURN_REJECT", "PURCHASE", "purchase_returns", saved.getId(), "Rejected purchase return " + saved.getReturnCode());
        auditLogService.log("purchase_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "REJECT");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseReturnDTO post(Long id) {
        PurchaseReturn entity = findReturnById(id);
        if (normalizeStatus(entity.getStatus()) != PurchaseStatus.APPROVED) {
            throw new BadRequestException("Only approved purchase returns can be posted");
        }
        PurchaseOrder purchaseOrder = entity.getPurchase();
        if (!isReceivedOrSettled(purchaseOrder.getStatus())) {
            throw new BadRequestException("Purchase return can only be posted against a received purchase order");
        }
        validateReturnQuantities(entity);
        if (safe(entity.getTotalAmount()).compareTo(safe(purchaseOrder.getDueAmount())) > 0) {
            throw new BadRequestException("Posted return amount cannot exceed current purchase due until supplier credit memo support is added");
        }
        PurchaseReturnDTO oldData = purchaseReturnMapper.toDTO(entity);
        deductReturnedStock(entity);
        applyReturnToPurchase(entity);
        accountingPostingService.postPurchaseReturn(entity);
        entity.setStatus(PurchaseStatus.POSTED);
        entity.setPostedAt(LocalDateTime.now());
        entity.setPostedBy(currentUsername());
        PurchaseReturnDTO saved = purchaseReturnMapper.toDTO(purchaseReturnRepository.save(entity));
        activityLogService.log("PURCHASE_RETURN_POST", "PURCHASE", "purchase_returns", saved.getId(), "Posted purchase return " + saved.getReturnCode());
        auditLogService.log("purchase_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "POST");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseReturnDTO reverse(Long id, String reversalReason) {
        PurchaseReturn entity = findReturnById(id);
        PurchaseStatus status = normalizeStatus(entity.getStatus());
        if (status == PurchaseStatus.REVERSED) {
            throw new BadRequestException("Purchase return is already reversed");
        }
        if (status != PurchaseStatus.POSTED) {
            throw new BadRequestException("Only posted purchase returns can be reversed");
        }
        String normalizedReason = RequestValueUtils.normalizeRequired(reversalReason, "Reversal reason");
        PurchaseReturnDTO oldData = purchaseReturnMapper.toDTO(entity);
        restoreReturnedStock(entity);
        restorePurchaseAfterReturnReversal(entity);
        accountingPostingService.reversePurchaseReturn(entity, normalizedReason);
        entity.setStatus(PurchaseStatus.REVERSED);
        entity.setReversedAt(LocalDateTime.now());
        entity.setReversedBy(currentUsername());
        entity.setReversalReason(normalizedReason);
        PurchaseReturnDTO saved = purchaseReturnMapper.toDTO(purchaseReturnRepository.save(entity));
        activityLogService.log("PURCHASE_RETURN_REVERSE", "PURCHASE", "purchase_returns", saved.getId(), "Reversed purchase return " + saved.getReturnCode());
        auditLogService.log("purchase_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "REVERSE");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseReturnDTO cancel(Long id) {
        PurchaseReturn entity = findReturnById(id);
        PurchaseStatus status = normalizeStatus(entity.getStatus());
        if (status == PurchaseStatus.POSTED) {
            throw new BadRequestException("Posted purchase return cannot be cancelled");
        }
        if (status == PurchaseStatus.CANCELLED || status == PurchaseStatus.REVERSED) {
            throw new BadRequestException("Purchase return cannot be cancelled in its current status");
        }
        PurchaseReturnDTO oldData = purchaseReturnMapper.toDTO(entity);
        entity.setStatus(PurchaseStatus.CANCELLED);
        entity.setCancelledAt(LocalDateTime.now());
        entity.setCancelledBy(currentUsername());
        PurchaseReturnDTO saved = purchaseReturnMapper.toDTO(purchaseReturnRepository.save(entity));
        activityLogService.log("PURCHASE_RETURN_CANCEL", "PURCHASE", "purchase_returns", saved.getId(), "Cancelled purchase return " + saved.getReturnCode());
        auditLogService.log("purchase_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "CANCEL");
        return saved;
    }

    private PurchaseReturnDTO save(PurchaseReturnDTO dto, PurchaseReturn entity, boolean creating) {
        validateItems(dto.getItems());

        PurchaseOrder purchaseOrder = findPurchaseById(dto.getPurchaseId());
        Supplier supplier = findSupplierById(dto.getSupplierId());
        if (purchaseOrder.getSupplier() == null || !purchaseOrder.getSupplier().getId().equals(supplier.getId())) {
            throw new BadRequestException("Return supplier must match the purchase supplier");
        }

        entity.setReturnCode(resolveReturnCode(entity.getId(), RequestValueUtils.normalize(dto.getReturnCode())));
        entity.setPurchase(purchaseOrder);
        entity.setSupplier(supplier);
        entity.setReturnDate(dto.getReturnDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setNotes(RequestValueUtils.normalize(dto.getNotes()));
        entity.setStatus(resolveDraftStatus(dto.getStatus(), entity.getStatus(), creating));

        CalculationResult calculation = buildItems(entity, dto.getItems());
        entity.getItems().clear();
        entity.getItems().addAll(calculation.items());
        entity.setTotalAmount(calculation.totalAmount());

        return purchaseReturnMapper.toDTO(purchaseReturnRepository.save(entity));
    }

    private void deductReturnedStock(PurchaseReturn purchaseReturn) {
        Long warehouseId = purchaseReturn.getPurchase().getWarehouse().getId();
        for (PurchaseReturnItem item : purchaseReturn.getItems()) {
            stockService.stockOut(
                    item.getProduct().getId(),
                    warehouseId,
                    item.getQuantity(),
                    "PURCHASE_RETURN",
                    purchaseReturn.getReturnCode());
        }
    }

    private void restoreReturnedStock(PurchaseReturn purchaseReturn) {
        Long warehouseId = purchaseReturn.getPurchase().getWarehouse().getId();
        String referenceNo = purchaseReturn.getReturnCode() + "-REV";
        for (PurchaseReturnItem item : purchaseReturn.getItems()) {
            stockService.stockIn(
                    item.getProduct().getId(),
                    warehouseId,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    "PURCHASE_RETURN_REVERSAL",
                    referenceNo);
        }
    }

    private void applyReturnToPurchase(PurchaseReturn purchaseReturn) {
        PurchaseOrder purchaseOrder = purchaseReturn.getPurchase();
        for (PurchaseReturnItem returnItem : purchaseReturn.getItems()) {
            PurchaseItem matchedItem = purchaseOrder.getItems().stream()
                    .filter(item -> item.getProduct() != null
                            && returnItem.getProduct() != null
                            && item.getProduct().getId().equals(returnItem.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Returned product does not belong to the purchase order"));
            matchedItem.setReturnedQuantity(safe(matchedItem.getReturnedQuantity()).add(safe(returnItem.getQuantity())));
        }
        BigDecimal newDue = safe(purchaseOrder.getDueAmount()).subtract(safe(purchaseReturn.getTotalAmount())).max(BigDecimal.ZERO);
        purchaseOrder.setDueAmount(newDue);
        purchaseOrder.setStatus(resolvePurchaseStatusAfterReturn(purchaseOrder));
        purchaseOrderRepository.save(purchaseOrder);
    }

    private void restorePurchaseAfterReturnReversal(PurchaseReturn purchaseReturn) {
        PurchaseOrder purchaseOrder = purchaseReturn.getPurchase();
        for (PurchaseReturnItem returnItem : purchaseReturn.getItems()) {
            PurchaseItem matchedItem = purchaseOrder.getItems().stream()
                    .filter(item -> item.getProduct() != null
                            && returnItem.getProduct() != null
                            && item.getProduct().getId().equals(returnItem.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Returned product does not belong to the purchase order"));
            BigDecimal restoredReturnedQty = safe(matchedItem.getReturnedQuantity()).subtract(safe(returnItem.getQuantity())).max(BigDecimal.ZERO);
            matchedItem.setReturnedQuantity(restoredReturnedQty);
        }
        BigDecimal newDue = safe(purchaseOrder.getDueAmount()).add(safe(purchaseReturn.getTotalAmount()));
        purchaseOrder.setDueAmount(newDue);
        purchaseOrder.setStatus(resolvePurchaseStatusAfterReturn(purchaseOrder));
        purchaseOrderRepository.save(purchaseOrder);
    }

    private void validateReturnQuantities(PurchaseReturn purchaseReturn) {
        PurchaseOrder purchaseOrder = purchaseReturn.getPurchase();
        for (PurchaseReturnItem returnItem : purchaseReturn.getItems()) {
            PurchaseItem matchedItem = purchaseOrder.getItems().stream()
                    .filter(item -> item.getProduct() != null
                            && returnItem.getProduct() != null
                            && item.getProduct().getId().equals(returnItem.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Returned product does not belong to the purchase order"));
            BigDecimal returnQty = safe(returnItem.getQuantity());
            BigDecimal receivedQty = safe(matchedItem.getReceivedQuantity());
            if (returnQty.compareTo(receivedQty) > 0) {
                throw new BadRequestException("Return quantity cannot exceed received quantity");
            }
            BigDecimal remainingQty = receivedQty.subtract(safe(matchedItem.getReturnedQuantity()));
            if (returnQty.compareTo(remainingQty) > 0) {
                throw new BadRequestException("Return quantity cannot exceed remaining unreturned quantity");
            }
        }
    }

    private CalculationResult buildItems(PurchaseReturn purchaseReturn, List<PurchaseReturnItemDTO> itemDTOs) {
        List<PurchaseReturnItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PurchaseReturnItemDTO itemDTO : itemDTOs) {
            Product product = findProductById(itemDTO.getProductId());
            BigDecimal quantity = positive(itemDTO.getQuantity(), "Quantity must be positive");
            BigDecimal unitPrice = nonNegative(itemDTO.getUnitPrice(), "Unit price cannot be negative");
            BigDecimal total = quantity.multiply(unitPrice);
            PurchaseItem purchaseItem = purchaseReturn.getPurchase().getItems().stream()
                    .filter(item -> item.getProduct() != null && item.getProduct().getId().equals(itemDTO.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Return item product does not belong to the purchase order"));
            BigDecimal available = safe(purchaseItem.getReceivedQuantity()).subtract(safe(purchaseItem.getReturnedQuantity()));
            if (quantity.compareTo(available) > 0) {
                throw new BadRequestException("Return quantity cannot exceed received and unreturned quantity");
            }

            PurchaseReturnItem item = new PurchaseReturnItem();
            item.setReturnEntity(purchaseReturn);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setTotal(total);
            items.add(item);

            totalAmount = totalAmount.add(total);
        }

        return new CalculationResult(items, totalAmount);
    }

    private String resolveReturnCode(Long currentId, String requestedReturnCode) {
        if (requestedReturnCode != null) {
            boolean exists = currentId == null
                    ? purchaseReturnRepository.existsByReturnCode(requestedReturnCode)
                    : purchaseReturnRepository.existsByReturnCodeAndIdNot(requestedReturnCode, currentId);
            if (exists) {
                throw new DuplicateResourceException("Purchase return code already exists: " + requestedReturnCode);
            }
            return requestedReturnCode;
        }

        long nextNumber = purchaseReturnRepository.findTopByOrderByIdDesc()
                .map(entity -> entity.getId() + 1)
                .orElse(1L);
        String generated = String.format("PR-%04d", nextNumber);
        while (purchaseReturnRepository.existsByReturnCode(generated)) {
            nextNumber++;
            generated = String.format("PR-%04d", nextNumber);
        }
        return generated;
    }

    private void validateItems(List<PurchaseReturnItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("At least one return item is required");
        }
    }

    private void ensureEditable(PurchaseReturn purchaseReturn) {
        PurchaseStatus status = normalizeStatus(purchaseReturn.getStatus());
        if (status != PurchaseStatus.DRAFT && status != PurchaseStatus.REJECTED) {
            throw new BadRequestException("Only draft or rejected purchase returns can be edited");
        }
    }

    private PurchaseStatus resolveDraftStatus(PurchaseStatus requested, PurchaseStatus existing, boolean creating) {
        PurchaseStatus normalizedRequested = normalizeStatus(requested);
        if (normalizedRequested == null) {
            return creating ? PurchaseStatus.DRAFT : normalizeStatus(existing);
        }
        if (normalizedRequested != PurchaseStatus.DRAFT && normalizedRequested != PurchaseStatus.REJECTED) {
            throw new BadRequestException("Purchase return save supports draft status only");
        }
        return PurchaseStatus.DRAFT;
    }

    private PurchaseStatus normalizeStatus(PurchaseStatus status) {
        if (status == null) {
            return PurchaseStatus.DRAFT;
        }
        if (status == PurchaseStatus.PENDING) {
            return PurchaseStatus.SUBMITTED;
        }
        return status;
    }

    private boolean isReceivedOrSettled(PurchaseStatus status) {
        PurchaseStatus normalized = normalizeStatus(status);
        return normalized == PurchaseStatus.RECEIVED
                || normalized == PurchaseStatus.PARTIAL_PAID
                || normalized == PurchaseStatus.PAID;
    }

    private PurchaseStatus resolvePurchaseStatusAfterReturn(PurchaseOrder purchaseOrder) {
        BigDecimal due = safe(purchaseOrder.getDueAmount());
        BigDecimal paid = safe(purchaseOrder.getPaidAmount());
        if (due.signum() <= 0) {
            return paid.signum() > 0 ? PurchaseStatus.PAID : PurchaseStatus.RECEIVED;
        }
        if (paid.signum() > 0) {
            return PurchaseStatus.PARTIAL_PAID;
        }
        return PurchaseStatus.RECEIVED;
    }

    private PurchaseReturn findReturnById(Long id) {
        return purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase return not found with id: " + id));
    }

    private PurchaseOrder findPurchaseById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }

    private Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private BigDecimal nonNegative(BigDecimal value, String message) {
        BigDecimal normalized = value != null ? value : BigDecimal.ZERO;
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(message);
        }
        return normalized;
    }

    private BigDecimal positive(BigDecimal value, String message) {
        BigDecimal normalized = value != null ? value : BigDecimal.ZERO;
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(message);
        }
        return normalized;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    private record CalculationResult(List<PurchaseReturnItem> items, BigDecimal totalAmount) {
    }
}
