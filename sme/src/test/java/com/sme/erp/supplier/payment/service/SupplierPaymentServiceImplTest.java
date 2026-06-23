package com.sme.erp.supplier.payment.service;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.audit.service.impl.CurrentAuditUser;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.payment.entity.SupplierPayment;
import com.sme.erp.supplier.payment.entity.SupplierPaymentAllocation;
import com.sme.erp.supplier.payment.enums.SupplierPaymentAllocationMode;
import com.sme.erp.supplier.payment.enums.SupplierPaymentMethod;
import com.sme.erp.supplier.payment.enums.SupplierPaymentStatus;
import com.sme.erp.supplier.payment.mapper.SupplierPaymentAllocationMapper;
import com.sme.erp.supplier.payment.mapper.SupplierPaymentMapper;
import com.sme.erp.supplier.payment.repository.SupplierPaymentRepository;
import com.sme.erp.supplier.payment.service.impl.SupplierPaymentServiceImpl;
import com.sme.erp.supplier.repository.SupplierRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierPaymentServiceImplTest {
    @Mock
    private SupplierPaymentRepository paymentRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private CurrentAuditUser currentAuditUser;

    private SupplierPaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SupplierPaymentServiceImpl(
                paymentRepository,
                supplierRepository,
                purchaseOrderRepository,
                new SupplierPaymentMapper(new SupplierPaymentAllocationMapper()),
                journalEntryRepository,
                accountRepository,
                activityLogService,
                auditLogService,
                currentAuditUser);
    }

    @Test
    void reversePostedPayment_restoresPurchaseDueAndPaidAmounts() {
        Supplier supplier = supplier();
        PurchaseOrder purchase = purchase(11L, supplier, new BigDecimal("100.00"), new BigDecimal("80.00"), new BigDecimal("20.00"));
        SupplierPayment payment = postedPayment(supplier, new BigDecimal("30.00"), new BigDecimal("30.00"), BigDecimal.ZERO);
        payment.getAllocations().add(allocation(payment, purchase, new BigDecimal("30.00")));

        when(paymentRepository.findDetailedById(1L)).thenReturn(Optional.of(payment));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("SUPPLIER_PAYMENT_REVERSAL", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(1L);
        when(journalEntryRepository.existsByJournalNo("JRN-0002")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash", AccountType.ASSET)));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(account("2000", "Accounts Payable", AccountType.LIABILITY)));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(SupplierPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.reverse(1L, "Duplicate bank payment");

        assertThat(purchase.getPaidAmount()).isEqualByComparingTo("50.00");
        assertThat(purchase.getDueAmount()).isEqualByComparingTo("50.00");
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.PARTIAL_PAID);
        assertThat(result.getStatus()).isEqualTo(SupplierPaymentStatus.REVERSED);
        assertThat(result.getReversalReason()).isEqualTo("Duplicate bank payment");
        verify(journalEntryRepository).save(any(JournalEntry.class));
    }

    @Test
    void duplicateReversal_isBlocked() {
        SupplierPayment payment = postedPayment(supplier(), new BigDecimal("30.00"), new BigDecimal("30.00"), BigDecimal.ZERO);
        when(paymentRepository.findDetailedById(1L)).thenReturn(Optional.of(payment));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("SUPPLIER_PAYMENT_REVERSAL", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.reverse(1L, "Again"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Supplier payment is already reversed.");
        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
    }

    @Test
    void reversalJournal_isCreatedOnce() {
        Supplier supplier = supplier();
        PurchaseOrder purchase = purchase(11L, supplier, new BigDecimal("100.00"), new BigDecimal("30.00"), new BigDecimal("70.00"));
        SupplierPayment payment = postedPayment(supplier, new BigDecimal("30.00"), new BigDecimal("30.00"), BigDecimal.ZERO);
        payment.getAllocations().add(allocation(payment, purchase, new BigDecimal("30.00")));

        when(paymentRepository.findDetailedById(1L)).thenReturn(Optional.of(payment));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("SUPPLIER_PAYMENT_REVERSAL", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(1L);
        when(journalEntryRepository.existsByJournalNo("JRN-0002")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash", AccountType.ASSET)));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(account("2000", "Accounts Payable", AccountType.LIABILITY)));
        when(paymentRepository.save(any(SupplierPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.reverse(1L, "First reversal");
        assertThatThrownBy(() -> service.reverse(1L, "Second reversal"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Supplier payment is already reversed.");
        verify(journalEntryRepository, times(1)).save(any(JournalEntry.class));
    }

    @Test
    void reversalCannotRunOnDraftCancelledOrReversedPayment() {
        assertReverseRejected(SupplierPaymentStatus.DRAFT, "Draft supplier payment cannot be reversed.");
        assertReverseRejected(SupplierPaymentStatus.CANCELLED, "Cancelled supplier payment cannot be reversed.");
        assertReverseRejected(SupplierPaymentStatus.REVERSED, "Supplier payment is already reversed.");
    }

    @Test
    void unappliedPayment_postsUnappliedAmountToSupplierAdvance() {
        Supplier supplier = supplier();
        PurchaseOrder purchase = purchase(11L, supplier, new BigDecimal("700.00"), BigDecimal.ZERO, new BigDecimal("700.00"));
        SupplierPayment payment = draftPayment(supplier, new BigDecimal("1000.00"));

        when(paymentRepository.findDetailedById(1L)).thenReturn(Optional.of(payment));
        when(purchaseOrderRepository.findUnpaidBySupplierIdOrderByPurchaseDateAscIdAsc(1L)).thenReturn(List.of(purchase));
        when(purchaseOrderRepository.findById(11L)).thenReturn(Optional.of(purchase));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("SUPPLIER_PAYMENT", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(1L);
        when(journalEntryRepository.existsByJournalNo("JRN-0002")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash", AccountType.ASSET)));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(account("2000", "Accounts Payable", AccountType.LIABILITY)));
        when(accountRepository.findByAccountNameIgnoreCase("Supplier Advance")).thenReturn(Optional.of(account("1030", "Supplier Advance", AccountType.ASSET)));
        when(paymentRepository.save(any(SupplierPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.post(1L);

        JournalEntry journal = capturedJournal();
        assertLine(journal, "Accounts Payable", "700.00", "0.00");
        assertLine(journal, "Supplier Advance", "300.00", "0.00");
        assertLine(journal, "Cash", "0.00", "1000.00");
    }

    @Test
    void fullyAllocatedPayment_postsOnlyToAccountsPayable() {
        Supplier supplier = supplier();
        PurchaseOrder purchase = purchase(11L, supplier, new BigDecimal("700.00"), BigDecimal.ZERO, new BigDecimal("700.00"));
        SupplierPayment payment = draftPayment(supplier, new BigDecimal("700.00"));

        when(paymentRepository.findDetailedById(1L)).thenReturn(Optional.of(payment));
        when(purchaseOrderRepository.findUnpaidBySupplierIdOrderByPurchaseDateAscIdAsc(1L)).thenReturn(List.of(purchase));
        when(purchaseOrderRepository.findById(11L)).thenReturn(Optional.of(purchase));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("SUPPLIER_PAYMENT", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(1L);
        when(journalEntryRepository.existsByJournalNo("JRN-0002")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash", AccountType.ASSET)));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(account("2000", "Accounts Payable", AccountType.LIABILITY)));
        when(paymentRepository.save(any(SupplierPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.post(1L);

        assertThat(purchase.getPaidAmount()).isEqualByComparingTo("700.00");
        assertThat(purchase.getDueAmount()).isEqualByComparingTo("0.00");
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.PAID);
        JournalEntry journal = capturedJournal();
        assertThat(journal.getLines()).hasSize(2);
        assertLine(journal, "Accounts Payable", "700.00", "0.00");
        assertLine(journal, "Cash", "0.00", "700.00");
    }

    @Test
    void partialPayment_updatesPurchaseDueAndStatus() {
        Supplier supplier = supplier();
        PurchaseOrder purchase = purchase(11L, supplier, new BigDecimal("700.00"), BigDecimal.ZERO, new BigDecimal("700.00"));
        SupplierPayment payment = draftPayment(supplier, new BigDecimal("300.00"));

        when(paymentRepository.findDetailedById(1L)).thenReturn(Optional.of(payment));
        when(purchaseOrderRepository.findUnpaidBySupplierIdOrderByPurchaseDateAscIdAsc(1L)).thenReturn(List.of(purchase));
        when(purchaseOrderRepository.findById(11L)).thenReturn(Optional.of(purchase));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("SUPPLIER_PAYMENT", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(1L);
        when(journalEntryRepository.existsByJournalNo("JRN-0002")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash", AccountType.ASSET)));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(account("2000", "Accounts Payable", AccountType.LIABILITY)));
        when(paymentRepository.save(any(SupplierPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.post(1L);

        assertThat(purchase.getPaidAmount()).isEqualByComparingTo("300.00");
        assertThat(purchase.getDueAmount()).isEqualByComparingTo("400.00");
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.PARTIAL_PAID);
    }

    @Test
    void paymentAfterPurchaseReturnPreservesReducedDueAmount() {
        Supplier supplier = supplier();
        PurchaseOrder purchase = purchase(11L, supplier, new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("700.00"));
        SupplierPayment payment = draftPayment(supplier, new BigDecimal("400.00"));

        when(paymentRepository.findDetailedById(1L)).thenReturn(Optional.of(payment));
        when(purchaseOrderRepository.findUnpaidBySupplierIdOrderByPurchaseDateAscIdAsc(1L)).thenReturn(List.of(purchase));
        when(purchaseOrderRepository.findById(11L)).thenReturn(Optional.of(purchase));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("SUPPLIER_PAYMENT", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(1L);
        when(journalEntryRepository.existsByJournalNo("JRN-0002")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash", AccountType.ASSET)));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(account("2000", "Accounts Payable", AccountType.LIABILITY)));
        when(paymentRepository.save(any(SupplierPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.post(1L);

        assertThat(purchase.getPaidAmount()).isEqualByComparingTo("400.00");
        assertThat(purchase.getDueAmount()).isEqualByComparingTo("300.00");
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.PARTIAL_PAID);
    }

    @Test
    void reversePaymentAfterPurchaseReturnRestoresOnlyReversedAllocation() {
        Supplier supplier = supplier();
        PurchaseOrder purchase = purchase(11L, supplier, new BigDecimal("1000.00"), new BigDecimal("500.00"), new BigDecimal("200.00"));
        SupplierPayment payment = postedPayment(supplier, new BigDecimal("200.00"), new BigDecimal("200.00"), BigDecimal.ZERO);
        payment.getAllocations().add(allocation(payment, purchase, new BigDecimal("200.00")));

        when(paymentRepository.findDetailedById(1L)).thenReturn(Optional.of(payment));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("SUPPLIER_PAYMENT_REVERSAL", 1L)).thenReturn(false);
        when(journalEntryRepository.findMaxId()).thenReturn(1L);
        when(journalEntryRepository.existsByJournalNo("JRN-0002")).thenReturn(false);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(account("1000", "Cash", AccountType.ASSET)));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(account("2000", "Accounts Payable", AccountType.LIABILITY)));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(SupplierPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.reverse(1L, "Wrong allocation");

        assertThat(purchase.getPaidAmount()).isEqualByComparingTo("300.00");
        assertThat(purchase.getDueAmount()).isEqualByComparingTo("400.00");
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.PARTIAL_PAID);
    }

    private void assertReverseRejected(SupplierPaymentStatus status, String message) {
        SupplierPayment payment = postedPayment(supplier(), new BigDecimal("10.00"), BigDecimal.ZERO, new BigDecimal("10.00"));
        payment.setStatus(status);
        when(paymentRepository.findDetailedById(1L)).thenReturn(Optional.of(payment));
        assertThatThrownBy(() -> service.reverse(1L, "No"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(message);
    }

    private JournalEntry capturedJournal() {
        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        return captor.getValue();
    }

    private void assertLine(JournalEntry journal, String accountName, String debit, String credit) {
        assertThat(journal.getLines())
                .anySatisfy(line -> {
                    assertThat(line.getAccount().getAccountName()).isEqualTo(accountName);
                    assertThat(line.getDebit()).isEqualByComparingTo(debit);
                    assertThat(line.getCredit()).isEqualByComparingTo(credit);
                });
    }

    private Supplier supplier() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierCode("SUP-0001");
        supplier.setName("Global Supplies");
        return supplier;
    }

    private SupplierPayment draftPayment(Supplier supplier, BigDecimal amount) {
        SupplierPayment payment = payment(supplier, amount, SupplierPaymentStatus.DRAFT);
        payment.setAllocationMode(SupplierPaymentAllocationMode.AUTO);
        return payment;
    }

    private SupplierPayment postedPayment(Supplier supplier, BigDecimal amount, BigDecimal allocated, BigDecimal unapplied) {
        SupplierPayment payment = payment(supplier, amount, SupplierPaymentStatus.POSTED);
        payment.setPostedAt(LocalDateTime.of(2026, 6, 18, 10, 0));
        payment.setTotalAllocatedAmount(allocated);
        payment.setUnappliedAmount(unapplied);
        return payment;
    }

    private SupplierPayment payment(Supplier supplier, BigDecimal amount, SupplierPaymentStatus status) {
        SupplierPayment payment = new SupplierPayment();
        payment.setId(1L);
        payment.setPaymentNo("SP-000001");
        payment.setSupplier(supplier);
        payment.setPaymentDate(LocalDate.of(2026, 6, 18));
        payment.setPaymentMethod(SupplierPaymentMethod.CASH);
        payment.setAmount(amount);
        payment.setStatus(status);
        return payment;
    }

    private SupplierPaymentAllocation allocation(SupplierPayment payment, PurchaseOrder purchase, BigDecimal amount) {
        SupplierPaymentAllocation allocation = new SupplierPaymentAllocation();
        allocation.setId(100L);
        allocation.setSupplierPayment(payment);
        allocation.setPurchaseOrder(purchase);
        allocation.setAllocatedAmount(amount);
        return allocation;
    }

    private PurchaseOrder purchase(Long id, Supplier supplier, BigDecimal netTotal, BigDecimal paid, BigDecimal due) {
        PurchaseOrder purchase = new PurchaseOrder();
        purchase.setId(id);
        purchase.setPurchaseCode("PO-0001");
        purchase.setSupplier(supplier);
        purchase.setPurchaseDate(LocalDateTime.of(2026, 6, 1, 0, 0));
        purchase.setNetTotal(netTotal);
        purchase.setPaidAmount(paid);
        purchase.setDueAmount(due);
        purchase.setStatus(due.compareTo(BigDecimal.ZERO) <= 0 ? PurchaseStatus.PAID
                : paid.compareTo(BigDecimal.ZERO) > 0 ? PurchaseStatus.PARTIAL_PAID : PurchaseStatus.RECEIVED);
        return purchase;
    }

    private Account account(String code, String name, AccountType type) {
        Account account = new Account();
        account.setAccountCode(code);
        account.setAccountName(name);
        account.setAccountType(type);
        return account;
    }
}
