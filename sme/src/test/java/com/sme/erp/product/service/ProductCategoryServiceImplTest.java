package com.sme.erp.product.service;

import com.sme.erp.product.dto.ProductCategoryDTO;
import com.sme.erp.product.entity.ProductCategory;
import com.sme.erp.product.mapper.ProductCategoryMapper;
import com.sme.erp.product.repository.ProductCategoryRepository;
import com.sme.erp.product.service.impl.ProductCategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceImplTest {

    @Mock
    private ProductCategoryRepository repository;

    private ProductCategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductCategoryServiceImpl(repository, new ProductCategoryMapper());
    }

    @Test
    void save_createsCategory() {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setCode("CAT-001");
        dto.setCategoryName("Electronics");

        when(repository.existsByCode("CAT-001")).thenReturn(false);
        when(repository.save(any(ProductCategory.class))).thenAnswer(invocation -> {
            ProductCategory saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ProductCategoryDTO result = service.save(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("CAT-001");
        assertThat(result.getCategoryName()).isEqualTo("Electronics");
    }

    @Test
    void getAll_returnsMappedCategories() {
        ProductCategory category = new ProductCategory();
        category.setId(1L);
        category.setCode("CAT-001");
        category.setCategoryName("Electronics");

        when(repository.findAll()).thenReturn(List.of(category));

        List<ProductCategoryDTO> result = service.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("CAT-001");
        assertThat(result.get(0).getCategoryName()).isEqualTo("Electronics");
    }
}
