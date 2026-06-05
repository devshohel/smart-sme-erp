package com.sme.erp.sales.service.impl;

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
import com.sme.erp.sales.enums.SalesPaymentStatus;
import com.sme.erp.sales.mapper.SalesInvoiceMapper;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesOrderRepository;
import com.sme.erp.sales.service.SalesInvoiceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final StockService stockService;

    public SalesInvoiceServiceImpl(
            SalesInvoiceRepository salesInvoiceRepository,
            SalesOrderRepository salesOrderRepository,
            CustomerRepository customerRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            UomRepository uomRepository,
            SalesInvoiceMapper salesInvoiceMapper,
            StockService stockService) {
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.uomRepository = uomRepository;
        this.salesInvoiceMapper = salesInvoiceMapper;
        this.stockService = stockService;
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
    public SalesInvoiceDTO getById(Long id) {
        return salesInvoiceMapper.toDTO(findInvoiceById(id));
    }

    @Override
    @Transactional
    public SalesInvoiceDTO create(SalesInvoiceDTO dto) {
        SalesInvoice entity = new SalesInvoice();
        return save(dto, entity);
    }

    @Override
    @Transactional
    public SalesInvoiceDTO update(Long id, SalesInvoiceDTO dto) {
        SalesInvoice entity = findInvoiceById(id);
        return save(dto, entity);
    }

    private SalesInvoiceDTO save(SalesInvoiceDTO dto, SalesInvoice entity) {
        validateItems(dto.getItems());
        SalesInvoiceStatus previousStatus = entity.getStatus();

        String requestedInvoiceNo = RequestValueUtils.normalize(dto.getInvoiceNo());
        entity.setInvoiceNo(resolveInvoiceNo(entity.getId(), requestedInvoiceNo));
        entity.setOrder(findOrderById(dto.getOrderId()));
        entity.setCustomer(findCustomerById(dto.getCustomerId()));
        entity.setWarehouse(findWarehouseById(dto.getWarehouseId()));
        entity.setSaleDate(dto.getSaleDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setNotes(RequestValueUtils.normalize(dto.getNotes()));
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : entity.getStatus());

        CalculationResult calculation = buildItems(entity, dto.getItems());
        replaceItems(entity, calculation.items());
        entity.setTotalAmount(calculation.totalAmount());
        entity.setDiscountAmount(calculation.discountAmount());
        entity.setTaxAmount(calculation.taxAmount());
        entity.setNetTotal(calculation.netTotal());

        BigDecimal paidAmount = safe(dto.getPaidAmount());
        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Paid amount cannot be negative");
        }
        entity.setPaidAmount(paidAmount);
        entity.setDueAmount(calculation.netTotal().subtract(paidAmount).max(BigDecimal.ZERO));
        entity.setPaymentStatus(resolvePaymentStatus(paidAmount, calculation.netTotal()));

        SalesInvoice saved = salesInvoiceRepository.save(entity);

        if (previousStatus != SalesInvoiceStatus.CONFIRMED && saved.getStatus() == SalesInvoiceStatus.CONFIRMED) {
            deductStock(saved);
        }

        return salesInvoiceMapper.toDTO(saved);
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

    private SalesPaymentStatus resolvePaymentStatus(BigDecimal paidAmount, BigDecimal netTotal) {
        if (paidAmount.compareTo(netTotal) >= 0) {
            return SalesPaymentStatus.PAID;
        }
        if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            return SalesPaymentStatus.PARTIAL;
        }
        return SalesPaymentStatus.DUE;
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
