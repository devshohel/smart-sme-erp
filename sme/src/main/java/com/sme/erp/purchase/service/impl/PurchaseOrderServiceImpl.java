package com.sme.erp.purchase.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.entity.Uom;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.product.repository.UomRepository;
import com.sme.erp.purchase.dto.PurchaseItemDTO;
import com.sme.erp.purchase.dto.PurchaseOrderDTO;
import com.sme.erp.purchase.entity.PurchaseItem;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.mapper.PurchaseOrderMapper;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.service.PurchaseOrderService;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final StockService stockService;

    public PurchaseOrderServiceImpl(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            UomRepository uomRepository,
            PurchaseOrderMapper purchaseOrderMapper,
            StockService stockService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.uomRepository = uomRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.stockService = stockService;
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
    public PurchaseOrderDTO getById(Long id) {
        return purchaseOrderMapper.toDTO(findPurchaseOrderById(id));
    }

    @Override
    @Transactional
    public PurchaseOrderDTO create(PurchaseOrderDTO dto) {
        PurchaseOrder entity = new PurchaseOrder();
        return save(dto, entity);
    }

    @Override
    @Transactional
    public PurchaseOrderDTO update(Long id, PurchaseOrderDTO dto) {
        PurchaseOrder entity = findPurchaseOrderById(id);
        return save(dto, entity);
    }

    private PurchaseOrderDTO save(PurchaseOrderDTO dto, PurchaseOrder entity) {
        validateItems(dto.getItems());
        PurchaseStatus previousStatus = entity.getStatus();

        String requestedPurchaseCode = RequestValueUtils.normalize(dto.getPurchaseCode());
        entity.setPurchaseCode(resolvePurchaseCode(entity.getId(), requestedPurchaseCode));
        entity.setSupplier(findSupplierById(dto.getSupplierId()));
        entity.setWarehouse(findWarehouseById(dto.getWarehouseId()));
        entity.setPurchaseDate(dto.getPurchaseDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : entity.getStatus());
        if (entity.getStatus() == null) {
            entity.setStatus(PurchaseStatus.PENDING);
        }

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

        PurchaseOrder saved = purchaseOrderRepository.save(entity);

        if (previousStatus != PurchaseStatus.RECEIVED && saved.getStatus() == PurchaseStatus.RECEIVED) {
            receiveStock(saved);
        }

        // TODO Supplier ledger or payment integration should own future payable and advance balance movements.

        return purchaseOrderMapper.toDTO(saved);
    }

    private void receiveStock(PurchaseOrder purchaseOrder) {
        Long warehouseId = purchaseOrder.getWarehouse().getId();
        for (PurchaseItem item : purchaseOrder.getItems()) {
            stockService.stockIn(
                    item.getProduct().getId(),
                    warehouseId,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    "PURCHASE_RECEIVE",
                    purchaseOrder.getPurchaseCode());
        }
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

    private record CalculationResult(
            List<PurchaseItem> items,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal netTotal) {
    }
}
