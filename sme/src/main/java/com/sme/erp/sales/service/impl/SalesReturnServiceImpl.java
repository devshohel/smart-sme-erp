package com.sme.erp.sales.service.impl;

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
import com.sme.erp.sales.mapper.SalesReturnMapper;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesReturnRepository;
import com.sme.erp.sales.service.SalesReturnService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesReturnServiceImpl implements SalesReturnService {

    private final SalesReturnRepository salesReturnRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SalesReturnMapper salesReturnMapper;
    private final StockService stockService;

    public SalesReturnServiceImpl(
            SalesReturnRepository salesReturnRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            SalesReturnMapper salesReturnMapper,
            StockService stockService) {
        this.salesReturnRepository = salesReturnRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.salesReturnMapper = salesReturnMapper;
        this.stockService = stockService;
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
        validateItems(dto.getItems());

        SalesInvoice invoice = findInvoiceById(dto.getInvoiceId());
        Customer customer = findCustomerById(dto.getCustomerId());
        if (!invoice.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Return customer must match the invoice customer");
        }

        SalesReturn entity = new SalesReturn();
        entity.setReturnCode(resolveReturnCode(RequestValueUtils.normalize(dto.getReturnCode())));
        entity.setInvoice(invoice);
        entity.setCustomer(customer);
        entity.setReturnDate(dto.getReturnDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setNotes(RequestValueUtils.normalize(dto.getNotes()));

        CalculationResult calculation = buildItems(entity, dto.getItems());
        entity.setItems(calculation.items());
        entity.setTotalAmount(calculation.totalAmount());

        SalesReturn saved = salesReturnRepository.save(entity);

        restockReturnedItems(saved);

        return salesReturnMapper.toDTO(saved);
    }

    private void restockReturnedItems(SalesReturn salesReturn) {
        Long warehouseId = salesReturn.getInvoice().getWarehouse().getId();
        for (SalesReturnItem item : salesReturn.getItems()) {
            stockService.stockIn(
                    item.getProduct().getId(),
                    warehouseId,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    "SALES_RETURN",
                    salesReturn.getReturnCode());
        }
    }

    private CalculationResult buildItems(SalesReturn salesReturn, List<SalesReturnItemDTO> itemDTOs) {
        List<SalesReturnItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SalesReturnItemDTO itemDTO : itemDTOs) {
            Product product = findProductById(itemDTO.getProductId());
            BigDecimal quantity = positive(itemDTO.getQuantity(), "Quantity must be positive");
            BigDecimal unitPrice = nonNegative(itemDTO.getUnitPrice(), "Unit price cannot be negative");
            BigDecimal total = quantity.multiply(unitPrice);

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

    private String resolveReturnCode(String requestedReturnCode) {
        if (requestedReturnCode != null) {
            if (salesReturnRepository.existsByReturnCode(requestedReturnCode)) {
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

    private record CalculationResult(List<SalesReturnItem> items, BigDecimal totalAmount) {
    }
}
