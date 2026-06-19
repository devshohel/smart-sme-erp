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
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.entity.Uom;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.product.repository.UomRepository;
import com.sme.erp.sales.dto.SalesInvoiceDTO;
import com.sme.erp.sales.dto.SalesItemDTO;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.entity.SalesItem;
import com.sme.erp.sales.entity.SalesOrder;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.enums.SalesOrderStatus;
import com.sme.erp.sales.enums.SalesPaymentStatus;
import com.sme.erp.sales.enums.SalesReturnStatus;
import com.sme.erp.sales.mapper.SalesInvoiceMapper;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesOrderRepository;
import com.sme.erp.sales.repository.SalesReturnRepository;
import com.sme.erp.sales.service.SalesInvoiceService;
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
public class SalesInvoiceServiceImpl implements SalesInvoiceService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UomRepository uomRepository;
    private final SalesInvoiceMapper salesInvoiceMapper;
    private final SalesReturnRepository salesReturnRepository;
    private final StockService stockService;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private final AccountingPostingService accountingPostingService;

    public SalesInvoiceServiceImpl(
            SalesInvoiceRepository salesInvoiceRepository,
            SalesOrderRepository salesOrderRepository,
            CustomerRepository customerRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            UomRepository uomRepository,
            SalesInvoiceMapper salesInvoiceMapper,
            SalesReturnRepository salesReturnRepository,
            StockService stockService,
            ActivityLogService activityLogService,
            AuditLogService auditLogService,
            AccountingPostingService accountingPostingService) {
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.uomRepository = uomRepository;
        this.salesInvoiceMapper = salesInvoiceMapper;
        this.salesReturnRepository = salesReturnRepository;
        this.stockService = stockService;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.accountingPostingService = accountingPostingService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvoiceDTO> getAll() {
        return salesInvoiceRepository.findAll().stream()
                .map(salesInvoiceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvoiceDTO> getUnpaidByCustomerId(Long customerId) {
        return salesInvoiceRepository.findUnpaidByCustomerIdOrderBySaleDateAscIdAsc(customerId).stream()
                .map(salesInvoiceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SalesInvoiceDTO getById(Long id) {
        return salesInvoiceMapper.toDTO(findInvoiceById(id));
    }

    @Override
    @Transactional
    public SalesInvoiceDTO create(SalesInvoiceDTO dto) {
        SalesInvoice entity = new SalesInvoice();
        SalesInvoiceDTO saved = save(dto, entity, true);
        activityLogService.log("SALES_INVOICE_CREATE", "SALES", "sales_invoices", saved.getId(), "Created sales invoice " + saved.getInvoiceNo());
        auditLogService.log("sales_invoices", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public SalesInvoiceDTO update(Long id, SalesInvoiceDTO dto) {
        SalesInvoice entity = findInvoiceById(id);
        ensureEditable(entity);
        SalesInvoiceDTO oldData = salesInvoiceMapper.toDTO(entity);
        SalesInvoiceDTO saved = save(dto, entity, false);
        activityLogService.log("SALES_INVOICE_UPDATE", "SALES", "sales_invoices", saved.getId(), "Updated sales invoice " + saved.getInvoiceNo());
        auditLogService.log("sales_invoices", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public SalesInvoiceDTO submit(Long id) {
        SalesInvoice entity = findInvoiceById(id);
        if (normalizeStatus(entity.getStatus()) != SalesInvoiceStatus.DRAFT) {
            throw new BadRequestException("Only draft sales invoices can be submitted");
        }
        SalesInvoiceDTO oldData = salesInvoiceMapper.toDTO(entity);
        entity.setStatus(SalesInvoiceStatus.SUBMITTED);
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setSubmittedBy(currentUsername());
        SalesInvoiceDTO saved = salesInvoiceMapper.toDTO(salesInvoiceRepository.save(entity));
        activityLogService.log("SALES_INVOICE_SUBMIT", "SALES", "sales_invoices", saved.getId(), "Submitted sales invoice " + saved.getInvoiceNo());
        auditLogService.log("sales_invoices", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "SUBMIT");
        return saved;
    }

    @Override
    @Transactional
    public SalesInvoiceDTO approve(Long id) {
        SalesInvoice entity = findInvoiceById(id);
        if (normalizeStatus(entity.getStatus()) != SalesInvoiceStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted sales invoices can be approved");
        }
        SalesInvoiceDTO oldData = salesInvoiceMapper.toDTO(entity);
        entity.setStatus(SalesInvoiceStatus.APPROVED);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setApprovedBy(currentUsername());
        SalesInvoiceDTO saved = salesInvoiceMapper.toDTO(salesInvoiceRepository.save(entity));
        activityLogService.log("SALES_INVOICE_APPROVE", "SALES", "sales_invoices", saved.getId(), "Approved sales invoice " + saved.getInvoiceNo());
        auditLogService.log("sales_invoices", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "APPROVE");
        return saved;
    }

    @Override
    @Transactional
    public SalesInvoiceDTO post(Long id) {
        SalesInvoice entity = findInvoiceById(id);
        SalesInvoiceStatus normalizedStatus = normalizeStatus(entity.getStatus());
        if (normalizedStatus == SalesInvoiceStatus.POSTED || normalizedStatus == SalesInvoiceStatus.PARTIAL_PAID || normalizedStatus == SalesInvoiceStatus.PAID) {
            throw new BadRequestException("Sales invoice is already posted");
        }
        if (normalizedStatus != SalesInvoiceStatus.APPROVED) {
            throw new BadRequestException("Only approved sales invoices can be posted");
        }
        ensureCustomerCreditLimit(entity);
        deductStock(entity);
        accountingPostingService.postSalesInvoice(entity);
        SalesInvoiceDTO oldData = salesInvoiceMapper.toDTO(entity);
        entity.setStatus(resolvePostedStatus(entity));
        entity.setPostedAt(LocalDateTime.now());
        entity.setPostedBy(currentUsername());
        SalesInvoiceDTO saved = salesInvoiceMapper.toDTO(salesInvoiceRepository.save(entity));
        activityLogService.log("SALES_INVOICE_POST", "SALES", "sales_invoices", saved.getId(), "Posted sales invoice " + saved.getInvoiceNo());
        auditLogService.log("sales_invoices", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "POST");
        return saved;
    }

    @Override
    @Transactional
    public SalesInvoiceDTO cancel(Long id) {
        SalesInvoice entity = findInvoiceById(id);
        SalesInvoiceStatus normalizedStatus = normalizeStatus(entity.getStatus());
        if (normalizedStatus == SalesInvoiceStatus.POSTED || normalizedStatus == SalesInvoiceStatus.PARTIAL_PAID || normalizedStatus == SalesInvoiceStatus.PAID) {
            throw new BadRequestException("Posted sales invoice cannot be cancelled. Use reversal instead.");
        }
        if (normalizedStatus == SalesInvoiceStatus.CANCELLED || normalizedStatus == SalesInvoiceStatus.REVERSED) {
            throw new BadRequestException("Sales invoice cannot be cancelled in its current status");
        }
        SalesInvoiceDTO oldData = salesInvoiceMapper.toDTO(entity);
        entity.setStatus(SalesInvoiceStatus.CANCELLED);
        entity.setCancelledAt(LocalDateTime.now());
        entity.setCancelledBy(currentUsername());
        SalesInvoiceDTO saved = salesInvoiceMapper.toDTO(salesInvoiceRepository.save(entity));
        activityLogService.log("SALES_INVOICE_CANCEL", "SALES", "sales_invoices", saved.getId(), "Cancelled sales invoice " + saved.getInvoiceNo());
        auditLogService.log("sales_invoices", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "CANCEL");
        return saved;
    }

    @Override
    @Transactional
    public SalesInvoiceDTO reverse(Long id, String reversalReason) {
        SalesInvoice entity = findInvoiceById(id);
        SalesInvoiceStatus normalizedStatus = normalizeStatus(entity.getStatus());
        if (normalizedStatus != SalesInvoiceStatus.POSTED && normalizedStatus != SalesInvoiceStatus.PARTIAL_PAID && normalizedStatus != SalesInvoiceStatus.PAID) {
            throw new BadRequestException("Only posted sales invoices can be reversed");
        }
        if (normalizedStatus == SalesInvoiceStatus.REVERSED) {
            throw new BadRequestException("Sales invoice is already reversed");
        }
        if (safe(entity.getPaidAmount()).signum() > 0) {
            throw new BadRequestException("Invoice reversal is blocked when customer receipts are already allocated");
        }
        if (salesReturnRepository.existsByInvoiceIdAndStatus(entity.getId(), SalesReturnStatus.POSTED)) {
            throw new BadRequestException("Invoice reversal is blocked when posted sales returns already exist");
        }
        String normalizedReason = RequestValueUtils.normalizeRequired(reversalReason, "Reversal reason");
        accountingPostingService.reverseSalesInvoice(entity, normalizedReason);
        reverseStock(entity);
        SalesInvoiceDTO oldData = salesInvoiceMapper.toDTO(entity);
        entity.setStatus(SalesInvoiceStatus.REVERSED);
        entity.setReversedAt(LocalDateTime.now());
        entity.setReversedBy(currentUsername());
        entity.setReversalReason(normalizedReason);
        entity.setPaidAmount(BigDecimal.ZERO);
        entity.setDueAmount(BigDecimal.ZERO);
        entity.setPaymentStatus(SalesPaymentStatus.DUE);
        SalesInvoiceDTO saved = salesInvoiceMapper.toDTO(salesInvoiceRepository.save(entity));
        activityLogService.log("SALES_INVOICE_REVERSE", "SALES", "sales_invoices", saved.getId(), "Reversed sales invoice " + saved.getInvoiceNo());
        auditLogService.log("sales_invoices", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "REVERSE");
        return saved;
    }

    private SalesInvoiceDTO save(SalesInvoiceDTO dto, SalesInvoice entity, boolean creating) {
        validateItems(dto.getItems());

        String requestedInvoiceNo = RequestValueUtils.normalize(dto.getInvoiceNo());
        entity.setInvoiceNo(resolveInvoiceNo(entity.getId(), requestedInvoiceNo));
        SalesOrder order = findOrderById(dto.getOrderId());
        validateOrderForInvoice(order);
        entity.setOrder(order);
        entity.setCustomer(findCustomerById(dto.getCustomerId()));
        entity.setWarehouse(findWarehouseById(dto.getWarehouseId()));
        entity.setSaleDate(dto.getSaleDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setNotes(RequestValueUtils.normalize(dto.getNotes()));
        entity.setStatus(resolveDraftStatus(dto.getStatus(), entity.getStatus(), creating));

        CalculationResult calculation = buildItems(entity, dto.getItems());
        replaceItems(entity, calculation.items());
        entity.setTotalAmount(calculation.totalAmount());
        entity.setDiscountAmount(calculation.discountAmount());
        entity.setTaxAmount(calculation.taxAmount());
        entity.setNetTotal(calculation.netTotal());
        entity.setPaidAmount(BigDecimal.ZERO);
        entity.setDueAmount(calculation.netTotal());
        entity.setPaymentStatus(SalesPaymentStatus.DUE);
        return salesInvoiceMapper.toDTO(salesInvoiceRepository.save(entity));
    }

    private void deductStock(SalesInvoice invoice) {
        Long warehouseId = invoice.getWarehouse().getId();
        for (SalesItem item : invoice.getItems()) {
            stockService.stockOut(
                    item.getProduct().getId(),
                    warehouseId,
                    item.getQuantity(),
                    "SALES_INVOICE",
                    invoice.getInvoiceNo());
        }
    }

    private void reverseStock(SalesInvoice invoice) {
        Long warehouseId = invoice.getWarehouse().getId();
        String referenceNo = invoice.getInvoiceNo() + "-REV";
        for (SalesItem item : invoice.getItems()) {
            stockService.stockIn(
                    item.getProduct().getId(),
                    warehouseId,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    "SALES_INVOICE_REVERSAL",
                    referenceNo);
        }
    }

    private void replaceItems(SalesInvoice invoice, List<SalesItem> items) {
        invoice.getItems().clear();
        invoice.getItems().addAll(items);
    }

    private CalculationResult buildItems(SalesInvoice invoice, List<SalesItemDTO> itemDTOs) {
        List<SalesItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        for (SalesItemDTO itemDTO : itemDTOs) {
            Product product = findProductById(itemDTO.getProductId());
            Uom uom = findUomById(itemDTO.getUomId());

            BigDecimal quantity = positive(itemDTO.getQuantity(), "Quantity must be positive");
            BigDecimal unitPrice = nonNegative(itemDTO.getUnitPrice(), "Unit price cannot be negative");
            BigDecimal discount = nonNegative(itemDTO.getDiscount(), "Discount cannot be negative");
            BigDecimal tax = nonNegative(itemDTO.getTax(), "Tax cannot be negative");
            BigDecimal gross = quantity.multiply(unitPrice);
            BigDecimal subTotal = gross.subtract(discount).add(tax);

            SalesItem item = new SalesItem();
            item.setInvoice(invoice);
            item.setProduct(product);
            item.setUom(uom);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setDiscount(discount);
            item.setTax(tax);
            item.setSubTotal(subTotal);
            items.add(item);

            totalAmount = totalAmount.add(gross);
            discountAmount = discountAmount.add(discount);
            taxAmount = taxAmount.add(tax);
        }

        BigDecimal netTotal = totalAmount.subtract(discountAmount).add(taxAmount);
        return new CalculationResult(items, totalAmount, discountAmount, taxAmount, netTotal);
    }

    private void ensureEditable(SalesInvoice invoice) {
        if (normalizeStatus(invoice.getStatus()) != SalesInvoiceStatus.DRAFT) {
            throw new BadRequestException("Only draft sales invoices can be edited");
        }
    }

    private void ensureCustomerCreditLimit(SalesInvoice invoice) {
        Customer customer = invoice.getCustomer();
        if (customer == null || customer.getCreditLimit() == null || customer.getCreditLimit().signum() <= 0) {
            return;
        }
        BigDecimal currentDue = safe(salesInvoiceRepository.sumDueByCustomerId(customer.getId()));
        BigDecimal projectedDue = currentDue.add(safe(invoice.getDueAmount()));
        if (projectedDue.compareTo(customer.getCreditLimit()) > 0) {
            throw new BadRequestException("Customer credit limit exceeded for this invoice posting");
        }
    }

    private SalesInvoiceStatus resolveDraftStatus(SalesInvoiceStatus requested, SalesInvoiceStatus existing, boolean creating) {
        SalesInvoiceStatus normalizedRequested = normalizeStatus(requested);
        if (normalizedRequested == null) {
            return creating ? SalesInvoiceStatus.DRAFT : normalizeStatus(existing);
        }
        if (normalizedRequested != SalesInvoiceStatus.DRAFT) {
            throw new BadRequestException("Sales invoice save supports draft status only");
        }
        return SalesInvoiceStatus.DRAFT;
    }

    private SalesInvoiceStatus normalizeStatus(SalesInvoiceStatus status) {
        if (status == null) {
            return SalesInvoiceStatus.DRAFT;
        }
        if (status == SalesInvoiceStatus.PENDING) {
            return SalesInvoiceStatus.SUBMITTED;
        }
        if (status == SalesInvoiceStatus.CONFIRMED || status == SalesInvoiceStatus.COMPLETED) {
            return SalesInvoiceStatus.POSTED;
        }
        return status;
    }

    private SalesInvoiceStatus resolvePostedStatus(SalesInvoice invoice) {
        if (invoice.getPaymentStatus() == SalesPaymentStatus.PAID) {
            return SalesInvoiceStatus.PAID;
        }
        if (invoice.getPaymentStatus() == SalesPaymentStatus.PARTIAL) {
            return SalesInvoiceStatus.PARTIAL_PAID;
        }
        return SalesInvoiceStatus.POSTED;
    }

    private String resolveInvoiceNo(Long currentId, String requestedInvoiceNo) {
        if (requestedInvoiceNo != null) {
            validateInvoiceNoUnique(requestedInvoiceNo, currentId);
            return requestedInvoiceNo;
        }

        if (currentId != null) {
            return findInvoiceById(currentId).getInvoiceNo();
        }

        long nextNumber = salesInvoiceRepository.findTopByOrderByIdDesc()
                .map(invoice -> invoice.getId() + 1)
                .orElse(1L);
        String generated = String.format("INV-%04d", nextNumber);
        while (salesInvoiceRepository.existsByInvoiceNo(generated)) {
            nextNumber++;
            generated = String.format("INV-%04d", nextNumber);
        }
        return generated;
    }

    private void validateInvoiceNoUnique(String invoiceNo, Long currentId) {
        boolean exists = currentId == null
                ? salesInvoiceRepository.existsByInvoiceNo(invoiceNo)
                : salesInvoiceRepository.existsByInvoiceNoAndIdNot(invoiceNo, currentId);
        if (exists) {
            throw new DuplicateResourceException("Sales invoice number already exists: " + invoiceNo);
        }
    }

    private void validateItems(List<SalesItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("At least one invoice item is required");
        }
    }

    private void validateOrderForInvoice(SalesOrder order) {
        if (order == null) {
            return;
        }
        SalesOrderStatus status = order.getStatus();
        if (status == SalesOrderStatus.CANCELLED || status == SalesOrderStatus.REJECTED) {
            throw new BadRequestException("Cancelled or rejected sales order cannot be invoiced");
        }
    }

    private SalesInvoice findInvoiceById(Long id) {
        return salesInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales invoice not found with id: " + id));
    }

    private SalesOrder findOrderById(Long id) {
        if (id == null) {
            return null;
        }
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
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

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
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

    private record CalculationResult(
            List<SalesItem> items,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal netTotal) {
    }
}
