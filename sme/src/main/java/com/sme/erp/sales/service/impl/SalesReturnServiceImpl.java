package com.sme.erp.sales.service.impl;

import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.sales.dto.SalesReturnDTO;
import com.sme.erp.sales.dto.SalesReturnItemDTO;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.entity.SalesReturn;
import com.sme.erp.sales.entity.SalesReturnItem;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.enums.SalesPaymentStatus;
import com.sme.erp.sales.enums.SalesReturnStatus;
import com.sme.erp.sales.mapper.SalesReturnMapper;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesReturnRepository;
import com.sme.erp.sales.service.SalesReturnService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalesReturnServiceImpl implements SalesReturnService {

    private final SalesReturnRepository salesReturnRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SalesReturnMapper salesReturnMapper;
    private final StockService stockService;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private final AccountingPostingService accountingPostingService;

    public SalesReturnServiceImpl(
            SalesReturnRepository salesReturnRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            SalesReturnMapper salesReturnMapper,
            StockService stockService,
            ActivityLogService activityLogService,
            AuditLogService auditLogService,
            AccountingPostingService accountingPostingService) {
        this.salesReturnRepository = salesReturnRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.salesReturnMapper = salesReturnMapper;
        this.stockService = stockService;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.accountingPostingService = accountingPostingService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesReturnDTO> getAll() {
        return salesReturnRepository.findAll().stream()
                .map(salesReturnMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReturnDTO getById(Long id) {
        return salesReturnMapper.toDTO(findReturnById(id));
    }

    @Override
    @Transactional
    public SalesReturnDTO create(SalesReturnDTO dto) {
        SalesReturn entity = new SalesReturn();
        SalesReturnDTO saved = save(dto, entity, true);
        activityLogService.log("SALES_RETURN_CREATE", "SALES", "sales_returns", saved.getId(), "Created sales return " + saved.getReturnCode());
        auditLogService.log("sales_returns", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public SalesReturnDTO update(Long id, SalesReturnDTO dto) {
        SalesReturn entity = findReturnById(id);
        ensureEditable(entity);
        SalesReturnDTO oldData = salesReturnMapper.toDTO(entity);
        SalesReturnDTO saved = save(dto, entity, false);
        activityLogService.log("SALES_RETURN_UPDATE", "SALES", "sales_returns", saved.getId(), "Updated sales return " + saved.getReturnCode());
        auditLogService.log("sales_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public SalesReturnDTO submit(Long id) {
        SalesReturn entity = findReturnById(id);
        SalesReturnStatus normalizedStatus = normalizeStatus(entity.getStatus());
        if (normalizedStatus != SalesReturnStatus.DRAFT && normalizedStatus != SalesReturnStatus.REJECTED) {
            throw new BadRequestException("Only draft or rejected sales returns can be submitted");
        }
        SalesReturnDTO oldData = salesReturnMapper.toDTO(entity);
        entity.setStatus(SalesReturnStatus.SUBMITTED);
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setSubmittedBy(currentUsername());
        SalesReturnDTO saved = salesReturnMapper.toDTO(salesReturnRepository.save(entity));
        activityLogService.log("SALES_RETURN_SUBMIT", "SALES", "sales_returns", saved.getId(), "Submitted sales return " + saved.getReturnCode());
        auditLogService.log("sales_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "SUBMIT");
        return saved;
    }

    @Override
    @Transactional
    public SalesReturnDTO approve(Long id) {
        SalesReturn entity = findReturnById(id);
        if (normalizeStatus(entity.getStatus()) != SalesReturnStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted sales returns can be approved");
        }
        SalesReturnDTO oldData = salesReturnMapper.toDTO(entity);
        entity.setStatus(SalesReturnStatus.APPROVED);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setApprovedBy(currentUsername());
        SalesReturnDTO saved = salesReturnMapper.toDTO(salesReturnRepository.save(entity));
        activityLogService.log("SALES_RETURN_APPROVE", "SALES", "sales_returns", saved.getId(), "Approved sales return " + saved.getReturnCode());
        auditLogService.log("sales_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "APPROVE");
        return saved;
    }

    @Override
    @Transactional
    public SalesReturnDTO reject(Long id, String reason) {
        SalesReturn entity = findReturnById(id);
        if (normalizeStatus(entity.getStatus()) != SalesReturnStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted sales returns can be rejected");
        }
        SalesReturnDTO oldData = salesReturnMapper.toDTO(entity);
        entity.setStatus(SalesReturnStatus.REJECTED);
        entity.setRejectedAt(LocalDateTime.now());
        entity.setRejectedBy(currentUsername());
        entity.setRejectionReason(RequestValueUtils.normalizeRequired(reason, "Rejection reason"));
        SalesReturnDTO saved = salesReturnMapper.toDTO(salesReturnRepository.save(entity));
        activityLogService.log("SALES_RETURN_REJECT", "SALES", "sales_returns", saved.getId(), "Rejected sales return " + saved.getReturnCode());
        auditLogService.log("sales_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "REJECT");
        return saved;
    }

    @Override
    @Transactional
    public SalesReturnDTO post(Long id) {
        SalesReturn entity = findReturnById(id);
        if (normalizeStatus(entity.getStatus()) != SalesReturnStatus.APPROVED) {
            throw new BadRequestException("Only approved sales returns can be posted");
        }
        SalesInvoice invoice = entity.getInvoice();
        if (invoice == null || !isPostedInvoice(invoice.getStatus())) {
            throw new BadRequestException("Sales return can only be posted against a posted sales invoice");
        }
        validateReturnQuantities(entity);
        if (safe(entity.getTotalAmount()).compareTo(safe(invoice.getDueAmount())) > 0) {
            throw new BadRequestException("Posted return amount cannot exceed current invoice due until credit memo support is added");
        }
        SalesReturnDTO oldData = salesReturnMapper.toDTO(entity);
        restockReturnedItems(entity);
        accountingPostingService.postSalesReturn(entity);
        applyReturnToInvoice(invoice, entity.getTotalAmount());
        entity.setStatus(SalesReturnStatus.POSTED);
        entity.setPostedAt(LocalDateTime.now());
        entity.setPostedBy(currentUsername());
        SalesReturnDTO saved = salesReturnMapper.toDTO(salesReturnRepository.save(entity));
        activityLogService.log("SALES_RETURN_POST", "SALES", "sales_returns", saved.getId(), "Posted sales return " + saved.getReturnCode());
        auditLogService.log("sales_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "POST");
        return saved;
    }

    @Override
    @Transactional
    public SalesReturnDTO reverse(Long id, String reversalReason) {
        SalesReturn entity = findReturnById(id);
        SalesReturnStatus normalizedStatus = normalizeStatus(entity.getStatus());
        if (normalizedStatus == SalesReturnStatus.REVERSED) {
            throw new BadRequestException("Sales return is already reversed");
        }
        if (normalizedStatus != SalesReturnStatus.POSTED) {
            throw new BadRequestException("Only posted sales returns can be reversed");
        }
        String normalizedReason = RequestValueUtils.normalizeRequired(reversalReason, "Reversal reason");
        SalesReturnDTO oldData = salesReturnMapper.toDTO(entity);
        reverseReturnedStock(entity);
        accountingPostingService.reverseSalesReturn(entity, normalizedReason);
        restoreInvoiceAfterReturnReversal(entity.getInvoice(), entity.getTotalAmount());
        entity.setStatus(SalesReturnStatus.REVERSED);
        entity.setReversedAt(LocalDateTime.now());
        entity.setReversedBy(currentUsername());
        entity.setReversalReason(normalizedReason);
        SalesReturnDTO saved = salesReturnMapper.toDTO(salesReturnRepository.save(entity));
        activityLogService.log("SALES_RETURN_REVERSE", "SALES", "sales_returns", saved.getId(), "Reversed sales return " + saved.getReturnCode());
        auditLogService.log("sales_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "REVERSE");
        return saved;
    }

    @Override
    @Transactional
    public SalesReturnDTO cancel(Long id) {
        SalesReturn entity = findReturnById(id);
        SalesReturnStatus normalizedStatus = normalizeStatus(entity.getStatus());
        if (normalizedStatus == SalesReturnStatus.POSTED) {
            throw new BadRequestException("Posted sales return cannot be cancelled");
        }
        if (normalizedStatus == SalesReturnStatus.CANCELLED || normalizedStatus == SalesReturnStatus.REVERSED) {
            throw new BadRequestException("Sales return cannot be cancelled in its current status");
        }
        SalesReturnDTO oldData = salesReturnMapper.toDTO(entity);
        entity.setStatus(SalesReturnStatus.CANCELLED);
        entity.setCancelledAt(LocalDateTime.now());
        entity.setCancelledBy(currentUsername());
        SalesReturnDTO saved = salesReturnMapper.toDTO(salesReturnRepository.save(entity));
        activityLogService.log("SALES_RETURN_CANCEL", "SALES", "sales_returns", saved.getId(), "Cancelled sales return " + saved.getReturnCode());
        auditLogService.log("sales_returns", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "CANCEL");
        return saved;
    }

    private SalesReturnDTO save(SalesReturnDTO dto, SalesReturn entity, boolean creating) {
        validateItems(dto.getItems());

        SalesInvoice invoice = findInvoiceById(dto.getInvoiceId());
        Customer customer = findCustomerById(dto.getCustomerId());
        if (!invoice.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Return customer must match the invoice customer");
        }

        entity.setReturnCode(resolveReturnCode(entity.getId(), RequestValueUtils.normalize(dto.getReturnCode())));
        entity.setInvoice(invoice);
        entity.setCustomer(customer);
        entity.setReturnDate(dto.getReturnDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setNotes(RequestValueUtils.normalize(dto.getNotes()));
        entity.setStatus(resolveDraftStatus(dto.getStatus(), entity.getStatus(), creating));

        CalculationResult calculation = buildItems(entity, dto.getItems());
        entity.getItems().clear();
        entity.getItems().addAll(calculation.items());
        entity.setTotalAmount(calculation.totalAmount());
        return salesReturnMapper.toDTO(salesReturnRepository.save(entity));
    }

    private void restockReturnedItems(SalesReturn salesReturn) {
        Long warehouseId = salesReturn.getInvoice().getWarehouse().getId();
        for (SalesReturnItem item : salesReturn.getItems()) {
            Product product = requireProduct(item, "Sales return contains a deleted product. Edit the return and select an active product before posting.");
            stockService.stockIn(
                    product.getId(),
                    warehouseId,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    "SALES_RETURN",
                    salesReturn.getReturnCode());
        }
    }

    private void reverseReturnedStock(SalesReturn salesReturn) {
        Long warehouseId = salesReturn.getInvoice().getWarehouse().getId();
        String referenceNo = salesReturn.getReturnCode() + "-REV";
        for (SalesReturnItem item : salesReturn.getItems()) {
            Product product = requireProduct(item, "Sales return contains a deleted product. Stock reversal cannot continue.");
            stockService.stockOut(
                    product.getId(),
                    warehouseId,
                    item.getQuantity(),
                    "SALES_RETURN_REVERSAL",
                    referenceNo);
        }
    }

    private void applyReturnToInvoice(SalesInvoice invoice, BigDecimal returnAmount) {
        BigDecimal newDue = safe(invoice.getDueAmount()).subtract(safe(returnAmount)).max(BigDecimal.ZERO);
        invoice.setDueAmount(newDue);
        invoice.setPaymentStatus(resolvePaymentStatus(invoice.getPaidAmount(), newDue));
        invoice.setStatus(resolveInvoiceStatus(invoice));
        salesInvoiceRepository.save(invoice);
    }

    private void restoreInvoiceAfterReturnReversal(SalesInvoice invoice, BigDecimal returnAmount) {
        BigDecimal newDue = safe(invoice.getDueAmount()).add(safe(returnAmount));
        invoice.setDueAmount(newDue);
        invoice.setPaymentStatus(resolvePaymentStatus(invoice.getPaidAmount(), newDue));
        invoice.setStatus(resolveInvoiceStatus(invoice));
        salesInvoiceRepository.save(invoice);
    }

    private CalculationResult buildItems(SalesReturn salesReturn, List<SalesReturnItemDTO> itemDTOs) {
        List<SalesReturnItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Long, BigDecimal> soldQtyByProduct = invoiceSoldQtyByProduct(salesReturn.getInvoice());

        for (SalesReturnItemDTO itemDTO : itemDTOs) {
            Product product = findProductById(itemDTO.getProductId());
            BigDecimal quantity = positive(itemDTO.getQuantity(), "Quantity must be positive");
            BigDecimal unitPrice = nonNegative(itemDTO.getUnitPrice(), "Unit price cannot be negative");
            BigDecimal total = quantity.multiply(unitPrice);
            BigDecimal soldQty = soldQtyByProduct.get(itemDTO.getProductId());
            if (soldQty == null) {
                throw new BadRequestException("Returned product does not belong to the invoice");
            }
            if (quantity.compareTo(soldQty) > 0) {
                throw new BadRequestException("Return quantity cannot exceed sold quantity");
            }

            SalesReturnItem item = new SalesReturnItem();
            item.setReturnEntity(salesReturn);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setTotal(total);
            items.add(item);

            totalAmount = totalAmount.add(total);
        }

        return new CalculationResult(items, totalAmount);
    }

    private void validateReturnQuantities(SalesReturn salesReturn) {
        SalesInvoice invoice = salesReturn.getInvoice();
        Map<Long, BigDecimal> soldQtyByProduct = invoiceSoldQtyByProduct(invoice);
        Map<Long, BigDecimal> currentReturnQty = salesReturn.getItems().stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getId() != null)
                .collect(Collectors.toMap(
                        item -> item.getProduct().getId(),
                        item -> safe(item.getQuantity()),
                        BigDecimal::add));
        Map<Long, BigDecimal> previouslyReturnedQty = salesReturnRepository
                .findPostedByInvoiceIdExcluding(invoice.getId(), salesReturn.getId())
                .stream()
                .flatMap(previousReturn -> previousReturn.getItems().stream())
                .filter(item -> item.getProduct() != null && item.getProduct().getId() != null)
                .collect(Collectors.toMap(
                        item -> item.getProduct().getId(),
                        item -> safe(item.getQuantity()),
                        BigDecimal::add));

        for (Map.Entry<Long, BigDecimal> entry : currentReturnQty.entrySet()) {
            BigDecimal soldQty = soldQtyByProduct.get(entry.getKey());
            if (soldQty == null) {
                throw new BadRequestException("Returned product does not belong to the invoice");
            }
            BigDecimal requestedQty = entry.getValue();
            if (requestedQty.compareTo(soldQty) > 0) {
                throw new BadRequestException("Return quantity cannot exceed sold quantity");
            }
            BigDecimal remainingQty = soldQty.subtract(previouslyReturnedQty.getOrDefault(entry.getKey(), BigDecimal.ZERO));
            if (requestedQty.compareTo(remainingQty) > 0) {
                throw new BadRequestException("Return quantity cannot exceed remaining unreturned quantity");
            }
        }
    }

    private Map<Long, BigDecimal> invoiceSoldQtyByProduct(SalesInvoice invoice) {
        return invoice.getItems().stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getId() != null)
                .collect(Collectors.toMap(
                        item -> item.getProduct().getId(),
                        item -> safe(item.getQuantity()),
                        BigDecimal::add));
    }

    private String resolveReturnCode(Long currentId, String requestedReturnCode) {
        if (requestedReturnCode != null) {
            boolean exists = currentId == null
                    ? salesReturnRepository.existsByReturnCode(requestedReturnCode)
                    : salesReturnRepository.existsByReturnCodeAndIdNot(requestedReturnCode, currentId);
            if (exists) {
                throw new DuplicateResourceException("Sales return code already exists: " + requestedReturnCode);
            }
            return requestedReturnCode;
        }

        long nextNumber = salesReturnRepository.findTopByOrderByIdDesc()
                .map(entity -> entity.getId() + 1)
                .orElse(1L);
        String generated = String.format("SR-%04d", nextNumber);
        while (salesReturnRepository.existsByReturnCode(generated)) {
            nextNumber++;
            generated = String.format("SR-%04d", nextNumber);
        }
        return generated;
    }

    private void validateItems(List<SalesReturnItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("At least one return item is required");
        }
    }

    private void ensureEditable(SalesReturn salesReturn) {
        SalesReturnStatus normalizedStatus = normalizeStatus(salesReturn.getStatus());
        if (normalizedStatus != SalesReturnStatus.DRAFT && normalizedStatus != SalesReturnStatus.REJECTED) {
            throw new BadRequestException("Only draft or rejected sales returns can be edited");
        }
    }

    private SalesReturnStatus resolveDraftStatus(SalesReturnStatus requested, SalesReturnStatus existing, boolean creating) {
        SalesReturnStatus normalizedRequested = normalizeStatus(requested);
        if (normalizedRequested == null) {
            return creating ? SalesReturnStatus.DRAFT : normalizeStatus(existing);
        }
        if (normalizedRequested != SalesReturnStatus.DRAFT && normalizedRequested != SalesReturnStatus.REJECTED) {
            throw new BadRequestException("Sales return save supports draft status only");
        }
        return SalesReturnStatus.DRAFT;
    }

    private SalesReturnStatus normalizeStatus(SalesReturnStatus status) {
        return status == null ? SalesReturnStatus.DRAFT : status;
    }

    private boolean isPostedInvoice(SalesInvoiceStatus status) {
        SalesInvoiceStatus normalized = status == SalesInvoiceStatus.CONFIRMED || status == SalesInvoiceStatus.COMPLETED
                ? SalesInvoiceStatus.POSTED : status;
        return normalized == SalesInvoiceStatus.POSTED
                || normalized == SalesInvoiceStatus.PARTIAL_PAID
                || normalized == SalesInvoiceStatus.PAID;
    }

    private SalesPaymentStatus resolvePaymentStatus(BigDecimal paidAmount, BigDecimal dueAmount) {
        if (safe(dueAmount).signum() <= 0) {
            return SalesPaymentStatus.PAID;
        }
        if (safe(paidAmount).signum() > 0) {
            return SalesPaymentStatus.PARTIAL;
        }
        return SalesPaymentStatus.DUE;
    }

    private SalesInvoiceStatus resolveInvoiceStatus(SalesInvoice invoice) {
        if (safe(invoice.getDueAmount()).signum() <= 0) {
            return SalesInvoiceStatus.PAID;
        }
        if (safe(invoice.getPaidAmount()).signum() > 0) {
            return SalesInvoiceStatus.PARTIAL_PAID;
        }
        return SalesInvoiceStatus.POSTED;
    }

    private SalesReturn findReturnById(Long id) {
        return salesReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales return not found with id: " + id));
    }

    private SalesInvoice findInvoiceById(Long id) {
        return salesInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales invoice not found with id: " + id));
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private Product requireProduct(SalesReturnItem item, String message) {
        if (item == null || item.getProduct() == null) {
            throw new BadRequestException(message);
        }
        return item.getProduct();
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

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    private record CalculationResult(List<SalesReturnItem> items, BigDecimal totalAmount) {
    }
}
