package com.sme.erp.customer.receipt.service;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.receipt.dto.CustomerReceiptDTO;
import com.sme.erp.customer.receipt.entity.CustomerReceipt;
import com.sme.erp.customer.receipt.enums.CustomerReceiptPaymentMethod;
import com.sme.erp.customer.receipt.enums.CustomerReceiptStatus;
import com.sme.erp.customer.receipt.mapper.CustomerReceiptMapper;
import com.sme.erp.customer.receipt.repository.CustomerReceiptRepository;
import com.sme.erp.customer.receipt.service.impl.CustomerReceiptServiceImpl;
import com.sme.erp.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerReceiptServiceImplTest {

    @Mock
    private CustomerReceiptRepository receiptRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private AuditLogService auditLogService;

    private final CustomerReceiptMapper mapper = new CustomerReceiptMapper();
    private CustomerReceiptServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerReceiptServiceImpl(
                receiptRepository,
                customerRepository,
                mapper,
                journalEntryRepository,
                accountRepository,
                activityLogService,
                auditLogService);
    }

    @Test
    void createDraftReceipt_generatesReceiptNoAndDraftStatus() {
        Customer customer = customer(1L, "CUS-0001", "Acme Trading");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(receiptRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(receiptRepository.existsByReceiptNo("CR-000001")).thenReturn(false);
        when(receiptRepository.save(any(CustomerReceipt.class))).thenAnswer(invocation -> {
            CustomerReceipt saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CustomerReceiptDTO dto = receiptDto(1L, LocalDate.of(2026, 6, 17), CustomerReceiptPaymentMethod.CASH, new BigDecimal("250.00"));
        CustomerReceiptDTO result = service.create(dto);

        ArgumentCaptor<CustomerReceipt> captor = ArgumentCaptor.forClass(CustomerReceipt.class);
        verify(receiptRepository).save(captor.capture());

        assertThat(captor.getValue().getReceiptNo()).isEqualTo("CR-000001");
        assertThat(captor.getValue().getStatus()).isEqualTo(CustomerReceiptStatus.DRAFT);
        assertThat(result.getReceiptNo()).isEqualTo("CR-000001");
        assertThat(result.getStatus()).isEqualTo(CustomerReceiptStatus.DRAFT);
    }

    @Test
    void updateDraftReceipt_updatesFields() {
        Customer customer = customer(1L, "CUS-0001", "Acme Trading");
        CustomerReceipt receipt = receipt(1L, customer, CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("100.00"));

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(receiptRepository.save(any(CustomerReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerReceiptDTO dto = receiptDto(1L, LocalDate.of(2026, 6, 18), CustomerReceiptPaymentMethod.BANK, new BigDecimal("175.00"));
        dto.setReferenceNo("REF-1");
        dto.setNotes("Updated receipt");

        CustomerReceiptDTO result = service.update(1L, dto);

        assertThat(result.getPaymentMethod()).isEqualTo(CustomerReceiptPaymentMethod.BANK);
        assertThat(result.getAmount()).isEqualByComparingTo("175.00");
        assertThat(result.getReferenceNo()).isEqualTo("REF-1");
        assertThat(result.getNotes()).isEqualTo("Updated receipt");
    }

    @Test
    void updatePostedReceipt_isRejected() {
        CustomerReceipt receipt = receipt(1L, customer(1L, "CUS-0001", "Acme Trading"), CustomerReceiptStatus.POSTED, CustomerReceiptPaymentMethod.CASH, new BigDecimal("100.00"));
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> service.update(1L, new CustomerReceiptDTO()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Posted receipt cannot be edited.");
    }

    @Test
    void postReceipt_createsJournalEntryOnce() {
        CustomerReceipt receipt = receipt(1L, customer(1L, "CUS-0001", "Acme Trading"), CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("200.00"));
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("CUSTOMER_RECEIPT", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(0L);
        when(journalEntryRepository.existsByJournalNo("JRN-0001")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash")));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Receivable")).thenReturn(Optional.of(account("1020", "Accounts Receivable")));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(receiptRepository.save(any(CustomerReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerReceiptDTO result = service.post(1L);

        assertThat(result.getStatus()).isEqualTo(CustomerReceiptStatus.POSTED);
        assertThat(result.getPostedAt()).isNotNull();
        verify(journalEntryRepository, times(1)).save(any(JournalEntry.class));
    }

    @Test
    void duplicatePost_doesNotCreateDuplicateJournal() {
        CustomerReceipt receipt = receipt(1L, customer(1L, "CUS-0001", "Acme Trading"), CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("200.00"));
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("CUSTOMER_RECEIPT", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(0L);
        when(journalEntryRepository.existsByJournalNo("JRN-0001")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash")));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Receivable")).thenReturn(Optional.of(account("1020", "Accounts Receivable")));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(receiptRepository.save(any(CustomerReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.post(1L);
        service.post(1L);

        verify(journalEntryRepository, times(1)).save(any(JournalEntry.class));
    }

    @Test
    void cancelDraftReceipt_marksCancelled() {
        CustomerReceipt receipt = receipt(1L, customer(1L, "CUS-0001", "Acme Trading"), CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("100.00"));
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(receiptRepository.save(any(CustomerReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerReceiptDTO result = service.cancel(1L);

        assertThat(result.getStatus()).isEqualTo(CustomerReceiptStatus.CANCELLED);
        assertThat(result.getCancelledAt()).isNotNull();
    }

    @Test
    void cancelPostedReceipt_isRejected() {
        CustomerReceipt receipt = receipt(1L, customer(1L, "CUS-0001", "Acme Trading"), CustomerReceiptStatus.POSTED, CustomerReceiptPaymentMethod.CASH, new BigDecimal("100.00"));
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> service.cancel(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Posted receipt cancellation requires reversal workflow and is not available in Batch-2A.");
    }

    private Customer customer(Long id, String code, String name) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setCustomerCode(code);
        customer.setName(name);
        return customer;
    }

    private CustomerReceipt receipt(Long id, Customer customer, CustomerReceiptStatus status,
                                    CustomerReceiptPaymentMethod paymentMethod, BigDecimal amount) {
        CustomerReceipt receipt = new CustomerReceipt();
        receipt.setId(id);
        receipt.setReceiptNo("CR-000001");
        receipt.setCustomer(customer);
        receipt.setReceiptDate(LocalDate.of(2026, 6, 17));
        receipt.setPaymentMethod(paymentMethod);
        receipt.setAmount(amount);
        receipt.setStatus(status);
        return receipt;
    }

    private CustomerReceiptDTO receiptDto(Long customerId, LocalDate receiptDate,
                                          CustomerReceiptPaymentMethod method, BigDecimal amount) {
        CustomerReceiptDTO dto = new CustomerReceiptDTO();
        dto.setCustomerId(customerId);
        dto.setReceiptDate(receiptDate);
        dto.setPaymentMethod(method);
        dto.setAmount(amount);
        return dto;
    }

    private Account account(String code, String name) {
        Account account = new Account();
        account.setAccountCode(code);
        account.setAccountName(name);
        return account;
    }
}
