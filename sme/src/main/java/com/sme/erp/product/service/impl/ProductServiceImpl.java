package com.sme.erp.product.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.enums.Status;
import com.sme.erp.file.dto.StoredFileDTO;
import com.sme.erp.file.service.FileStorageService;
import com.sme.erp.product.dto.ProductDTO;
import com.sme.erp.product.dto.ProductPageDTO;
import com.sme.erp.product.dto.ProductStatsDTO;
import com.sme.erp.product.entity.*;
import com.sme.erp.product.mapper.ProductMapper;
import com.sme.erp.product.repository.*;
import com.sme.erp.product.service.ProductService;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final ProductCategoryRepository categoryRepository;
    private final ProductBrandRepository brandRepository;
    private final UomRepository uomRepository;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private final FileStorageService fileStorageService;

    public ProductServiceImpl(ProductRepository repository,
                              ProductMapper mapper,
                              ProductCategoryRepository categoryRepository,
                              ProductBrandRepository brandRepository,
                              UomRepository uomRepository,
                              ActivityLogService activityLogService,
                              AuditLogService auditLogService,
                              FileStorageService fileStorageService) {
        this.repository = repository;
        this.mapper = mapper;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.uomRepository = uomRepository;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public ProductDTO saveProduct(ProductDTO dto) {
        return saveProduct(dto, null);
    }

    @Override
    @Transactional
    public ProductDTO saveProduct(ProductDTO dto, MultipartFile image) {
        validateProductBusinessRules(dto);
        String normalizedProductCode = RequestValueUtils.normalize(dto.getProductCode());
        dto.setProductCode(normalizedProductCode);
        dto.setSku(RequestValueUtils.normalizeRequired(dto.getSku(), "SKU"));
        dto.setProductName(RequestValueUtils.normalizeRequired(dto.getProductName(), "Product name"));
        Product product = getProductForSave(dto.getId());
        boolean isCreate = product.getId() == null;
        ProductDTO oldData = isCreate ? null : mapper.toDTO(product);
        validateSkuUnique(dto.getSku(), dto.getId());
        if (dto.getId() == null) {
            product.setProductCode(resolveProductCodeForCreate(normalizedProductCode));
        } else if (normalizedProductCode != null) {
            validateProductCodeUnique(normalizedProductCode, dto.getId());
            product.setProductCode(normalizedProductCode);
        } else if (product.getProductCode() == null || product.getProductCode().isBlank()) {
            product.setProductCode(generateProductCode());
        }
        mapper.updateEntity(dto, product);

        // ✅ Category
        if (dto.getCategoryId() != null) {
            product.setCategory(findCategoryById(dto.getCategoryId()));
        } else {
            product.setCategory(null);
        }

        // ✅ Brand
        if (dto.getBrandId() != null) {
            product.setBrand(findBrandById(dto.getBrandId()));
        } else {
            product.setBrand(null);
        }

        // ✅ UOM
        if (dto.getUomId() != null) {
            product.setUom(findUomById(dto.getUomId()));
        } else {
            product.setUom(null);
        }

        if (image != null && !image.isEmpty()) {
            applyProductImage(product, fileStorageService.storeProductImage(image));
        }

        Product saved = repository.save(product);
        ProductDTO savedDto = mapper.toDTO(saved);
        String action = isCreate ? "CREATE" : "UPDATE";
        activityLogService.log(isCreate ? "PRODUCT_CREATE" : "PRODUCT_UPDATE", "PRODUCT", "products", savedDto.getId(), "Saved product " + savedDto.getProductName());
        auditLogService.log("products", savedDto.getId(), auditLogService.toJson(oldData), auditLogService.toJson(savedDto), action);
        return savedDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getDeletedProducts() {
        return repository.findDeletedProducts()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageDTO searchProducts(String keyword, Long categoryId, Long brandId, Status status, int page, int size, String sort, String direction) {
        int safePage = Math.max(page, 0);
        int safeSize = List.of(10, 25, 50, 100).contains(size) ? size : 10;
        Pageable pageable = PageRequest.of(safePage, safeSize, resolveSort(sort, direction));
        Page<Product> result = repository.findAll(productSpec(keyword, categoryId, brandId, status), pageable);
        List<ProductDTO> rows = result.getContent().stream().map(mapper::toDTO).collect(Collectors.toList());
        return new ProductPageDTO(rows, result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductStatsDTO getStats() {
        return new ProductStatsDTO(
                repository.count(),
                repository.countByStatus(Status.ACTIVE),
                repository.countByStatus(Status.INACTIVE),
                repository.countByImageUrlIsNullOrImageUrl(""));
    }

    @Override
    @Transactional
    public int updateStatusBulk(List<Long> productIds, Status status) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BadRequestException("Product ids are required");
        }
        if (status == null) {
            throw new BadRequestException("Status is required");
        }

        List<Product> products = repository.findAllById(productIds);
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("No products found for selected ids");
        }
        products.forEach(product -> product.setStatus(status));
        repository.saveAll(products);
        activityLogService.log("PRODUCT_BULK_STATUS_UPDATE", "PRODUCT", "products", null, "Updated " + products.size() + " products to " + status);
        return products.size();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getById(Long id) {
        return mapper.toDTO(findProductById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findProductById(id);
        ProductDTO oldData = mapper.toDTO(product);
        repository.delete(product);
        activityLogService.log("PRODUCT_DELETE", "PRODUCT", "products", id, "Deleted product " + oldData.getProductName());
        auditLogService.log("products", id, auditLogService.toJson(oldData), null, "DELETE");
    }

    @Override
    @Transactional
    public ProductDTO restore(Long id) {
        int updated = repository.restoreById(id);
        if (updated == 0) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        Product restored = findProductById(id);
        ProductDTO restoredDto = mapper.toDTO(restored);
        activityLogService.log("PRODUCT_RESTORE", "PRODUCT", "products", id, "Restored product " + restoredDto.getProductName());
        auditLogService.log("products", id, null, auditLogService.toJson(restoredDto), "RESTORE");
        return restoredDto;
    }

    private String resolveProductCodeForCreate(String requestedCode) {
        if (requestedCode != null) {
            validateProductCodeUnique(requestedCode, null);
            return requestedCode;
        }
        return generateProductCode();
    }

    private Product getProductForSave(Long id) {
        return id == null ? new Product() : findProductById(id);
    }

    private Product findProductById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductCategory findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private ProductBrand findBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
    }

    private Uom findUomById(Long id) {
        return uomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UOM not found with id: " + id));
    }

    private void validateProductCodeUnique(String productCode, Long currentId) {
        boolean exists = currentId == null
                ? repository.existsByProductCode(productCode)
                : repository.existsByProductCodeAndIdNot(productCode, currentId);

        if (exists) {
            throw new DuplicateResourceException("Product code already exists: " + productCode);
        }
    }

    private String generateProductCode() {
        String productCode;
        do {
            productCode = "PRD-" + System.currentTimeMillis();
        } while (repository.existsByProductCode(productCode));
        return productCode;
    }
    private void validateSkuUnique(String sku, Long currentId) {
        boolean exists = currentId == null
                ? repository.existsBySku(sku)
                : repository.existsBySkuAndIdNot(sku, currentId);

        if (exists) {
            throw new DuplicateResourceException("SKU already exists: " + sku);
        }
    }

    private void validateProductBusinessRules(ProductDTO dto) {
        if (dto.getPurchasePrice() != null && dto.getPurchasePrice().signum() < 0) {
            throw new BadRequestException("Purchase price cannot be negative");
        }
        if (dto.getSalePrice() != null && dto.getSalePrice().signum() < 0) {
            throw new BadRequestException("Sale price cannot be negative");
        }
    }

    private void applyProductImage(Product product, StoredFileDTO storedFile) {
        product.setImageOriginalFilename(storedFile.getOriginalFilename());
        product.setImageStoredFilename(storedFile.getStoredFilename());
        product.setImageContentType(storedFile.getContentType());
        product.setImageSize(storedFile.getFileSize());
        product.setImagePath(storedFile.getStoragePath());
        product.setImageUrl(storedFile.getPublicUrl());
    }

    private Specification<Product> productSpec(String keyword, Long categoryId, Long brandId, Status status) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("category", JoinType.LEFT);
                root.fetch("brand", JoinType.LEFT);
                root.fetch("uom", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();
            String normalizedKeyword = RequestValueUtils.normalize(keyword);
            if (normalizedKeyword != null) {
                String value = "%" + normalizedKeyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("productName")), value),
                        cb.like(cb.lower(root.get("sku")), value),
                        cb.like(cb.lower(root.get("barcode")), value)));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort resolveSort(String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String property = switch (sort == null ? "" : sort) {
            case "sku" -> "sku";
            case "category" -> "category.categoryName";
            case "purchasePrice" -> "purchasePrice";
            case "salePrice" -> "salePrice";
            case "status" -> "status";
            default -> "productName";
        };
        return Sort.by(sortDirection, property).and(Sort.by(Sort.Direction.ASC, "id"));
    }
}
