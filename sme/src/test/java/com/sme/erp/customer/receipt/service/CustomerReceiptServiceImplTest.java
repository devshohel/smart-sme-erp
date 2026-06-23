package com.sme.erp.customer.receipt.service;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.receipt.dto.CustomerReceiptAllocationDTO;
import com.sme.erp.customer.receipt.dto.CustomerReceiptDTO;
import com.sme.erp.customer.receipt.entity.CustomerReceipt;
import com.sme.erp.customer.receipt.enums.CustomerReceiptAllocationMode;
import com.sme.erp.customer.receipt.enums.CustomerReceiptPaymentMethod;
import com.sme.erp.customer.receipt.enums.CustomerReceiptStatus;
import com.sme.erp.customer.receipt.mapper.CustomerReceiptAllocationMapper;
import com.sme.erp.customer.receipt.mapper.CustomerReceiptMapper;
import com.sme.erp.customer.receipt.repository.CustomerReceiptAllocationRepository;
import com.sme.erp.customer.receipt.repository.CustomerReceiptRepository;
import com.sme.erp.customer.receipt.service.impl.CustomerReceiptServiceImpl;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.enums.SalesPaymentStatus;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
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
    private CustomerReceiptAllocationRepository allocationRepository;
    @Mock
    private SalesInvoiceRepository salesInvoiceRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private AuditLogService auditLogService;

    private final CustomerReceiptMapper mapper = new CustomerReceiptMapper(new CustomerReceiptAllocationMapper());
    private CustomerReceiptServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerReceiptServiceImpl(
                receiptRepository,
                customerRepository,
                mapper,
                new CustomerReceiptAllocationMapper(),
                journalEntryRepository,
                accountRepository,
                allocationRepository,
                salesInvoiceRepository,
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
        assertThat(result.getAllocationMode()).isEqualTo(CustomerReceiptAllocationMode.AUTO);
        assertThat(result.getTotalAllocatedAmount()).isEqualByComparingTo("0.00");
        assertThat(result.getUnappliedAmount()).isEqualByComparingTo("250.00");
    }

    @Test
    void updateDraftReceipt_updatesFields() {
        Customer customer = customer(1L, "CUS-0001", "Acme Trading");
        CustomerReceipt receipt = receipt(1L, customer, CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("100.00"));

        when(receiptRepository.findDetailedById(1L)).thenReturn(Optional.of(receipt));
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
        when(receiptRepository.findDetailedById(1L)).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> service.update(1L, new CustomerReceiptDTO()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Posted receipt cannot be edited.");
    }

    @Test
    void autoAllocation_appliesOldestInvoicesFirstAndKeepsUnappliedAmount() {
        Customer customer = customer(1L, "CUS-0001", "Acme Trading");
        CustomerReceipt receipt = receipt(1L, customer, CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("150.00"));
        receipt.setAllocationMode(CustomerReceiptAllocationMode.AUTO);

        SalesInvoice first = invoice(11L, customer, "INV-001", new BigDecimal("100.00"), new BigDecimal("0.00"), new BigDecimal("100.00"));
        SalesInvoice second = invoice(12L, customer, "INV-002", new BigDecimal("80.00"), new BigDecimal("20.00"), new BigDecimal("60.00"));

        when(receiptRepository.findDetailedById(1L)).thenReturn(Optional.of(receipt));
        when(salesInvoiceRepository.findUnpaidByCustomerIdOrderBySaleDateAscIdAsc(1L)).thenReturn(List.of(first, second));
        when(salesInvoiceRepository.findById(11L)).thenReturn(Optional.of(first));
        when(salesInvoiceRepository.findById(12L)).thenReturn(Optional.of(second));
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("CUSTOMER_RECEIPT", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(0L);
        when(journalEntryRepository.existsByJournalNo("JRN-0001")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash")));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Receivable")).thenReturn(Optional.of(account("1020", "Accounts Receivable")));
        when(receiptRepository.save(any(CustomerReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerReceiptDTO result = service.post(1L);

        assertThat(result.getTotalAllocatedAmount()).isEqualByComparingTo("150.00");
        assertThat(result.getUnappliedAmount()).isEqualByComparingTo("0.00");
        assertThat(first.getPaidAmount()).isEqualByComparingTo("100.00");
        assertThat(first.getDueAmount()).isEqualByComparingTo("0.00");
        assertThat(first.getPaymentStatus()).isEqualTo(SalesPaymentStatus.PAID);
        assertThat(second.getPaidAmount()).isEqualByComparingTo("70.00");
        assertThat(second.getDueAmount()).isEqualByComparingTo("10.00");
        assertThat(second.getPaymentStatus()).isEqualTo(SalesPaymentStatus.PARTIAL);
    }

    @Test
    void partialAllocation_transitionsInvoiceToPartialPaidAndUpdatesDue() {
        Customer customer = customer(1L, "CUS-0001", "Acme Trading");
        CustomerReceipt receipt = receipt(1L, customer, CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("40.00"));
        receipt.setAllocationMode(CustomerReceiptAllocationMode.AUTO);
        SalesInvoice invoice = invoice(11L, customer, "INV-001", new BigDecimal("100.00"), BigDecimal.ZERO, new BigDecimal("100.00"));

        stubReceiptPosting(receipt, List.of(invoice));

        service.post(1L);

        assertThat(invoice.getPaidAmount()).isEqualByComparingTo("40.00");
        assertThat(invoice.getDueAmount()).isEqualByComparingTo("60.00");
        assertThat(invoice.getPaymentStatus()).isEqualTo(SalesPaymentStatus.PARTIAL);
        assertThat(invoice.getStatus()).isEqualTo(SalesInvoiceStatus.PARTIAL_PAID);
    }

    @Test
    void fullAllocation_transitionsInvoiceToPaidAndClearsDue() {
        Customer customer = customer(1L, "CUS-0001", "Acme Trading");
        CustomerReceipt receipt = receipt(1L, customer, CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("100.00"));
        receipt.setAllocationMode(CustomerReceiptAllocationMode.AUTO);
        SalesInvoice invoice = invoice(11L, customer, "INV-001", new BigDecimal("100.00"), BigDecimal.ZERO, new BigDecimal("100.00"));

        stubReceiptPosting(receipt, List.of(invoice));

        service.post(1L);

        assertThat(invoice.getPaidAmount()).isEqualByComparingTo("100.00");
        assertThat(invoice.getDueAmount()).isEqualByComparingTo("0.00");
        assertThat(invoice.getPaymentStatus()).isEqualTo(SalesPaymentStatus.PAID);
        assertThat(invoice.getStatus()).isEqualTo(SalesInvoiceStatus.PAID);
    }

    @Test
    void allocationAfterSalesReturnPreservesReducedDueAmount() {
        Customer customer = customer(1L, "CUS-0001", "Acme Trading");
        CustomerReceipt receipt = receipt(1L, customer, CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("40.00"));
        receipt.setAllocationMode(CustomerReceiptAllocationMode.AUTO);
        SalesInvoice invoice = invoice(11L, customer, "INV-001", new BigDecimal("100.00"), BigDecimal.ZERO, new BigDecimal("70.00"));

        stubReceiptPosting(receipt, List.of(invoice));

        service.post(1L);

        assertThat(invoice.getPaidAmount()).isEqualByComparingTo("40.00");
        assertThat(invoice.getDueAmount()).isEqualByComparingTo("30.00");
        assertThat(invoice.getPaymentStatus()).isEqualTo(SalesPaymentStatus.PARTIAL);
        assertThat(invoice.getStatus()).isEqualTo(SalesInvoiceStatus.PARTIAL_PAID);
    }

    @Test
    void manualAllocation_updatesReceiptAllocationSummary() {
        Customer customer = customer(1L, "CUS-0001", "Acme Trading");
        CustomerReceipt receipt = receipt(1L, customer, CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("120.00"));
        receipt.setAllocationMode(CustomerReceiptAllocationMode.MANUAL);

        CustomerReceiptAllocationDTO line = new CustomerReceiptAllocationDTO();
        line.setSalesInvoiceId(11L);
        line.setAllocatedAmount(new BigDecimal("60.00"));
        CustomerReceiptDTO dto = receiptDto(1L, LocalDate.of(2026, 6, 17), CustomerReceiptPaymentMethod.CASH, new BigDecimal("120.00"));
        dto.setAllocationMode(CustomerReceiptAllocationMode.MANUAL);
        dto.setAllocations(List.of(line));

        SalesInvoice invoice = invoice(11L, customer, "INV-001", new BigDecimal("100.00"), new BigDecimal("0.00"), new BigDecimal("100.00"));

        when(receiptRepository.findDetailedById(1L)).thenReturn(Optional.of(receipt));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(salesInvoiceRepository.findById(11L)).thenReturn(Optional.of(invoice));
        when(receiptRepository.save(any(CustomerReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerReceiptDTO result = service.update(1L, dto);

        assertThat(result.getAllocationMode()).isEqualTo(CustomerReceiptAllocationMode.MANUAL);
        assertThat(result.getTotalAllocatedAmount()).isEqualByComparingTo("60.00");
        assertThat(result.getUnappliedAmount()).isEqualByComparingTo("60.00");
        assertThat(invoice.getPaidAmount()).isEqualByComparingTo("0.00");
        assertThat(invoice.getDueAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void allocationCannotExceedInvoiceDue() {
        Customer customer = customer(1L, "CUS-0001", "Acme Trading");
        CustomerReceipt receipt = receipt(1L, customer, CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("120.00"));
        receipt.setAllocationMode(CustomerReceiptAllocationMode.MANUAL);

        CustomerReceiptAllocationDTO line = new CustomerReceiptAllocationDTO();
        line.setSalesInvoiceId(11L);
        line.setAllocatedAmount(new BigDecimal("120.00"));
        CustomerReceiptDTO dto = receiptDto(1L, LocalDate.of(2026, 6, 17), CustomerReceiptPaymentMethod.CASH, new BigDecimal("120.00"));
        dto.setAllocationMode(CustomerReceiptAllocationMode.MANUAL);
        dto.setAllocations(List.of(line));

        SalesInvoice invoice = invoice(11L, customer, "INV-001", new BigDecimal("100.00"), new BigDecimal("0.00"), new BigDecimal("100.00"));

        when(receiptRepository.findDetailedById(1L)).thenReturn(Optional.of(receipt));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(salesInvoiceRepository.findById(11L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Allocation cannot exceed invoice due amount.");
    }

    @Test
    void allocationCannotExceedReceiptAmount() {
        Customer customer = customer(1L, "CUS-0001", "Acme Trading");
        CustomerReceipt receipt = receipt(1L, customer, CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("100.00"));
        receipt.setAllocationMode(CustomerReceiptAllocationMode.MANUAL);

        CustomerReceiptAllocationDTO firstLine = new CustomerReceiptAllocationDTO();
        firstLine.setSalesInvoiceId(11L);
        firstLine.setAllocatedAmount(new BigDecimal("60.00"));
        CustomerReceiptAllocationDTO secondLine = new CustomerReceiptAllocationDTO();
        secondLine.setSalesInvoiceId(12L);
        secondLine.setAllocatedAmount(new BigDecimal("60.00"));

        CustomerReceiptDTO dto = receiptDto(1L, LocalDate.of(2026, 6, 17), CustomerReceiptPaymentMethod.CASH, new BigDecimal("100.00"));
        dto.setAllocationMode(CustomerReceiptAllocationMode.MANUAL);
        dto.setAllocations(List.of(firstLine, secondLine));

        SalesInvoice first = invoice(11L, customer, "INV-001", new BigDecimal("60.00"), new BigDecimal("0.00"), new BigDecimal("60.00"));
        SalesInvoice second = invoice(12L, customer, "INV-002", new BigDecimal("60.00"), new BigDecimal("0.00"), new BigDecimal("60.00"));

        when(receiptRepository.findDetailedById(1L)).thenReturn(Optional.of(receipt));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(salesInvoiceRepository.findById(11L)).thenReturn(Optional.of(first));
        when(salesInvoiceRepository.findById(12L)).thenReturn(Optional.of(second));

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Allocation cannot exceed receipt amount.");
    }

    @Test
    void postReceipt_blocksDuplicatePosting() {
        Customer customer = customer(1L, "CUS-0001", "Acme Trading");
        CustomerReceipt receipt = receipt(1L, customer, CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("200.00"));
        SalesInvoice invoice = invoice(11L, customer, "INV-001", new BigDecimal("100.00"), new BigDecimal("0.00"), new BigDecimal("100.00"));

        when(receiptRepository.findDetailedById(1L)).thenReturn(Optional.of(receipt));
        when(salesInvoiceRepository.findUnpaidByCustomerIdOrderBySaleDateAscIdAsc(1L)).thenReturn(List.of(invoice));
        when(salesInvoiceRepository.findById(11L)).thenReturn(Optional.of(invoice));
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("CUSTOMER_RECEIPT", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(0L);
        when(journalEntryRepository.existsByJournalNo("JRN-0001")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash")));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Receivable")).thenReturn(Optional.of(account("1020", "Accounts Receivable")));
        when(receiptRepository.save(any(CustomerReceipt.class))).thenAnswer(invocation -> {
            CustomerReceipt saved = invocation.getArgument(0);
            saved.setStatus(CustomerReceiptStatus.POSTED);
            return saved;
        });

        service.post(1L);

        assertThatThrownBy(() -> service.post(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Receipt is already posted.");
        verify(journalEntryRepository, times(1)).save(any(JournalEntry.class));
    }

    @Test
    void cancelDraftReceipt_marksCancelled() {
        CustomerReceipt receipt = receipt(1L, customer(1L, "CUS-0001", "Acme Trading"), CustomerReceiptStatus.DRAFT, CustomerReceiptPaymentMethod.CASH, new BigDecimal("100.00"));
        when(receiptRepository.findDetailedById(1L)).thenReturn(Optional.of(receipt));
        when(receiptRepository.save(any(CustomerReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerReceiptDTO result = service.cancel(1L);

        assertThat(result.getStatus()).isEqualTo(CustomerReceiptStatus.CANCELLED);
        assertThat(result.getCancelledAt()).isNotNull();
    }

    @Test
    void cancelPostedReceipt_isRejected() {
        CustomerReceipt receipt = receipt(1L, customer(1L, "CUS-0001", "Acme Trading"), CustomerReceiptStatus.POSTED, CustomerReceiptPaymentMethod.CASH, new BigDecimal("100.00"));
        when(receiptRepository.findDetailedById(1L)).thenReturn(Optional.of(receipt));

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

    private void stubReceiptPosting(CustomerReceipt receipt, List<SalesInvoice> invoices) {
        when(receiptRepository.findDetailedById(1L)).thenReturn(Optional.of(receipt));
        when(salesInvoiceRepository.findUnpaidByCustomerIdOrderBySaleDateAscIdAsc(1L)).thenReturn(invoices);
        for (SalesInvoice invoice : invoices) {
            when(salesInvoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        }
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("CUSTOMER_RECEIPT", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(0L);
        when(journalEntryRepository.existsByJournalNo("JRN-0001")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash")));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Receivable")).thenReturn(Optional.of(account("1020", "Accounts Receivable")));
        when(receiptRepository.save(any(CustomerReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private SalesInvoice invoice(Long id, Customer customer, String invoiceNo, BigDecimal netTotal,
                                 BigDecimal paidAmount, BigDecimal dueAmount) {
        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(id);
        invoice.setInvoiceNo(invoiceNo);
        invoice.setCustomer(customer);
        invoice.setSaleDate(LocalDateTime.of(2026, 6, 1, 0, 0));
        invoice.setNetTotal(netTotal);
        invoice.setPaidAmount(paidAmount);
        invoice.setDueAmount(dueAmount);
        invoice.setPaymentStatus(dueAmount.compareTo(BigDecimal.ZERO) <= 0 ? SalesPaymentStatus.PAID
                : paidAmount.compareTo(BigDecimal.ZERO) > 0 ? SalesPaymentStatus.PARTIAL : SalesPaymentStatus.DUE);
        invoice.setStatus(SalesInvoiceStatus.CONFIRMED);
        return invoice;
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
