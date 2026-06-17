package com.sme.erp.customer.service;

import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.customer.dto.CustomerDTO;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.mapper.CustomerMapper;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.customer.receipt.mapper.CustomerReceiptMapper;
import com.sme.erp.customer.receipt.entity.CustomerReceipt;
import com.sme.erp.customer.receipt.repository.CustomerReceiptRepository;
import com.sme.erp.customer.receipt.enums.CustomerReceiptPaymentMethod;
import com.sme.erp.customer.receipt.enums.CustomerReceiptStatus;
import com.sme.erp.customer.service.impl.CustomerServiceImpl;
import com.sme.erp.enums.Status;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.entity.SalesReturn;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesReturnRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private SalesInvoiceRepository salesInvoiceRepository;
    @Mock
    private SalesReturnRepository salesReturnRepository;
    @Mock
    private CustomerReceiptRepository customerReceiptRepository;
    @Mock
    private CustomerReceiptMapper customerReceiptMapper;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(
                customerRepository,
                new CustomerMapper(),
                activityLogService,
                auditLogService,
                salesInvoiceRepository,
                salesReturnRepository,
                customerReceiptRepository,
                customerReceiptMapper);
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
    void searchPage_returnsPagedCustomers() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerCode("CUS-0001");
        customer.setName("Acme Trading");
        customer.setStatus(Status.ACTIVE);

        when(customerRepository.searchPage(org.mockito.ArgumentMatchers.eq("acme"), org.mockito.ArgumentMatchers.eq(Status.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(customer)));
        when(salesInvoiceRepository.sumDueByCustomerIds(List.of(1L)))
                .thenReturn(List.<Object[]>of(new Object[] { 1L, new BigDecimal("125.50") }));

        var result = service.searchPage(" acme ", Status.ACTIVE, 0, 10, "createdAt", "desc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDueBalance()).isEqualByComparingTo("125.50");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(customerRepository).searchPage(org.mockito.ArgumentMatchers.eq("acme"), org.mockito.ArgumentMatchers.eq(Status.ACTIVE), pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        assertThat(sort.getOrderFor("createdAt")).isNotNull();
        assertThat(sort.getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(sort.getOrderFor("id")).isNotNull();
        assertThat(sort.getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
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

    @Test
    void getLedger_buildsRunningBalanceFromOpeningInvoicesReceiptsAndReturns() {
        Customer customer = customer("CUS-0001", "Acme Trading", new BigDecimal("100.00"));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        SalesInvoice invoice = invoice(customer, 10L, "SI-001", "2026-01-05T10:00:00", "250.00", "50.00");
        CustomerReceipt receipt = receipt(customer, 20L, "CR-001", LocalDate.of(2026, 1, 6), "75.00");
        SalesReturn salesReturn = salesReturn(customer, 30L, "SR-001", "2026-01-07T10:00:00", "25.00");

        when(salesInvoiceRepository.findPostedByCustomerForLedger(1L, null, null)).thenReturn(List.of(invoice));
        when(customerReceiptRepository.findPostedByCustomerForLedger(1L, null, null)).thenReturn(List.of(receipt));
        when(salesReturnRepository.findByCustomerForLedger(1L, null, null)).thenReturn(List.of(salesReturn));

        var ledger = service.getLedger(1L, null, null);

        assertThat(ledger.getEntries()).hasSize(4);
        assertThat(ledger.getOpeningBalance()).isEqualByComparingTo("100.00");
        assertThat(ledger.getEntries().get(1).getReferenceType()).isEqualTo("SALES_INVOICE");
        assertThat(ledger.getEntries().get(1).getRunningBalance()).isEqualByComparingTo("350.00");
        assertThat(ledger.getEntries().get(2).getReferenceType()).isEqualTo("CUSTOMER_RECEIPT");
        assertThat(ledger.getEntries().get(2).getRunningBalance()).isEqualByComparingTo("275.00");
        assertThat(ledger.getEntries().get(3).getReferenceType()).isEqualTo("SALES_RETURN");
        assertThat(ledger.getClosingBalance()).isEqualByComparingTo("250.00");
    }

    @Test
    void getAging_bucketsDueInvoicesByInvoiceAge() {
        Customer customer = customer("CUS-0001", "Acme Trading", BigDecimal.ZERO);
        SalesInvoice current = invoice(customer, 10L, "SI-001", LocalDate.now().atStartOfDay().toString(), "100.00", "100.00");
        SalesInvoice days30 = invoice(customer, 11L, "SI-002", LocalDate.now().minusDays(20).atStartOfDay().toString(), "200.00", "200.00");
        SalesInvoice days60 = invoice(customer, 12L, "SI-003", LocalDate.now().minusDays(45).atStartOfDay().toString(), "300.00", "300.00");
        SalesInvoice days90 = invoice(customer, 13L, "SI-004", LocalDate.now().minusDays(75).atStartOfDay().toString(), "400.00", "400.00");
        SalesInvoice over90 = invoice(customer, 14L, "SI-005", LocalDate.now().minusDays(120).atStartOfDay().toString(), "500.00", "500.00");

        when(salesInvoiceRepository.findDueInvoicesForAging(null, null, null))
                .thenReturn(List.of(current, days30, days60, days90, over90));

        var report = service.getAging(null, null, null);

        assertThat(report.getRows()).hasSize(1);
        var row = report.getRows().get(0);
        assertThat(row.getCurrent()).isEqualByComparingTo("100.00");
        assertThat(row.getDays1To30()).isEqualByComparingTo("200.00");
        assertThat(row.getDays31To60()).isEqualByComparingTo("300.00");
        assertThat(row.getDays61To90()).isEqualByComparingTo("400.00");
        assertThat(row.getDays90Plus()).isEqualByComparingTo("500.00");
        assertThat(row.getTotalDue()).isEqualByComparingTo("1500.00");
        assertThat(report.getTotalDue()).isEqualByComparingTo("1500.00");
    }

    private Customer customer(String code, String name, BigDecimal openingBalance) {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerCode(code);
        customer.setName(name);
        customer.setOpeningBalance(openingBalance);
        customer.setCurrentBalance(openingBalance);
        customer.setCreditLimit(BigDecimal.ZERO);
        customer.setStatus(Status.ACTIVE);
        customer.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return customer;
    }

    private SalesInvoice invoice(Customer customer, Long id, String invoiceNo, String saleDate, String netTotal, String dueAmount) {
        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(id);
        invoice.setCustomer(customer);
        invoice.setInvoiceNo(invoiceNo);
        invoice.setSaleDate(LocalDateTime.parse(saleDate));
        invoice.setNetTotal(new BigDecimal(netTotal));
        invoice.setDueAmount(new BigDecimal(dueAmount));
        invoice.setPaidAmount(BigDecimal.ZERO);
        return invoice;
    }

    private CustomerReceipt receipt(Customer customer, Long id, String receiptNo, LocalDate receiptDate, String amount) {
        CustomerReceipt receipt = new CustomerReceipt();
        receipt.setId(id);
        receipt.setCustomer(customer);
        receipt.setReceiptNo(receiptNo);
        receipt.setReceiptDate(receiptDate);
        receipt.setPaymentMethod(CustomerReceiptPaymentMethod.CASH);
        receipt.setAmount(new BigDecimal(amount));
        receipt.setStatus(CustomerReceiptStatus.POSTED);
        return receipt;
    }

    private SalesReturn salesReturn(Customer customer, Long id, String returnCode, String returnDate, String totalAmount) {
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setId(id);
        salesReturn.setCustomer(customer);
        salesReturn.setReturnCode(returnCode);
        salesReturn.setReturnDate(LocalDateTime.parse(returnDate));
        salesReturn.setTotalAmount(new BigDecimal(totalAmount));
        return salesReturn;
    }
}
