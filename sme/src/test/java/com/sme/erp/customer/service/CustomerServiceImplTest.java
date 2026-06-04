package com.sme.erp.customer.service;

import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.customer.dto.CustomerDTO;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.mapper.CustomerMapper;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.customer.service.impl.CustomerServiceImpl;
import com.sme.erp.enums.Status;
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
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(customerRepository, new CustomerMapper());
    }

    @Test
    void create_generatesCustomerCodeUsingRowsIncludingSoftDeletedRecords() {
        CustomerDTO dto = new CustomerDTO();
        dto.setName("Acme Trading");
        dto.setOpeningBalance(new BigDecimal("150.00"));
        dto.setCreditLimit(new BigDecimal("500.00"));

        when(customerRepository.findMaxIdIncludingDeleted()).thenReturn(9L);
        when(customerRepository.countByCustomerCodeIncludingDeleted("CUS-0010")).thenReturn(0L);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        CustomerDTO result = service.create(dto);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());

        Customer saved = customerCaptor.getValue();
        assertThat(saved.getCustomerCode()).isEqualTo("CUS-0010");
        assertThat(saved.getCurrentBalance()).isEqualByComparingTo("150.00");
        assertThat(saved.getIsDeleted()).isFalse();
        assertThat(result.getCustomerCode()).isEqualTo("CUS-0010");
    }

    @Test
    void create_rejectsDuplicateCustomerCodeIncludingSoftDeletedRecords() {
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerCode("CUS-0005");
        dto.setName("Acme Trading");

        when(customerRepository.countByCustomerCodeIncludingDeleted("CUS-0005")).thenReturn(1L);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Customer code already exists: CUS-0005");
    }

    @Test
    void getAll_normalizesKeywordAndPassesStatusFilter() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerCode("CUS-0001");
        customer.setName("Acme Trading");
        customer.setStatus(Status.ACTIVE);

        when(customerRepository.search("acme", Status.ACTIVE)).thenReturn(List.of(customer));

        List<CustomerDTO> result = service.getAll("  acme  ", Status.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerCode()).isEqualTo("CUS-0001");
        verify(customerRepository).search("acme", Status.ACTIVE);
    }

    @Test
    void update_preservesExistingOpeningBalanceWhenNotProvided() {
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setCustomerCode("CUS-0001");
        existing.setName("Old Name");
        existing.setCompanyName("Existing Company");
        existing.setPhone("123456");
        existing.setOpeningBalance(new BigDecimal("75.00"));
        existing.setCreditLimit(new BigDecimal("100.00"));
        existing.setCurrentBalance(new BigDecimal("75.00"));
        existing.setStatus(Status.ACTIVE);

        CustomerDTO dto = new CustomerDTO();
        dto.setName("New Name");
        dto.setCreditLimit(new BigDecimal("250.00"));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerDTO result = service.update(1L, dto);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getCustomerCode()).isEqualTo("CUS-0001");
        assertThat(result.getCompanyName()).isEqualTo("Existing Company");
        assertThat(result.getPhone()).isEqualTo("123456");
        assertThat(result.getOpeningBalance()).isEqualByComparingTo("75.00");
        assertThat(result.getCreditLimit()).isEqualByComparingTo("250.00");
    }

    @Test
    void update_clearsOptionalFieldsWhenBlankValuesAreNormalized() {
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setCustomerCode("CUS-0001");
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
        existing.setOpeningBalance(BigDecimal.ZERO);
        existing.setCreditLimit(BigDecimal.ZERO);
        existing.setCurrentBalance(BigDecimal.ZERO);
        existing.setStatus(Status.ACTIVE);

        CustomerDTO dto = new CustomerDTO();
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

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerDTO result = service.update(1L, dto);

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
    }

    @Test
    void delete_deletesExistingCustomerForSoftDeleteHandling() {
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setCustomerCode("CUS-0001");
        existing.setName("Acme Trading");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(customerRepository).delete(existing);
    }

    @Test
    void getById_throwsWhenCustomerDoesNotExist() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with id: 99");
    }
}
