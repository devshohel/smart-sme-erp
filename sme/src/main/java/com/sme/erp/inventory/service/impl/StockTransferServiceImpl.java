package com.sme.erp.inventory.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.inventory.dto.StockTransferDTO;
import com.sme.erp.inventory.dto.StockTransferItemDTO;
import com.sme.erp.inventory.dto.StockTransferPageDTO;
import com.sme.erp.inventory.entity.StockTransfer;
import com.sme.erp.inventory.entity.StockTransferItem;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.enums.StockTransferStatus;
import com.sme.erp.inventory.repository.StockTransferRepository;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.inventory.service.StockService;
import com.sme.erp.inventory.service.StockTransferService;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockTransferServiceImpl implements StockTransferService {

    private final StockTransferRepository transferRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    public StockTransferServiceImpl(StockTransferRepository transferRepository,
                                    WarehouseRepository warehouseRepository,
                                    ProductRepository productRepository,
                                    StockService stockService) {
        this.transferRepository = transferRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransferDTO> getAll() {
        return transferRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransferPageDTO search(String keyword, Long fromWarehouseId, Long toWarehouseId, StockTransferStatus status,
                                       LocalDate fromDate, LocalDate toDate, int page, int size, String sort, String direction) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize(size), sortFor(sort, direction));
        Page<StockTransfer> result = transferRepository.findAll(
                spec(keyword, fromWarehouseId, toWarehouseId, status, fromDate, toDate), pageable);
        return new StockTransferPageDTO(
                result.getContent().stream().map(this::toDTO).collect(Collectors.toList()),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransferDTO getById(Long id) {
        return toDTO(findById(id));
    }

    @Override
    @Transactional
    public StockTransferDTO create(StockTransferDTO dto) {
        validateHeader(dto);
        StockTransfer transfer = new StockTransfer();
        transfer.setTransferNo(nextTransferNo());
        applyEditableFields(transfer, dto);
        StockTransferStatus requested = dto.getStatus() == null ? StockTransferStatus.DRAFT : dto.getStatus();
        if (requested != StockTransferStatus.DRAFT && requested != StockTransferStatus.PENDING) {
            throw new BadRequestException("Transfer can only be created as DRAFT or PENDING.");
        }
        transfer.setStatus(requested);
        return toDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public StockTransferDTO update(Long id, StockTransferDTO dto) {
        StockTransfer transfer = findById(id);
        if (transfer.getStatus() == StockTransferStatus.IN_TRANSIT || transfer.getStatus() == StockTransferStatus.RECEIVED
                || transfer.getStatus() == StockTransferStatus.CANCELLED) {
            throw new BadRequestException("Transfer cannot be edited after in transit.");
        }
        validateHeader(dto);
        applyEditableFields(transfer, dto);
        StockTransferStatus requested = dto.getStatus() == null ? transfer.getStatus() : dto.getStatus();
        if (requested == StockTransferStatus.IN_TRANSIT || requested == StockTransferStatus.RECEIVED
                || requested == StockTransferStatus.CANCELLED) {
            throw new BadRequestException("Use workflow actions to change transfer status.");
        }
        transfer.setStatus(requested);
        return toDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public StockTransferDTO approve(Long id) {
        StockTransfer transfer = findById(id);
        requireStatus(transfer, StockTransferStatus.DRAFT, StockTransferStatus.PENDING);
        transfer.setStatus(StockTransferStatus.APPROVED);
        transfer.setApprovedAt(LocalDateTime.now());
        return toDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public StockTransferDTO send(Long id) {
        StockTransfer transfer = findById(id);
        requireStatus(transfer, StockTransferStatus.APPROVED);
        for (StockTransferItem item : transfer.getItems()) {
            stockService.transferOut(item.getProduct().getId(), transfer.getFromWarehouse().getId(),
                    item.getQuantity(), transfer.getTransferNo());
        }
        transfer.setStatus(StockTransferStatus.IN_TRANSIT);
        return toDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public StockTransferDTO receive(Long id) {
        StockTransfer transfer = findById(id);
        requireStatus(transfer, StockTransferStatus.IN_TRANSIT);
        for (StockTransferItem item : transfer.getItems()) {
            stockService.transferIn(item.getProduct().getId(), transfer.getToWarehouse().getId(),
                    item.getQuantity(), transfer.getTransferNo());
        }
        transfer.setStatus(StockTransferStatus.RECEIVED);
        transfer.setReceivedAt(LocalDateTime.now());
        return toDTO(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public StockTransferDTO cancel(Long id) {
        StockTransfer transfer = findById(id);
        requireStatus(transfer, StockTransferStatus.DRAFT, StockTransferStatus.PENDING, StockTransferStatus.APPROVED);
        transfer.setStatus(StockTransferStatus.CANCELLED);
        return toDTO(transferRepository.save(transfer));
    }

    private void applyEditableFields(StockTransfer transfer, StockTransferDTO dto) {
        Warehouse from = warehouseRepository.findById(dto.getFromWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + dto.getFromWarehouseId()));
        Warehouse to = warehouseRepository.findById(dto.getToWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + dto.getToWarehouseId()));
        transfer.setFromWarehouse(from);
        transfer.setToWarehouse(to);
        transfer.setTransferDate(dto.getTransferDate());
        transfer.setExpectedDate(dto.getExpectedDate());
        transfer.setRemarks(trim(dto.getRemarks()));
        transfer.getItems().clear();
        for (StockTransferItemDTO itemDTO : dto.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));
            StockTransferItem item = new StockTransferItem();
            item.setTransfer(transfer);
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setRemarks(trim(itemDTO.getRemarks()));
            transfer.getItems().add(item);
        }
    }

    private void validateHeader(StockTransferDTO dto) {
        if (dto.getFromWarehouseId() == null || dto.getToWarehouseId() == null) {
            throw new BadRequestException("From warehouse and to warehouse are required.");
        }
        if (dto.getFromWarehouseId().equals(dto.getToWarehouseId())) {
            throw new BadRequestException("From warehouse and to warehouse cannot be same.");
        }
        if (dto.getTransferDate() == null) {
            throw new BadRequestException("Transfer date is required.");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BadRequestException("At least one transfer item is required.");
        }
        for (StockTransferItemDTO item : dto.getItems()) {
            if (item.getProductId() == null || item.getProductId() <= 0) {
                throw new BadRequestException("Product is required for each transfer item.");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Item quantity must be greater than 0.");
            }
        }
    }

    private void requireStatus(StockTransfer transfer, StockTransferStatus... allowed) {
        for (StockTransferStatus status : allowed) {
            if (transfer.getStatus() == status) {
                return;
            }
        }
        throw new BadRequestException("Action is not allowed for transfer status: " + transfer.getStatus());
    }

    private StockTransfer findById(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock transfer not found with id: " + id));
    }

    private synchronized String nextTransferNo() {
        StockTransfer last = transferRepository.findTopByOrderByIdDesc();
        long next = last == null || last.getId() == null ? 1L : last.getId() + 1L;
        return String.format("ST-%06d", next);
    }

    private StockTransferDTO toDTO(StockTransfer transfer) {
        StockTransferDTO dto = new StockTransferDTO();
        dto.setId(transfer.getId());
        dto.setTransferNo(transfer.getTransferNo());
        dto.setStatus(transfer.getStatus());
        dto.setTransferDate(transfer.getTransferDate());
        dto.setExpectedDate(transfer.getExpectedDate());
        dto.setRemarks(transfer.getRemarks());
        dto.setCreatedAt(transfer.getCreatedAt());
        dto.setUpdatedAt(transfer.getUpdatedAt());
        dto.setCreatedBy(transfer.getCreatedBy());
        dto.setApprovedBy(transfer.getApprovedBy());
        dto.setApprovedAt(transfer.getApprovedAt());
        dto.setReceivedBy(transfer.getReceivedBy());
        dto.setReceivedAt(transfer.getReceivedAt());
        if (transfer.getFromWarehouse() != null) {
            dto.setFromWarehouseId(transfer.getFromWarehouse().getId());
            dto.setFromWarehouseName(transfer.getFromWarehouse().getName());
        }
        if (transfer.getToWarehouse() != null) {
            dto.setToWarehouseId(transfer.getToWarehouse().getId());
            dto.setToWarehouseName(transfer.getToWarehouse().getName());
        }
        List<StockTransferItemDTO> items = new ArrayList<>();
        for (StockTransferItem item : transfer.getItems()) {
            StockTransferItemDTO itemDTO = new StockTransferItemDTO();
            itemDTO.setId(item.getId());
            if (item.getProduct() != null) {
                itemDTO.setProductId(item.getProduct().getId());
                itemDTO.setProductName(item.getProduct().getProductName());
                itemDTO.setSku(item.getProduct().getSku());
            }
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setRemarks(item.getRemarks());
            items.add(itemDTO);
        }
        dto.setItems(items);
        return dto;
    }

    private Specification<StockTransfer> spec(String keyword, Long fromWarehouseId, Long toWarehouseId,
                                              StockTransferStatus status, LocalDate fromDate, LocalDate toDate) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("fromWarehouse", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("toWarehouse", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("items", jakarta.persistence.criteria.JoinType.LEFT)
                        .fetch("product", jakarta.persistence.criteria.JoinType.LEFT);
                query.distinct(true);
            }
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("transferNo")), "%" + keyword.trim().toLowerCase() + "%"));
            }
            if (fromWarehouseId != null) {
                predicates.add(cb.equal(root.get("fromWarehouse").get("id"), fromWarehouseId));
            }
            if (toWarehouseId != null) {
                predicates.add(cb.equal(root.get("toWarehouse").get("id"), toWarehouseId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transferDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transferDate"), toDate));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Sort sortFor(String sort, String direction) {
        String property = switch (sort == null ? "" : sort) {
            case "transferNo" -> "transferNo";
            case "transferDate" -> "transferDate";
            case "status" -> "status";
            default -> "id";
        };
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, property);
    }

    private int safeSize(int size) {
        return size == 25 || size == 50 || size == 100 ? size : 10;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
