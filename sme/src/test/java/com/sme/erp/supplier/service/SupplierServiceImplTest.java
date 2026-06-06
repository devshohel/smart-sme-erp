package com.sme.erp.supplier.service;

import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.enums.Status;
import com.sme.erp.supplier.dto.SupplierDTO;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.mapper.SupplierMapper;
import com.sme.erp.supplier.repository.SupplierRepository;
import com.sme.erp.supplier.service.impl.SupplierServiceImpl;
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
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private AuditLogService auditLogService;

    private SupplierServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SupplierServiceImpl(supplierRepository, new SupplierMapper(), activityLogService, auditLogService);
    }

    @Test
    void create_generatesSupplierCodeUsingRowsIncludingSoftDeletedRecords() {
        SupplierDTO dto = new SupplierDTO();
        dto.setName("Global Supplies");
        dto.setOpeningBalance(new BigDecimal("150.00"));

        when(supplierRepository.findMaxIdIncludingDeleted()).thenReturn(9L);
        when(supplierRepository.countBySupplierCodeIncludingDeleted("SUP-0010")).thenReturn(0L);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        SupplierDTO result = service.create(dto);

        ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(supplierCaptor.capture());

        Supplier saved = supplierCaptor.getValue();
        assertThat(saved.getSupplierCode()).isEqualTo("SUP-0010");
        assertThat(saved.getCurrentBalance()).isEqualByComparingTo("150.00");
        assertThat(saved.getIsDeleted()).isFalse();
        assertThat(result.getSupplierCode()).isEqualTo("SUP-0010");
    }

    @Test
    void create_rejectsDuplicateSupplierCodeIncludingSoftDeletedRecords() {
        SupplierDTO dto = new SupplierDTO();
        dto.setSupplierCode("SUP-0005");
        dto.setName("Global Supplies");

        when(supplierRepository.countBySupplierCodeIncludingDeleted("SUP-0005")).thenReturn(1L);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Supplier code already exists: SUP-0005");
    }

    @Test
    void getAll_normalizesKeywordAndPassesStatusFilter() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierCode("SUP-0001");
        supplier.setName("Global Supplies");
        supplier.setStatus(Status.ACTIVE);

        when(supplierRepository.search("global", Status.ACTIVE)).thenReturn(List.of(supplier));

        List<SupplierDTO> result = service.getAll("  global  ", Status.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSupplierCode()).isEqualTo("SUP-0001");
        verify(supplierRepository).search("global", Status.ACTIVE);
    }

    @Test
    void update_preservesExistingOpeningBalanceWhenNotProvided() {
        Supplier existing = new Supplier();
        existing.setId(1L);
        existing.setSupplierCode("SUP-0001");
        existing.setName("Old Name");
        existing.setCompanyName("Existing Company");
        existing.setPhone("123456");
        existing.setOpeningBalance(new BigDecimal("75.00"));
        existing.setCurrentBalance(new BigDecimal("75.00"));
        existing.setStatus(Status.ACTIVE);

        SupplierDTO dto = new SupplierDTO();
        dto.setName("New Name");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierDTO result = service.update(1L, dto);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getSupplierCode()).isEqualTo("SUP-0001");
        assertThat(result.getCompanyName()).isEqualTo("Existing Company");
        assertThat(result.getPhone()).isEqualTo("123456");
        assertThat(result.getOpeningBalance()).isEqualByComparingTo("75.00");
    }

    @Test
    void update_clearsOptionalFieldsWhenBlankValuesAreNormalized() {
        Supplier existing = new Supplier();
        existing.setId(1L);
        existing.setSupplierCode("SUP-0001");
        existing.setName("Old Name");
        existing.setCompanyName("Old Company");
        existing.setContactPerson("Old Contact");
        existing.setPhone("123456");
        existing.setEmail("old@example.com");
        existing.setAddress("Old Address");
        existing.setCity("Old City");
        existing.setCountry("Old Country");
        existing.setPostalCode("1000");
        existing.setTaxNumber("TAX-1");
        existing.setBankAccount("Bank-1");
        existing.setPaymentTerms("Net 30");
        existing.setOpeningBalance(BigDecimal.ZERO);
        existing.setCurrentBalance(BigDecimal.ZERO);
        existing.setStatus(Status.ACTIVE);

        SupplierDTO dto = new SupplierDTO();
        dto.setName("Updated Name");
        dto.setCompanyName(" ");
        dto.setContactPerson(" ");
        dto.setPhone(" ");
        dto.setEmail(" ");
        dto.setAddress(" ");
        dto.setCity(" ");
        dto.setCountry(" ");
        dto.setPostalCode(" ");
        dto.setTaxNumber(" ");
        dto.setBankAccount(" ");
        dto.setPaymentTerms(" ");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierDTO result = service.update(1L, dto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getCompanyName()).isNull();
        assertThat(result.getContactPerson()).isNull();
        assertThat(result.getPhone()).isNull();
        assertThat(result.getEmail()).isNull();
        assertThat(result.getAddress()).isNull();
        assertThat(result.getCity()).isNull();
        assertThat(result.getCountry()).isNull();
        assertThat(result.getPostalCode()).isNull();
        assertThat(result.getTaxNumber()).isNull();
        assertThat(result.getBankAccount()).isNull();
        assertThat(result.getPaymentTerms()).isNull();
    }

    @Test
    void delete_deletesExistingSupplierForSoftDeleteHandling() {
        Supplier existing = new Supplier();
        existing.setId(1L);
        existing.setSupplierCode("SUP-0001");
        existing.setName("Global Supplies");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(supplierRepository).delete(existing);
    }

    @Test
    void getById_throwsWhenSupplierDoesNotExist() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Supplier not found with id: 99");
    }
}
