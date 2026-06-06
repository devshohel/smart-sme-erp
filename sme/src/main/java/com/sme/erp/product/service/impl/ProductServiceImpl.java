package com.sme.erp.product.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.product.dto.ProductDTO;
import com.sme.erp.product.entity.*;
import com.sme.erp.product.mapper.ProductMapper;
import com.sme.erp.product.repository.*;
import com.sme.erp.product.service.ProductService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ProductServiceImpl(ProductRepository repository,
                              ProductMapper mapper,
                              ProductCategoryRepository categoryRepository,
                              ProductBrandRepository brandRepository,
                              UomRepository uomRepository,
                              ActivityLogService activityLogService,
                              AuditLogService auditLogService) {
        this.repository = repository;
        this.mapper = mapper;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.uomRepository = uomRepository;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public ProductDTO saveProduct(ProductDTO dto) {

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
}
