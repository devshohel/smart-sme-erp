package com.sme.erp.inventory.service;

import com.sme.erp.inventory.dto.WarehouseDTO;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.mapper.WarehouseMapper;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.inventory.service.impl.WarehouseServiceImpl;
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
class WarehouseServiceImplTest {

    @Mock
    private WarehouseRepository repository;

    private WarehouseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WarehouseServiceImpl(repository, new WarehouseMapper());
    }

    @Test
    void save_createsWarehouse() {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setCode("WH-001");
        dto.setName("Main Warehouse");
        dto.setLocation("Dhaka");
        dto.setActive(true);

        when(repository.existsByWarehouseCode("WH-001")).thenReturn(false);
        when(repository.save(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        WarehouseDTO result = service.save(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("WH-001");
        assertThat(result.getName()).isEqualTo("Main Warehouse");
        assertThat(result.getLocation()).isEqualTo("Dhaka");
        assertThat(result.getActive()).isTrue();
    }

    @Test
    void getAll_returnsMappedWarehouses() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setWarehouseCode("WH-001");
        warehouse.setName("Main Warehouse");
        warehouse.setLocation("Dhaka");
        warehouse.setActive(true);

        when(repository.findAll()).thenReturn(List.of(warehouse));

        List<WarehouseDTO> result = service.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("WH-001");
        assertThat(result.get(0).getName()).isEqualTo("Main Warehouse");
        assertThat(result.get(0).getLocation()).isEqualTo("Dhaka");
    }
}
