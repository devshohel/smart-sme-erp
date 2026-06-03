package com.sme.erp.product.service;

import com.sme.erp.product.dto.ProductBrandDTO;
import com.sme.erp.product.entity.ProductBrand;
import com.sme.erp.product.mapper.ProductBrandMapper;
import com.sme.erp.product.repository.ProductBrandRepository;
import com.sme.erp.product.service.impl.ProductBrandServiceImpl;
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
class ProductBrandServiceImplTest {

    @Mock
    private ProductBrandRepository repository;

    private ProductBrandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductBrandServiceImpl(repository, new ProductBrandMapper());
    }

    @Test
    void save_createsBrand() {
        ProductBrandDTO dto = new ProductBrandDTO();
        dto.setCode("BR-001");
        dto.setBrandName("Acme");

        when(repository.existsByCode("BR-001")).thenReturn(false);
        when(repository.save(any(ProductBrand.class))).thenAnswer(invocation -> {
            ProductBrand saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ProductBrandDTO result = service.save(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("BR-001");
        assertThat(result.getBrandName()).isEqualTo("Acme");
    }

    @Test
    void getAll_returnsMappedBrands() {
        ProductBrand brand = new ProductBrand();
        brand.setId(1L);
        brand.setCode("BR-001");
        brand.setBrandName("Acme");

        when(repository.findAll()).thenReturn(List.of(brand));

        List<ProductBrandDTO> result = service.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("BR-001");
        assertThat(result.get(0).getBrandName()).isEqualTo("Acme");
    }
}
