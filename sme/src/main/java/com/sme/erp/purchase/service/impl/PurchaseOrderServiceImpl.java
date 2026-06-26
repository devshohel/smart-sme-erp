package com.sme.erp.purchase.service.impl;

import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.notification.enums.NotificationSeverity;
import com.sme.erp.notification.enums.NotificationType;
import com.sme.erp.notification.service.NotificationService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.entity.Uom;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.product.repository.UomRepository;
import com.sme.erp.purchase.dto.PurchaseItemDTO;
import com.sme.erp.purchase.dto.PurchaseOrderDTO;
import com.sme.erp.purchase.dto.PurchaseReceiveDTO;
import com.sme.erp.purchase.dto.PurchaseReceiveItemDTO;
import com.sme.erp.purchase.entity.GoodsReceiveItem;
import com.sme.erp.purchase.entity.GoodsReceiveNote;
import com.sme.erp.purchase.entity.PurchaseItem;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.enums.PurchaseReceiveStatus;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.mapper.PurchaseOrderMapper;
import com.sme.erp.purchase.repository.GoodsReceiveNoteRepository;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.service.PurchaseOrderService;
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
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UomRepository uomRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final GoodsReceiveNoteRepository goodsReceiveNoteRepository;
    private final StockService stockService;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private final AccountingPostingService accountingPostingService;
    private final NotificationService notificationService;

    public PurchaseOrderServiceImpl(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            UomRepository uomRepository,
            PurchaseOrderMapper purchaseOrderMapper,
            GoodsReceiveNoteRepository goodsReceiveNoteRepository,
            StockService stockService,
            ActivityLogService activityLogService,
            AuditLogService auditLogService,
            AccountingPostingService accountingPostingService,
            NotificationService notificationService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.uomRepository = uomRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.goodsReceiveNoteRepository = goodsReceiveNoteRepository;
        this.stockService = stockService;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.accountingPostingService = accountingPostingService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getAll() {
        return purchaseOrderRepository.findAll().stream()
                .map(purchaseOrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getUnpaidBySupplier(Long supplierId) {
        if (supplierId == null) {
            return List.of();
        }
        return purchaseOrderRepository.findUnpaidBySupplierIdOrderByPurchaseDateAscIdAsc(supplierId).stream()
                .map(purchaseOrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDTO getById(Long id) {
        return purchaseOrderMapper.toDTO(findPurchaseOrderById(id));
    }

    @Override
    @Transactional
    public PurchaseOrderDTO create(PurchaseOrderDTO dto) {
        PurchaseOrder entity = new PurchaseOrder();
        PurchaseOrderDTO saved = save(dto, entity);
        activityLogService.log("PURCHASE_CREATE", "PURCHASE", "purchase_orders", saved.getId(), "Created purchase order " + saved.getPurchaseCode());
        auditLogService.log("purchase_orders", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseOrderDTO update(Long id, PurchaseOrderDTO dto) {
        PurchaseOrder entity = findPurchaseOrderById(id);
        ensureEditable(entity);
        PurchaseOrderDTO oldData = purchaseOrderMapper.toDTO(entity);
        PurchaseOrderDTO saved = save(dto, entity);
        auditLogService.log("purchase_orders", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseOrderDTO submit(Long id) {
        PurchaseOrder entity = findPurchaseOrderById(id);
        PurchaseStatus status = normalizeStatus(entity.getStatus());
        if (status != PurchaseStatus.DRAFT && status != PurchaseStatus.REJECTED) {
            throw new BadRequestException("Only draft or rejected purchase orders can be submitted");
        }
        PurchaseOrderDTO oldData = purchaseOrderMapper.toDTO(entity);
        entity.setStatus(PurchaseStatus.SUBMITTED);
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setSubmittedBy(currentUsername());
        PurchaseOrderDTO saved = purchaseOrderMapper.toDTO(purchaseOrderRepository.save(entity));
        activityLogService.log("PURCHASE_ORDER_SUBMIT", "PURCHASE", "purchase_orders", saved.getId(), "Submitted purchase order " + saved.getPurchaseCode());
        auditLogService.log("purchase_orders", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "SUBMIT");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseOrderDTO approve(Long id) {
        PurchaseOrder entity = findPurchaseOrderById(id);
        if (normalizeStatus(entity.getStatus()) != PurchaseStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted purchase orders can be approved");
        }
        PurchaseOrderDTO oldData = purchaseOrderMapper.toDTO(entity);
        entity.setStatus(PurchaseStatus.APPROVED);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setApprovedBy(currentUsername());
        PurchaseOrderDTO saved = purchaseOrderMapper.toDTO(purchaseOrderRepository.save(entity));
        activityLogService.log("PURCHASE_ORDER_APPROVE", "PURCHASE", "purchase_orders", saved.getId(), "Approved purchase order " + saved.getPurchaseCode());
        auditLogService.log("purchase_orders", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "APPROVE");
        notificationService.notifyGlobal(
                "Purchase order approved",
                "Purchase order " + saved.getPurchaseCode() + " was approved.",
                NotificationType.INFO,
                NotificationSeverity.MEDIUM,
                "PURCHASE_ORDER",
                saved.getId(),
                "/purchases/orders/details/" + saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public PurchaseOrderDTO reject(Long id, String reason) {
        PurchaseOrder entity = findPurchaseOrderById(id);
        if (normalizeStatus(entity.getStatus()) != PurchaseStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted purchase orders can be rejected");
        }
        PurchaseOrderDTO oldData = purchaseOrderMapper.toDTO(entity);
        entity.setStatus(PurchaseStatus.REJECTED);
        entity.setRejectedAt(LocalDateTime.now());
        entity.setRejectedBy(currentUsername());
        entity.setRejectionReason(RequestValueUtils.normalizeRequired(reason, "Rejection reason"));
        PurchaseOrderDTO saved = purchaseOrderMapper.toDTO(purchaseOrderRepository.save(entity));
        activityLogService.log("PURCHASE_ORDER_REJECT", "PURCHASE", "purchase_orders", saved.getId(), "Rejected purchase order " + saved.getPurchaseCode());
        auditLogService.log("purchase_orders", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "REJECT");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseOrderDTO cancel(Long id) {
        PurchaseOrder entity = findPurchaseOrderById(id);
        PurchaseStatus status = normalizeStatus(entity.getStatus());
        if (status != PurchaseStatus.DRAFT && status != PurchaseStatus.SUBMITTED && status != PurchaseStatus.REJECTED) {
            throw new BadRequestException("Only draft or submitted purchase orders can be cancelled");
        }
        PurchaseOrderDTO oldData = purchaseOrderMapper.toDTO(entity);
        entity.setStatus(PurchaseStatus.CANCELLED);
        entity.setCancelledAt(LocalDateTime.now());
        entity.setCancelledBy(currentUsername());
        PurchaseOrderDTO saved = purchaseOrderMapper.toDTO(purchaseOrderRepository.save(entity));
        activityLogService.log("PURCHASE_ORDER_CANCEL", "PURCHASE", "purchase_orders", saved.getId(), "Cancelled purchase order " + saved.getPurchaseCode());
        auditLogService.log("purchase_orders", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "CANCEL");
        return saved;
    }

    @Override
    @Transactional
    public PurchaseOrderDTO receive(Long id, PurchaseReceiveDTO dto) {
        PurchaseOrder entity = findPurchaseOrderById(id);
        PurchaseStatus status = normalizeStatus(entity.getStatus());
        if (status != PurchaseStatus.APPROVED && status != PurchaseStatus.PARTIAL_RECEIVED) {
            throw new BadRequestException("Only approved or partially received purchase orders can receive goods");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BadRequestException("At least one receive item is required");
        }
        PurchaseOrderDTO oldData = purchaseOrderMapper.toDTO(entity);
        GoodsReceiveNote grn = new GoodsReceiveNote();
        grn.setGrnNo(resolveGrnNo());
        grn.setPurchaseOrder(entity);
        grn.setWarehouse(entity.getWarehouse());
        grn.setReceiveDate(dto.getReceiveDate());
        grn.setStatus(PurchaseReceiveStatus.POSTED);
        grn.setNotes(RequestValueUtils.normalize(dto.getNotes()));

        BigDecimal totalOrdered = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;
        List<GoodsReceiveItem> receiveItems = new ArrayList<>();
        for (PurchaseReceiveItemDTO itemDTO : dto.getItems()) {
            PurchaseItem purchaseItem = entity.getItems().stream()
                    .filter(item -> item.getProduct() != null && item.getProduct().getId().equals(itemDTO.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Receive item product does not belong to the purchase order"));
            BigDecimal receivedQty = positive(itemDTO.getReceivedQty(), "Received quantity must be positive");
            BigDecimal orderedQty = safe(purchaseItem.getQuantity());
            BigDecimal currentReceived = safe(purchaseItem.getReceivedQuantity());
            BigDecimal remaining = orderedQty.subtract(currentReceived);
            if (receivedQty.compareTo(remaining) > 0) {
                throw new BadRequestException("Received quantity cannot exceed remaining ordered quantity");
            }
            purchaseItem.setReceivedQuantity(currentReceived.add(receivedQty));
            stockService.stockIn(
                    purchaseItem.getProduct().getId(),
                    entity.getWarehouse().getId(),
                    receivedQty,
                    purchaseItem.getUnitPrice(),
                    "PURCHASE_RECEIVE",
                    entity.getPurchaseCode());

            GoodsReceiveItem grnItem = new GoodsReceiveItem();
            grnItem.setGoodsReceiveNote(grn);
            grnItem.setProduct(purchaseItem.getProduct());
            grnItem.setOrderedQty(orderedQty);
            grnItem.setReceivedQty(receivedQty);
            grnItem.setRemainingQty(remaining.subtract(receivedQty));
            receiveItems.add(grnItem);

            totalOrdered = totalOrdered.add(orderedQty);
            totalReceived = totalReceived.add(safe(purchaseItem.getReceivedQuantity()));
        }
        grn.getItems().clear();
        grn.getItems().addAll(receiveItems);
        goodsReceiveNoteRepository.save(grn);

        entity.setStatus(totalReceived.compareTo(totalOrdered) >= 0 ? PurchaseStatus.RECEIVED : PurchaseStatus.PARTIAL_RECEIVED);
        PurchaseOrder savedOrder = purchaseOrderRepository.save(entity);
        if (savedOrder.getStatus() == PurchaseStatus.RECEIVED) {
            accountingPostingService.postPurchase(savedOrder);
        }
        PurchaseOrderDTO saved = purchaseOrderMapper.toDTO(savedOrder);
        activityLogService.log("PURCHASE_ORDER_RECEIVE", "PURCHASE", "purchase_orders", saved.getId(), "Received goods for purchase order " + saved.getPurchaseCode());
        auditLogService.log("purchase_orders", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "RECEIVE");
        return saved;
    }

    private PurchaseOrderDTO save(PurchaseOrderDTO dto, PurchaseOrder entity) {
        validateItems(dto.getItems());

        String requestedPurchaseCode = RequestValueUtils.normalize(dto.getPurchaseCode());
        entity.setPurchaseCode(resolvePurchaseCode(entity.getId(), requestedPurchaseCode));
        entity.setSupplier(findSupplierById(dto.getSupplierId()));
        entity.setWarehouse(findWarehouseById(dto.getWarehouseId()));
        entity.setPurchaseDate(dto.getPurchaseDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setStatus(resolveDraftStatus(dto.getStatus(), entity.getStatus(), entity.getId() == null));

        CalculationResult calculation = buildItems(entity, dto.getItems());
        replaceItems(entity, calculation.items());
        entity.setTotalAmount(calculation.totalAmount());
        entity.setDiscountAmount(calculation.discountAmount());
        entity.setTaxAmount(calculation.taxAmount());
        entity.setNetTotal(calculation.netTotal());
        entity.setPaidAmount(BigDecimal.ZERO);
        entity.setDueAmount(calculation.netTotal());
        return purchaseOrderMapper.toDTO(purchaseOrderRepository.save(entity));
    }

    private void replaceItems(PurchaseOrder purchaseOrder, List<PurchaseItem> items) {
        purchaseOrder.getItems().clear();
        purchaseOrder.getItems().addAll(items);
    }

    private CalculationResult buildItems(PurchaseOrder purchaseOrder, List<PurchaseItemDTO> itemDTOs) {
        List<PurchaseItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        for (PurchaseItemDTO itemDTO : itemDTOs) {
            Product product = findProductById(itemDTO.getProductId());
            Uom uom = findUomById(itemDTO.getUomId());

            BigDecimal quantity = positive(itemDTO.getQuantity(), "Quantity must be positive");
            BigDecimal unitPrice = nonNegative(itemDTO.getUnitPrice(), "Unit price cannot be negative");
            BigDecimal discount = nonNegative(itemDTO.getDiscount(), "Discount cannot be negative");
            BigDecimal tax = nonNegative(itemDTO.getTax(), "Tax cannot be negative");
            BigDecimal gross = quantity.multiply(unitPrice);
            BigDecimal subTotal = gross.subtract(discount).add(tax);

            PurchaseItem item = new PurchaseItem();
            item.setPurchase(purchaseOrder);
            item.setProduct(product);
            item.setUom(uom);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setDiscount(discount);
            item.setTax(tax);
            item.setSubTotal(subTotal);
            item.setReceivedQuantity(purchaseOrder.getId() != null && itemDTO.getId() != null ? safe(findExistingItemReceivedQuantity(purchaseOrder, itemDTO.getId())) : BigDecimal.ZERO);
            item.setReturnedQuantity(purchaseOrder.getId() != null && itemDTO.getId() != null ? safe(findExistingItemReturnedQuantity(purchaseOrder, itemDTO.getId())) : BigDecimal.ZERO);
            items.add(item);

            totalAmount = totalAmount.add(gross);
            discountAmount = discountAmount.add(discount);
            taxAmount = taxAmount.add(tax);
        }

        BigDecimal netTotal = totalAmount.subtract(discountAmount).add(taxAmount);
        return new CalculationResult(items, totalAmount, discountAmount, taxAmount, netTotal);
    }

    private String resolvePurchaseCode(Long currentId, String requestedPurchaseCode) {
        if (requestedPurchaseCode != null) {
            validatePurchaseCodeUnique(requestedPurchaseCode, currentId);
            return requestedPurchaseCode;
        }

        if (currentId != null) {
            return findPurchaseOrderById(currentId).getPurchaseCode();
        }

        long nextNumber = purchaseOrderRepository.findTopByOrderByIdDesc()
                .map(order -> order.getId() + 1)
                .orElse(1L);
        String generated = String.format("PO-%04d", nextNumber);
        while (purchaseOrderRepository.existsByPurchaseCode(generated)) {
            nextNumber++;
            generated = String.format("PO-%04d", nextNumber);
        }
        return generated;
    }

    private void validatePurchaseCodeUnique(String purchaseCode, Long currentId) {
        boolean exists = currentId == null
                ? purchaseOrderRepository.existsByPurchaseCode(purchaseCode)
                : purchaseOrderRepository.existsByPurchaseCodeAndIdNot(purchaseCode, currentId);
        if (exists) {
            throw new DuplicateResourceException("Purchase code already exists: " + purchaseCode);
        }
    }

    private void validateItems(List<PurchaseItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("At least one purchase item is required");
        }
    }

    private void ensureEditable(PurchaseOrder entity) {
        PurchaseStatus status = normalizeStatus(entity.getStatus());
        if (status != PurchaseStatus.DRAFT && status != PurchaseStatus.REJECTED) {
            throw new BadRequestException("Only draft or rejected purchase orders can be edited");
        }
    }

    private PurchaseStatus resolveDraftStatus(PurchaseStatus requested, PurchaseStatus existing, boolean creating) {
        PurchaseStatus normalizedRequested = normalizeStatus(requested);
        if (normalizedRequested == null) {
            return creating ? PurchaseStatus.DRAFT : normalizeStatus(existing);
        }
        if (normalizedRequested != PurchaseStatus.DRAFT && normalizedRequested != PurchaseStatus.REJECTED) {
            throw new BadRequestException("Purchase order save supports draft status only");
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

    private BigDecimal findExistingItemReceivedQuantity(PurchaseOrder order, Long itemId) {
        return order.getItems().stream()
                .filter(item -> itemId.equals(item.getId()))
                .map(PurchaseItem::getReceivedQuantity)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal findExistingItemReturnedQuantity(PurchaseOrder order, Long itemId) {
        return order.getItems().stream()
                .filter(item -> itemId.equals(item.getId()))
                .map(PurchaseItem::getReturnedQuantity)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private PurchaseOrder findPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }

    private Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    private Warehouse findWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private Uom findUomById(Long id) {
        if (id == null) {
            return null;
        }
        return uomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UOM not found with id: " + id));
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal nonNegative(BigDecimal value, String message) {
        BigDecimal normalized = safe(value);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(message);
        }
        return normalized;
    }

    private BigDecimal positive(BigDecimal value, String message) {
        BigDecimal normalized = safe(value);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(message);
        }
        return normalized;
    }

    private String resolveGrnNo() {
        long nextNumber = goodsReceiveNoteRepository.findTopByOrderByIdDesc()
                .map(note -> note.getId() + 1)
                .orElse(1L);
        String generated = String.format("GRN-%04d", nextNumber);
        while (goodsReceiveNoteRepository.existsByGrnNo(generated)) {
            nextNumber++;
            generated = String.format("GRN-%04d", nextNumber);
        }
        return generated;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    private record CalculationResult(
            List<PurchaseItem> items,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal netTotal) {
    }
}
