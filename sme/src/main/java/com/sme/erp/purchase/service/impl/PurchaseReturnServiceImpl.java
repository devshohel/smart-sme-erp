package com.sme.erp.purchase.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.purchase.dto.PurchaseReturnDTO;
import com.sme.erp.purchase.dto.PurchaseReturnItemDTO;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.entity.PurchaseReturn;
import com.sme.erp.purchase.entity.PurchaseReturnItem;
import com.sme.erp.purchase.mapper.PurchaseReturnMapper;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.repository.PurchaseReturnRepository;
import com.sme.erp.purchase.service.PurchaseReturnService;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    public PurchaseReturnServiceImpl(
            PurchaseReturnRepository purchaseReturnRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            PurchaseReturnMapper purchaseReturnMapper,
            StockService stockService) {
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.purchaseReturnMapper = purchaseReturnMapper;
        this.stockService = stockService;
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
        validateItems(dto.getItems());

        PurchaseOrder purchaseOrder = findPurchaseById(dto.getPurchaseId());
        Supplier supplier = findSupplierById(dto.getSupplierId());
        if (purchaseOrder.getSupplier() == null || !purchaseOrder.getSupplier().getId().equals(supplier.getId())) {
            throw new BadRequestException("Return supplier must match the purchase supplier");
        }

        PurchaseReturn entity = new PurchaseReturn();
        entity.setReturnCode(resolveReturnCode(RequestValueUtils.normalize(dto.getReturnCode())));
        entity.setPurchase(purchaseOrder);
        entity.setSupplier(supplier);
        entity.setReturnDate(dto.getReturnDate());
        entity.setCreatedBy(dto.getCreatedBy());

        CalculationResult calculation = buildItems(entity, dto.getItems());
        entity.setItems(calculation.items());
        entity.setTotalAmount(calculation.totalAmount());

        PurchaseReturn saved = purchaseReturnRepository.save(entity);

        deductReturnedStock(saved);

        // TODO Supplier ledger or payment integration should own future payable and advance balance movements.

        return purchaseReturnMapper.toDTO(saved);
    }

    private void deductReturnedStock(PurchaseReturn purchaseReturn) {
        Long warehouseId = purchaseReturn.getPurchase().getWarehouse().getId();
        for (PurchaseReturnItem item : purchaseReturn.getItems()) {
            stockService.stockOut(
                    item.getProduct().getId(),
                    warehouseId,
                    item.getQuantity());
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

    private String resolveReturnCode(String requestedReturnCode) {
        if (requestedReturnCode != null) {
            if (purchaseReturnRepository.existsByReturnCode(requestedReturnCode)) {
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

    private record CalculationResult(List<PurchaseReturnItem> items, BigDecimal totalAmount) {
    }
}
