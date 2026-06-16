package com.sme.erp.product.service;

import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.file.service.FileStorageService;
import com.sme.erp.product.dto.ProductDTO;
import com.sme.erp.product.entity.Product;
import com.sme.erp.product.mapper.ProductMapper;
import com.sme.erp.product.repository.ProductBrandRepository;
import com.sme.erp.product.repository.ProductCategoryRepository;
import com.sme.erp.product.repository.ProductRepository;
import com.sme.erp.product.repository.UomRepository;
import com.sme.erp.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductCategoryRepository categoryRepository;
    @Mock
    private ProductBrandRepository brandRepository;
    @Mock
    private UomRepository uomRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private FileStorageService fileStorageService;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(
                productRepository,
                new ProductMapper(),
                categoryRepository,
                brandRepository,
                uomRepository,
                activityLogService,
                auditLogService,
                fileStorageService);
    }

    @Test
    void saveProduct_updatePreservesExistingFieldsNotPresentInDto() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setProductCode("PRD-001");
        existing.setProductName("Old Name");
        existing.setSku("SKU-001");
        existing.setPurchasePrice(new BigDecimal("10.00"));
        existing.setSalePrice(new BigDecimal("12.00"));
        existing.setBarcode("BAR-001");

        ProductDTO dto = new ProductDTO();
        dto.setId(1L);
        dto.setProductName("New Name");
        dto.setSku("SKU-001");
        dto.setPurchasePrice(new BigDecimal("11.00"));
        dto.setSalePrice(new BigDecimal("13.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySkuAndIdNot("SKU-001", 1L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDTO result = service.saveProduct(dto);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product saved = productCaptor.getValue();
        assertThat(saved).isSameAs(existing);
        assertThat(saved.getProductCode()).isEqualTo("PRD-001");
        assertThat(saved.getBarcode()).isEqualTo("BAR-001");
        assertThat(saved.getProductName()).isEqualTo("New Name");
        assertThat(saved.getPurchasePrice()).isEqualByComparingTo("11.00");
        assertThat(result.getProductCode()).isEqualTo("PRD-001");
        assertThat(result.getProductName()).isEqualTo("New Name");
    }

    @Test
    void getAllProducts_returnsMappedProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setProductCode("PRD-001");
        product.setProductName("Test Product");
        product.setSku("SKU-001");
        product.setPurchasePrice(new BigDecimal("10.00"));
        product.setSalePrice(new BigDecimal("15.00"));

        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductDTO> result = service.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductCode()).isEqualTo("PRD-001");
        assertThat(result.get(0).getProductName()).isEqualTo("Test Product");
    }

    @Test
    void saveProduct_createSavesNewProduct() {
        ProductDTO dto = new ProductDTO();
        dto.setProductCode("PRD-101");
        dto.setProductName("New Product");
        dto.setSku("SKU-101");
        dto.setPurchasePrice(new BigDecimal("10.00"));
        dto.setSalePrice(new BigDecimal("15.00"));

        when(productRepository.existsBySku("SKU-101")).thenReturn(false);
        when(productRepository.existsByProductCode("PRD-101")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ProductDTO result = service.saveProduct(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getProductCode()).isEqualTo("PRD-101");
        assertThat(result.getProductName()).isEqualTo("New Product");
    }

    @Test
    void saveProduct_createRejectsDuplicateProductCode() {
        ProductDTO dto = new ProductDTO();
        dto.setProductCode("PRD-100");
        dto.setProductName("Test Product");
        dto.setSku("SKU-100");
        dto.setPurchasePrice(new BigDecimal("10.00"));
        dto.setSalePrice(new BigDecimal("15.00"));

        when(productRepository.existsBySku("SKU-100")).thenReturn(false);
        when(productRepository.existsByProductCode("PRD-100")).thenReturn(true);

        assertThatThrownBy(() -> service.saveProduct(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Product code already exists: PRD-100");
    }
}
