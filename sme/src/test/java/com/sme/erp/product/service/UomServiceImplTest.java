package com.sme.erp.product.service;

import com.sme.erp.product.dto.UomDTO;
import com.sme.erp.product.entity.Uom;
import com.sme.erp.product.mapper.UomMapper;
import com.sme.erp.product.repository.UomRepository;
import com.sme.erp.product.service.impl.UomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UomServiceImplTest {

    @Mock
    private UomRepository repository;

    private UomServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UomServiceImpl(repository, new UomMapper());
    }

    @Test
    void save_createsUom() {
        UomDTO dto = new UomDTO();
        dto.setCode("PCS");
        dto.setName("Pieces");
        dto.setConversionFactor(BigDecimal.ONE);

        when(repository.existsByCode("PCS")).thenReturn(false);
        when(repository.save(any(Uom.class))).thenAnswer(invocation -> {
            Uom saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UomDTO result = service.save(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("PCS");
        assertThat(result.getName()).isEqualTo("Pieces");
    }

    @Test
    void getAll_returnsMappedUoms() {
        Uom uom = new Uom();
        uom.setId(1L);
        uom.setCode("PCS");
        uom.setName("Pieces");
        uom.setConversionFactor(BigDecimal.ONE);

        when(repository.findAll()).thenReturn(List.of(uom));

        List<UomDTO> result = service.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("PCS");
        assertThat(result.get(0).getName()).isEqualTo("Pieces");
    }
}
