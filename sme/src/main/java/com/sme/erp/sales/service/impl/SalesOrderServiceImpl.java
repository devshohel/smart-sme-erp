package com.sme.erp.sales.service.impl;

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
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.entity.Uom;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.product.repository.UomRepository;
import com.sme.erp.sales.dto.SalesInvoiceDTO;
import com.sme.erp.sales.dto.SalesItemDTO;
import com.sme.erp.sales.dto.SalesOrderDTO;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.entity.SalesItem;
import com.sme.erp.sales.entity.SalesOrder;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.enums.SalesOrderStatus;
import com.sme.erp.sales.enums.SalesPaymentStatus;
import com.sme.erp.sales.mapper.SalesInvoiceMapper;
import com.sme.erp.sales.mapper.SalesOrderMapper;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesOrderRepository;
import com.sme.erp.sales.service.SalesOrderService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesOrderServiceImpl implements SalesOrderService {
    private final SalesOrderRepository salesOrderRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UomRepository uomRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final SalesInvoiceMapper salesInvoiceMapper;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;

    public SalesOrderServiceImpl(
            SalesOrderRepository salesOrderRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            CustomerRepository customerRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            UomRepository uomRepository,
            SalesOrderMapper salesOrderMapper,
            SalesInvoiceMapper salesInvoiceMapper,
            ActivityLogService activityLogService,
            AuditLogService auditLogService) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.customerRepository = customerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.uomRepository = uomRepository;
        this.salesOrderMapper = salesOrderMapper;
        this.salesInvoiceMapper = salesInvoiceMapper;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDTO> getAll() {
        return salesOrderRepository.findAll().stream()
                .sorted(Comparator
                        .comparing(SalesOrder::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(SalesOrder::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(salesOrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderDTO getById(Long id) {
        return salesOrderMapper.toDTO(findOrderById(id));
    }

    @Override
    @Transactional
    public SalesOrderDTO create(SalesOrderDTO dto) {
        SalesOrder entity = new SalesOrder();
        SalesOrderDTO saved = save(dto, entity, true);
        activityLogService.log("SALES_ORDER_CREATE", "SALES", "sales_orders", saved.getId(), "Created sales order " + saved.getOrderNo());
        auditLogService.log("sales_orders", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public SalesOrderDTO update(Long id, SalesOrderDTO dto) {
        SalesOrder entity = findOrderById(id);
        ensureEditable(entity);
        SalesOrderDTO oldData = salesOrderMapper.toDTO(entity);
        SalesOrderDTO saved = save(dto, entity, false);
        activityLogService.log("SALES_ORDER_UPDATE", "SALES", "sales_orders", saved.getId(), "Updated sales order " + saved.getOrderNo());
        auditLogService.log("sales_orders", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public SalesOrderDTO submit(Long id) {
        SalesOrder entity = findOrderById(id);
        SalesOrderStatus normalizedStatus = normalizeStatus(entity.getStatus());
        if (normalizedStatus != SalesOrderStatus.DRAFT && normalizedStatus != SalesOrderStatus.REJECTED) {
            throw new BadRequestException("Only draft or rejected sales orders can be submitted");
        }
        SalesOrderDTO oldData = salesOrderMapper.toDTO(entity);
        entity.setStatus(SalesOrderStatus.SUBMITTED);
        entity.setSubmittedAt(LocalDateTime.now());
        entity.setSubmittedBy(currentUsername());
        SalesOrderDTO saved = salesOrderMapper.toDTO(salesOrderRepository.save(entity));
        activityLogService.log("SALES_ORDER_SUBMIT", "SALES", "sales_orders", saved.getId(), "Submitted sales order " + saved.getOrderNo());
        auditLogService.log("sales_orders", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "SUBMIT");
        return saved;
    }

    @Override
    @Transactional
    public SalesOrderDTO approve(Long id) {
        SalesOrder entity = findOrderById(id);
        if (normalizeStatus(entity.getStatus()) != SalesOrderStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted sales orders can be approved");
        }
        SalesOrderDTO oldData = salesOrderMapper.toDTO(entity);
        entity.setStatus(SalesOrderStatus.APPROVED);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setApprovedBy(currentUsername());
        SalesOrderDTO saved = salesOrderMapper.toDTO(salesOrderRepository.save(entity));
        activityLogService.log("SALES_ORDER_APPROVE", "SALES", "sales_orders", saved.getId(), "Approved sales order " + saved.getOrderNo());
        auditLogService.log("sales_orders", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "APPROVE");
        return saved;
    }

    @Override
    @Transactional
    public SalesOrderDTO reject(Long id, String reason) {
        SalesOrder entity = findOrderById(id);
        if (normalizeStatus(entity.getStatus()) != SalesOrderStatus.SUBMITTED) {
            throw new BadRequestException("Only submitted sales orders can be rejected");
        }
        SalesOrderDTO oldData = salesOrderMapper.toDTO(entity);
        entity.setStatus(SalesOrderStatus.REJECTED);
        entity.setRejectedAt(LocalDateTime.now());
        entity.setRejectedBy(currentUsername());
        entity.setRejectionReason(RequestValueUtils.normalizeRequired(reason, "Rejection reason"));
        SalesOrderDTO saved = salesOrderMapper.toDTO(salesOrderRepository.save(entity));
        activityLogService.log("SALES_ORDER_REJECT", "SALES", "sales_orders", saved.getId(), "Rejected sales order " + saved.getOrderNo());
        auditLogService.log("sales_orders", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "REJECT");
        return saved;
    }

    @Override
    @Transactional
    public SalesOrderDTO cancel(Long id) {
        SalesOrder entity = findOrderById(id);
        SalesOrderStatus normalizedStatus = normalizeStatus(entity.getStatus());
        if (normalizedStatus == SalesOrderStatus.CONVERTED || normalizedStatus == SalesOrderStatus.CANCELLED) {
            throw new BadRequestException("Sales order cannot be cancelled in its current status");
        }
        SalesOrderDTO oldData = salesOrderMapper.toDTO(entity);
        entity.setStatus(SalesOrderStatus.CANCELLED);
        entity.setCancelledAt(LocalDateTime.now());
        entity.setCancelledBy(currentUsername());
        SalesOrderDTO saved = salesOrderMapper.toDTO(salesOrderRepository.save(entity));
        activityLogService.log("SALES_ORDER_CANCEL", "SALES", "sales_orders", saved.getId(), "Cancelled sales order " + saved.getOrderNo());
        auditLogService.log("sales_orders", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "CANCEL");
        return saved;
    }

    @Override
    @Transactional
    public SalesInvoiceDTO convertToInvoice(Long id) {
        SalesOrder order = findOrderById(id);
        if (normalizeStatus(order.getStatus()) != SalesOrderStatus.APPROVED) {
            throw new BadRequestException("Only approved sales orders can be converted to invoice");
        }
        if (salesInvoiceRepository.findFirstByOrderId(id).isPresent()) {
            throw new BadRequestException("Sales order is already converted to an invoice");
        }

        SalesInvoice invoice = new SalesInvoice();
        invoice.setInvoiceNo(nextInvoiceNo());
        invoice.setOrder(order);
        invoice.setCustomer(order.getCustomer());
        invoice.setWarehouse(order.getWarehouse());
        invoice.setSaleDate(order.getOrderDate());
        invoice.setCreatedBy(order.getCreatedBy());
        invoice.setNotes(order.getNotes());
        invoice.setStatus(SalesInvoiceStatus.DRAFT);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setPaymentStatus(SalesPaymentStatus.DUE);

        List<SalesItem> invoiceItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesItem orderItem : order.getItems()) {
            Product product = requireProduct(orderItem, "Sales order contains a deleted product. Edit the order and select an active product before converting.");
            SalesItem invoiceItem = new SalesItem();
            invoiceItem.setInvoice(invoice);
            invoiceItem.setProduct(product);
            invoiceItem.setUom(orderItem.getUom());
            invoiceItem.setQuantity(orderItem.getQuantity());
            invoiceItem.setUnitPrice(orderItem.getUnitPrice());
            invoiceItem.setDiscount(BigDecimal.ZERO);
            invoiceItem.setTax(BigDecimal.ZERO);
            BigDecimal subTotal = safe(orderItem.getQuantity()).multiply(safe(orderItem.getUnitPrice()));
            invoiceItem.setSubTotal(subTotal);
            invoiceItems.add(invoiceItem);
            totalAmount = totalAmount.add(subTotal);
        }
        invoice.getItems().clear();
        invoice.getItems().addAll(invoiceItems);
        invoice.setTotalAmount(totalAmount);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setNetTotal(totalAmount);
        invoice.setDueAmount(totalAmount);
        SalesInvoice savedInvoice = salesInvoiceRepository.save(invoice);

        SalesOrderDTO oldOrder = salesOrderMapper.toDTO(order);
        order.setStatus(SalesOrderStatus.CONVERTED);
        order.setConvertedAt(LocalDateTime.now());
        order.setConvertedBy(currentUsername());
        salesOrderRepository.save(order);
        SalesOrderDTO savedOrder = salesOrderMapper.toDTO(order);

        activityLogService.log("SALES_ORDER_CONVERT", "SALES", "sales_orders", order.getId(), "Converted sales order " + order.getOrderNo() + " to invoice " + savedInvoice.getInvoiceNo());
        auditLogService.log("sales_orders", order.getId(), auditLogService.toJson(oldOrder), auditLogService.toJson(savedOrder), "CONVERT");
        auditLogService.log("sales_invoices", savedInvoice.getId(), null, auditLogService.toJson(salesInvoiceMapper.toDTO(savedInvoice)), "CREATE");
        return salesInvoiceMapper.toDTO(savedInvoice);
    }

    private SalesOrderDTO save(SalesOrderDTO dto, SalesOrder entity, boolean creating) {
        validateItems(dto.getItems());

        String requestedOrderNo = RequestValueUtils.normalize(dto.getOrderNo());
        entity.setOrderNo(resolveOrderNo(entity.getId(), requestedOrderNo));
        entity.setCustomer(findCustomerById(dto.getCustomerId()));
        entity.setWarehouse(findWarehouseById(dto.getWarehouseId()));
        entity.setOrderDate(dto.getOrderDate());
        entity.setStatus(resolveDraftStatus(dto.getStatus(), entity.getStatus(), creating));
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setNotes(RequestValueUtils.normalize(dto.getNotes()));

        CalculationResult calculation = buildItems(entity, dto.getItems());
        replaceItems(entity, calculation.items());
        entity.setGrandTotal(calculation.grandTotal());
        return salesOrderMapper.toDTO(salesOrderRepository.save(entity));
    }

    private CalculationResult buildItems(SalesOrder order, List<SalesItemDTO> itemDTOs) {
        List<SalesItem> items = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (SalesItemDTO itemDTO : itemDTOs) {
            Product product = findProductById(itemDTO.getProductId());
            Uom uom = findUomById(itemDTO.getUomId());
            BigDecimal quantity = positive(itemDTO.getQuantity(), "Quantity must be positive");
            BigDecimal unitPrice = nonNegative(itemDTO.getUnitPrice(), "Unit price cannot be negative");
            BigDecimal discount = nonNegative(itemDTO.getDiscount(), "Discount cannot be negative");
            BigDecimal tax = nonNegative(itemDTO.getTax(), "Tax cannot be negative");
            BigDecimal subTotal = quantity.multiply(unitPrice).subtract(discount).add(tax);

            SalesItem item = new SalesItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setUom(uom);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setDiscount(discount);
            item.setTax(tax);
            item.setSubTotal(subTotal);
            items.add(item);

            grandTotal = grandTotal.add(subTotal);
        }

        return new CalculationResult(items, grandTotal);
    }

    private void replaceItems(SalesOrder order, List<SalesItem> items) {
        order.getItems().clear();
        order.getItems().addAll(items);
    }

    private void ensureEditable(SalesOrder entity) {
        SalesOrderStatus normalizedStatus = normalizeStatus(entity.getStatus());
        if (normalizedStatus != SalesOrderStatus.DRAFT && normalizedStatus != SalesOrderStatus.REJECTED) {
            throw new BadRequestException("Only draft or rejected sales orders can be edited");
        }
    }

    private SalesOrderStatus resolveDraftStatus(SalesOrderStatus requested, SalesOrderStatus existing, boolean creating) {
        SalesOrderStatus normalizedRequested = normalizeStatus(requested);
        if (normalizedRequested == null) {
            return creating ? SalesOrderStatus.DRAFT : normalizeStatus(existing);
        }
        if (normalizedRequested != SalesOrderStatus.DRAFT && normalizedRequested != SalesOrderStatus.REJECTED) {
            throw new BadRequestException("Sales order save supports draft status only");
        }
        return SalesOrderStatus.DRAFT;
    }

    private SalesOrderStatus normalizeStatus(SalesOrderStatus status) {
        if (status == null) {
            return SalesOrderStatus.DRAFT;
        }
        if (status == SalesOrderStatus.PENDING) {
            return SalesOrderStatus.SUBMITTED;
        }
        return status;
    }

    private String resolveOrderNo(Long currentId, String requestedOrderNo) {
        if (requestedOrderNo != null) {
            validateOrderNoUnique(requestedOrderNo, currentId);
            return requestedOrderNo;
        }

        if (currentId != null) {
            return findOrderById(currentId).getOrderNo();
        }

        long nextNumber = salesOrderRepository.findTopByOrderByIdDesc()
                .map(order -> order.getId() + 1)
                .orElse(1L);
        String generated = String.format("SO-%04d", nextNumber);
        while (salesOrderRepository.existsByOrderNo(generated)) {
            nextNumber++;
            generated = String.format("SO-%04d", nextNumber);
        }
        return generated;
    }

    private void validateOrderNoUnique(String orderNo, Long currentId) {
        boolean exists = currentId == null
                ? salesOrderRepository.existsByOrderNo(orderNo)
                : salesOrderRepository.existsByOrderNoAndIdNot(orderNo, currentId);
        if (exists) {
            throw new DuplicateResourceException("Sales order number already exists: " + orderNo);
        }
    }

    private SalesOrder findOrderById(Long id) {
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

    private Product requireProduct(SalesItem item, String message) {
        if (item == null || item.getProduct() == null) {
            throw new BadRequestException(message);
        }
        return item.getProduct();
    }

    private void validateItems(List<SalesItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("At least one sales order item is required");
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String nextInvoiceNo() {
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

    private record CalculationResult(List<SalesItem> items, BigDecimal grandTotal) {
    }
}
