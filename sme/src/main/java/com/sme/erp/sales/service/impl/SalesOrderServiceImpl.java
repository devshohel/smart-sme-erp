package com.sme.erp.sales.service.impl;

import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.BadRequestException;
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
import com.sme.erp.sales.dto.SalesItemDTO;
import com.sme.erp.sales.dto.SalesOrderDTO;
import com.sme.erp.sales.entity.SalesItem;
import com.sme.erp.sales.entity.SalesOrder;
import com.sme.erp.sales.mapper.SalesOrderMapper;
import com.sme.erp.sales.repository.SalesOrderRepository;
import com.sme.erp.sales.service.SalesOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesOrderServiceImpl implements SalesOrderService {

    private static final Logger log = LoggerFactory.getLogger(SalesOrderServiceImpl.class);

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UomRepository uomRepository;
    private final SalesOrderMapper salesOrderMapper;

    public SalesOrderServiceImpl(
            SalesOrderRepository salesOrderRepository,
            CustomerRepository customerRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            UomRepository uomRepository,
            SalesOrderMapper salesOrderMapper) {
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.uomRepository = uomRepository;
        this.salesOrderMapper = salesOrderMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDTO> getAll() {
        return salesOrderRepository.findAll().stream()
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
        return save(dto, entity);
    }

    @Override
    @Transactional
    public SalesOrderDTO update(Long id, SalesOrderDTO dto) {
        SalesOrder entity = findOrderById(id);
        return save(dto, entity);
    }

    private SalesOrderDTO save(SalesOrderDTO dto, SalesOrder entity) {
        validateItems(dto.getItems());

        String requestedOrderNo = RequestValueUtils.normalize(dto.getOrderNo());
        log.info(
                "Saving sales order id={}, requestedOrderNo={}, customerId={}, warehouseId={}, orderDate={}, status={}, itemCount={}, grandTotal={}",
                entity.getId(),
                requestedOrderNo,
                dto.getCustomerId(),
                dto.getWarehouseId(),
                dto.getOrderDate(),
                dto.getStatus(),
                dto.getItems() != null ? dto.getItems().size() : 0,
                dto.getGrandTotal());
        entity.setOrderNo(resolveOrderNo(entity.getId(), requestedOrderNo));
        entity.setCustomer(findCustomerById(dto.getCustomerId()));
        entity.setWarehouse(findWarehouseById(dto.getWarehouseId()));
        entity.setOrderDate(dto.getOrderDate());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : entity.getStatus());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setNotes(RequestValueUtils.normalize(dto.getNotes()));

        CalculationResult calculation = buildItems(entity, dto.getItems());
        replaceItems(entity, calculation.items());
        entity.setGrandTotal(calculation.grandTotal());

        log.info(
                "Sales order prepared orderNo={}, customerRef={}, warehouseRef={}, status={}, persistedItemCount={}, calculatedGrandTotal={}",
                entity.getOrderNo(),
                entity.getCustomer() != null ? entity.getCustomer().getId() : null,
                entity.getWarehouse() != null ? entity.getWarehouse().getId() : null,
                entity.getStatus(),
                entity.getItems().size(),
                entity.getGrandTotal());

        SalesOrder saved;
        try {
            saved = salesOrderRepository.save(entity);
        } catch (RuntimeException ex) {
            log.error(
                    "Sales order save failed orderNo={}, status={}, customerId={}, warehouseId={}, itemCount={}. Check sales_orders columns and sales_items invoice_id/order_id constraints.",
                    entity.getOrderNo(),
                    entity.getStatus(),
                    entity.getCustomer() != null ? entity.getCustomer().getId() : null,
                    entity.getWarehouse() != null ? entity.getWarehouse().getId() : null,
                    entity.getItems().size(),
                    ex);
            throw ex;
        }

        log.info("Sales order saved id={}, orderNo={}, itemCount={}", saved.getId(), saved.getOrderNo(), saved.getItems().size());
        return salesOrderMapper.toDTO(saved);
    }

    private CalculationResult buildItems(SalesOrder order, List<SalesItemDTO> itemDTOs) {
        List<SalesItem> items = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (SalesItemDTO itemDTO : itemDTOs) {
            Product product = findProductById(itemDTO.getProductId());
            Uom uom = findUomById(itemDTO.getUomId());
            BigDecimal quantity = positive(itemDTO.getQuantity(), "Quantity must be positive");
            BigDecimal unitPrice = nonNegative(itemDTO.getUnitPrice(), "Unit price cannot be negative");
            BigDecimal subTotal = quantity.multiply(unitPrice);

            SalesItem item = new SalesItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setUom(uom);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setDiscount(BigDecimal.ZERO);
            item.setTax(BigDecimal.ZERO);
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

    private void validateItems(List<SalesItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("At least one sales order item is required");
        }
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

    private record CalculationResult(List<SalesItem> items, BigDecimal grandTotal) {
    }
}
