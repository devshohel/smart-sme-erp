package com.sme.erp.supplier.service;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.enums.JournalStatus;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryLineRepository;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.enums.Status;
import com.sme.erp.notification.service.NotificationService;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.entity.PurchaseReturn;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.repository.PurchaseReturnRepository;
import com.sme.erp.supplier.dto.ApReconciliationDTO;
import com.sme.erp.supplier.dto.SupplierDTO;
import com.sme.erp.supplier.dto.SupplierStatementDTO;
import com.sme.erp.supplier.entity.Supplier;
import com.sme.erp.supplier.mapper.SupplierMapper;
import com.sme.erp.supplier.payment.entity.SupplierPayment;
import com.sme.erp.supplier.payment.entity.SupplierPaymentAllocation;
import com.sme.erp.supplier.payment.enums.SupplierPaymentAllocationMode;
import com.sme.erp.supplier.payment.enums.SupplierPaymentMethod;
import com.sme.erp.supplier.payment.enums.SupplierPaymentStatus;
import com.sme.erp.supplier.payment.mapper.SupplierPaymentAllocationMapper;
import com.sme.erp.supplier.payment.mapper.SupplierPaymentMapper;
import com.sme.erp.supplier.payment.repository.SupplierPaymentRepository;
import com.sme.erp.supplier.repository.SupplierRepository;
import com.sme.erp.supplier.service.impl.SupplierServiceImpl;
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
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private PurchaseReturnRepository purchaseReturnRepository;
    @Mock
    private SupplierPaymentRepository supplierPaymentRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private NotificationService notificationService;

    private SupplierServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SupplierServiceImpl(supplierRepository, new SupplierMapper(), purchaseOrderRepository, purchaseReturnRepository,
                supplierPaymentRepository, new SupplierPaymentMapper(new SupplierPaymentAllocationMapper()),
                accountRepository, journalEntryLineRepository, activityLogService, auditLogService, notificationService);
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
    void update_preservesExistingCurrentBalanceEvenIfPayloadProvidesValue() {
        Supplier existing = new Supplier();
        existing.setId(1L);
        existing.setSupplierCode("SUP-0001");
        existing.setName("Old Name");
        existing.setOpeningBalance(new BigDecimal("75.00"));
        existing.setCurrentBalance(new BigDecimal("125.00"));
        existing.setStatus(Status.ACTIVE);

        SupplierDTO dto = new SupplierDTO();
        dto.setName("New Name");
        dto.setCurrentBalance(new BigDecimal("999.00"));

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierDTO result = service.update(1L, dto);

        assertThat(result.getCurrentBalance()).isEqualByComparingTo("125.00");
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

    @Test
    void apReconciliation_fullyAllocatedPaymentMatchesPurchaseDueAndGlAp() {
        Supplier supplier = supplier(1L);
        PurchaseOrder purchase = purchase(10L, supplier);
        SupplierPayment payment = payment(20L, supplier, SupplierPaymentStatus.POSTED);
        Account ap = account();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(purchaseOrderRepository.findApReconciliationPurchaseRows(1L, null, null)).thenReturn(List.of());
        when(supplierPaymentRepository.findApReconciliationAdvanceRows(1L, null, null)).thenReturn(List.of());
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(ap));
        when(journalEntryLineRepository.findPostedLedgerLines(10L, null, null)).thenReturn(List.of(
                apLine(ap, "PURCHASE", 10L, BigDecimal.ZERO, new BigDecimal("1000.00")),
                apLine(ap, "SUPPLIER_PAYMENT", 20L, new BigDecimal("1000.00"), BigDecimal.ZERO)
        ));
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(purchase));
        when(supplierPaymentRepository.findById(20L)).thenReturn(Optional.of(payment));

        ApReconciliationDTO result = service.getApReconciliation(1L, null, null);

        assertThat(result.getRows()).hasSize(1);
        assertThat(result.getRows().get(0).getStatus()).isEqualTo("MATCHED");
        assertThat(result.getSummary().getTotalPurchaseDue()).isEqualByComparingTo("0.00");
        assertThat(result.getSummary().getTotalGlAccountsPayable()).isEqualByComparingTo("0.00");
        assertThat(result.getBreakdown().getAllocatedPayments()).isEqualByComparingTo("1000.00");
    }

    @Test
    void apReconciliation_unappliedPaymentShowsSupplierAdvanceSeparateFromAp() {
        Supplier supplier = supplier(1L);
        PurchaseOrder purchase = purchase(10L, supplier);
        SupplierPayment payment = payment(20L, supplier, SupplierPaymentStatus.POSTED);
        Account ap = account();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(purchaseOrderRepository.findApReconciliationPurchaseRows(1L, null, null))
                .thenReturn(List.<Object[]>of(new Object[] { 1L, new BigDecimal("300.00"), new BigDecimal("1000.00") }));
        when(supplierPaymentRepository.findApReconciliationAdvanceRows(1L, null, null))
                .thenReturn(List.<Object[]>of(new Object[] { 1L, new BigDecimal("300.00") }));
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(ap));
        when(journalEntryLineRepository.findPostedLedgerLines(10L, null, null)).thenReturn(List.of(
                apLine(ap, "PURCHASE", 10L, BigDecimal.ZERO, new BigDecimal("1000.00")),
                apLine(ap, "SUPPLIER_PAYMENT", 20L, new BigDecimal("700.00"), BigDecimal.ZERO)
        ));
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(purchase));
        when(supplierPaymentRepository.findById(20L)).thenReturn(Optional.of(payment));

        ApReconciliationDTO result = service.getApReconciliation(1L, null, null);

        assertThat(result.getRows().get(0).getPurchaseDue()).isEqualByComparingTo("300.00");
        assertThat(result.getRows().get(0).getSupplierAdvance()).isEqualByComparingTo("300.00");
        assertThat(result.getRows().get(0).getGlAccountsPayable()).isEqualByComparingTo("300.00");
        assertThat(result.getRows().get(0).getNetExposure()).isEqualByComparingTo("0.00");
        assertThat(result.getRows().get(0).getStatus()).isEqualTo("MATCHED");
    }

    @Test
    void apReconciliation_reversedPaymentRestoresGlApAndPurchaseDue() {
        Supplier supplier = supplier(1L);
        PurchaseOrder purchase = purchase(10L, supplier);
        SupplierPayment payment = payment(20L, supplier, SupplierPaymentStatus.REVERSED);
        Account ap = account();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(purchaseOrderRepository.findApReconciliationPurchaseRows(1L, null, null))
                .thenReturn(List.<Object[]>of(new Object[] { 1L, new BigDecimal("1000.00"), new BigDecimal("1000.00") }));
        when(supplierPaymentRepository.findApReconciliationAdvanceRows(1L, null, null)).thenReturn(List.of());
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(ap));
        when(journalEntryLineRepository.findPostedLedgerLines(10L, null, null)).thenReturn(List.of(
                apLine(ap, "PURCHASE", 10L, BigDecimal.ZERO, new BigDecimal("1000.00")),
                apLine(ap, "SUPPLIER_PAYMENT", 20L, new BigDecimal("700.00"), BigDecimal.ZERO),
                apLine(ap, "SUPPLIER_PAYMENT_REVERSAL", 20L, BigDecimal.ZERO, new BigDecimal("700.00"))
        ));
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(purchase));
        when(supplierPaymentRepository.findById(20L)).thenReturn(Optional.of(payment));

        ApReconciliationDTO result = service.getApReconciliation(1L, null, null);

        assertThat(result.getRows().get(0).getGlAccountsPayable()).isEqualByComparingTo("1000.00");
        assertThat(result.getRows().get(0).getVariance()).isEqualByComparingTo("0.00");
        assertThat(result.getBreakdown().getPaymentReversals()).isEqualByComparingTo("700.00");
    }

    @Test
    void apReconciliation_purchaseReturnEffectAppearsAsVarianceAgainstPurchaseDue() {
        Supplier supplier = supplier(1L);
        PurchaseOrder purchase = purchase(10L, supplier);
        PurchaseReturn purchaseReturn = purchaseReturn(11L, supplier);
        Account ap = account();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(purchaseOrderRepository.findApReconciliationPurchaseRows(1L, null, null))
                .thenReturn(List.<Object[]>of(new Object[] { 1L, new BigDecimal("1000.00"), new BigDecimal("1000.00") }));
        when(supplierPaymentRepository.findApReconciliationAdvanceRows(1L, null, null)).thenReturn(List.of());
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(ap));
        when(journalEntryLineRepository.findPostedLedgerLines(10L, null, null)).thenReturn(List.of(
                apLine(ap, "PURCHASE", 10L, BigDecimal.ZERO, new BigDecimal("1000.00")),
                apLine(ap, "PURCHASE_RETURN", 11L, new BigDecimal("200.00"), BigDecimal.ZERO)
        ));
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(purchase));
        when(purchaseReturnRepository.findById(11L)).thenReturn(Optional.of(purchaseReturn));

        ApReconciliationDTO result = service.getApReconciliation(1L, null, null);

        assertThat(result.getRows().get(0).getGlAccountsPayable()).isEqualByComparingTo("800.00");
        assertThat(result.getRows().get(0).getVariance()).isEqualByComparingTo("-200.00");
        assertThat(result.getRows().get(0).getStatus()).isEqualTo("VARIANCE");
        assertThat(result.getBreakdown().getPurchaseReturns()).isEqualByComparingTo("200.00");
    }

    @Test
    void apReconciliation_manualApAdjustmentCreatesReviewNeededRowAndSummaryTotals() {
        Account ap = account();
        when(supplierRepository.search(null, null)).thenReturn(List.of());
        when(purchaseOrderRepository.findApReconciliationPurchaseRows(null, null, null)).thenReturn(List.of());
        when(supplierPaymentRepository.findApReconciliationAdvanceRows(null, null, null)).thenReturn(List.of());
        when(accountRepository.findByAccountNameIgnoreCase("Accounts Payable")).thenReturn(Optional.of(ap));
        when(journalEntryLineRepository.findPostedLedgerLines(10L, null, null)).thenReturn(List.of(
                apLine(ap, null, null, BigDecimal.ZERO, new BigDecimal("50.00"))
        ));

        ApReconciliationDTO result = service.getApReconciliation(null, null, null);

        assertThat(result.getRows()).hasSize(1);
        assertThat(result.getRows().get(0).getSupplierCode()).isEqualTo("MANUAL");
        assertThat(result.getRows().get(0).getStatus()).isEqualTo("REVIEW_NEEDED");
        assertThat(result.getSummary().getTotalGlAccountsPayable()).isEqualByComparingTo("50.00");
        assertThat(result.getSummary().getTotalVariance()).isEqualByComparingTo("50.00");
        assertThat(result.getBreakdown().getManualApAdjustments()).isEqualByComparingTo("50.00");
    }

    @Test
    void statement_includesPurchases() {
        Supplier supplier = supplier(1L);
        PurchaseOrder purchase = purchase(10L, supplier);
        supplier.setOpeningBalance(new BigDecimal("50.00"));
        stubStatementSources(supplier, List.of(purchase), List.of(), List.of());

        SupplierStatementDTO result = service.getStatement(1L, null, null);

        assertThat(result.getRows()).extracting("referenceType").contains("PURCHASE");
        assertThat(result.getClosingPayableBalance()).isEqualByComparingTo("1050.00");
        assertThat(result.getNetSupplierPosition()).isEqualByComparingTo("1050.00");
    }

    @Test
    void statement_includesPurchaseReturns() {
        Supplier supplier = supplier(1L);
        PurchaseOrder purchase = purchase(10L, supplier);
        PurchaseReturn purchaseReturn = purchaseReturn(11L, supplier);
        stubStatementSources(supplier, List.of(purchase), List.of(purchaseReturn), List.of());

        SupplierStatementDTO result = service.getStatement(1L, null, null);

        assertThat(result.getRows()).extracting("referenceType").contains("PURCHASE_RETURN");
        assertThat(result.getClosingPayableBalance()).isEqualByComparingTo("800.00");
    }

    @Test
    void statement_includesAllocatedPaymentsWithoutReducingAdvance() {
        Supplier supplier = supplier(1L);
        PurchaseOrder purchase = purchase(10L, supplier);
        SupplierPayment payment = payment(20L, supplier, SupplierPaymentStatus.POSTED);
        payment.setTotalAllocatedAmount(new BigDecimal("700.00"));
        payment.setUnappliedAmount(BigDecimal.ZERO);
        payment.setAllocations(List.of(allocation(payment, purchase, new BigDecimal("700.00"))));
        stubStatementSources(supplier, List.of(purchase), List.of(), List.of(payment));

        SupplierStatementDTO result = service.getStatement(1L, null, null);

        assertThat(result.getRows()).extracting("referenceType").contains("SUPPLIER_PAYMENT");
        assertThat(result.getClosingPayableBalance()).isEqualByComparingTo("300.00");
        assertThat(result.getSupplierAdvanceBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void statement_includesUnappliedAdvancesSeparatelyFromPayable() {
        Supplier supplier = supplier(1L);
        PurchaseOrder purchase = purchase(10L, supplier);
        SupplierPayment payment = payment(20L, supplier, SupplierPaymentStatus.POSTED);
        payment.setAmount(new BigDecimal("900.00"));
        payment.setTotalAllocatedAmount(new BigDecimal("700.00"));
        payment.setUnappliedAmount(new BigDecimal("200.00"));
        payment.setAllocations(List.of(allocation(payment, purchase, new BigDecimal("700.00"))));
        stubStatementSources(supplier, List.of(purchase), List.of(), List.of(payment));

        SupplierStatementDTO result = service.getStatement(1L, null, null);

        assertThat(result.getClosingPayableBalance()).isEqualByComparingTo("300.00");
        assertThat(result.getSupplierAdvanceBalance()).isEqualByComparingTo("200.00");
        assertThat(result.getNetSupplierPosition()).isEqualByComparingTo("100.00");
        assertThat(result.getRows()).anySatisfy(row -> {
            assertThat(row.getReferenceType()).isEqualTo("SUPPLIER_PAYMENT");
            assertThat(row.getAdvanceAmount()).isEqualByComparingTo("200.00");
        });
    }

    @Test
    void statement_includesReversalsWithOriginalPaymentAndReversalRows() {
        Supplier supplier = supplier(1L);
        PurchaseOrder purchase = purchase(10L, supplier);
        SupplierPayment payment = payment(20L, supplier, SupplierPaymentStatus.REVERSED);
        payment.setAmount(new BigDecimal("900.00"));
        payment.setTotalAllocatedAmount(new BigDecimal("700.00"));
        payment.setUnappliedAmount(new BigDecimal("200.00"));
        payment.setReversedAt(LocalDateTime.of(2026, 6, 5, 10, 0));
        payment.setAllocations(List.of(allocation(payment, purchase, new BigDecimal("700.00"))));
        stubStatementSources(supplier, List.of(purchase), List.of(), List.of(payment));

        SupplierStatementDTO result = service.getStatement(1L, null, null);

        assertThat(result.getRows()).extracting("referenceType")
                .contains("SUPPLIER_PAYMENT", "SUPPLIER_PAYMENT_REVERSAL");
        assertThat(result.getClosingPayableBalance()).isEqualByComparingTo("1000.00");
        assertThat(result.getSupplierAdvanceBalance()).isEqualByComparingTo("0.00");
        assertThat(result.getNetSupplierPosition()).isEqualByComparingTo("1000.00");
    }

    @Test
    void statement_openingBalanceWithFromDateAppliesEarlierEntries() {
        Supplier supplier = supplier(1L);
        supplier.setOpeningBalance(new BigDecimal("50.00"));
        PurchaseOrder earlierPurchase = purchase(10L, supplier);
        earlierPurchase.setPurchaseDate(LocalDateTime.of(2026, 5, 25, 0, 0));
        PurchaseOrder periodPurchase = purchase(11L, supplier);
        periodPurchase.setPurchaseDate(LocalDateTime.of(2026, 6, 2, 0, 0));
        periodPurchase.setNetTotal(new BigDecimal("300.00"));
        periodPurchase.setDueAmount(new BigDecimal("300.00"));
        stubStatementSources(supplier, List.of(earlierPurchase, periodPurchase), List.of(), List.of());

        SupplierStatementDTO result = service.getStatement(1L, LocalDate.of(2026, 6, 1), null);

        assertThat(result.getOpeningBalance()).isEqualByComparingTo("1050.00");
        assertThat(result.getClosingPayableBalance()).isEqualByComparingTo("1350.00");
        assertThat(result.getRows()).noneSatisfy(row ->
                assertThat(row.getReferenceNo()).isEqualTo("PO-10"));
    }

    @Test
    void statement_closingAndNetBalanceAreCalculatedFromPayableMinusAdvance() {
        Supplier supplier = supplier(1L);
        supplier.setOpeningBalance(new BigDecimal("100.00"));
        PurchaseOrder purchase = purchase(10L, supplier);
        PurchaseReturn purchaseReturn = purchaseReturn(11L, supplier);
        SupplierPayment payment = payment(20L, supplier, SupplierPaymentStatus.POSTED);
        payment.setAmount(new BigDecimal("400.00"));
        payment.setTotalAllocatedAmount(new BigDecimal("250.00"));
        payment.setUnappliedAmount(new BigDecimal("150.00"));
        payment.setAllocations(List.of(allocation(payment, purchase, new BigDecimal("250.00"))));
        stubStatementSources(supplier, List.of(purchase), List.of(purchaseReturn), List.of(payment));

        SupplierStatementDTO result = service.getStatement(1L, null, null);

        assertThat(result.getOpeningBalance()).isEqualByComparingTo("100.00");
        assertThat(result.getClosingPayableBalance()).isEqualByComparingTo("650.00");
        assertThat(result.getSupplierAdvanceBalance()).isEqualByComparingTo("150.00");
        assertThat(result.getNetSupplierPosition()).isEqualByComparingTo("500.00");
    }

    private Supplier supplier(Long id) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setSupplierCode("SUP-" + id);
        supplier.setName("Supplier " + id);
        return supplier;
    }

    private PurchaseOrder purchase(Long id, Supplier supplier) {
        PurchaseOrder purchase = new PurchaseOrder();
        purchase.setId(id);
        purchase.setSupplier(supplier);
        purchase.setPurchaseCode("PO-" + id);
        purchase.setPurchaseDate(LocalDateTime.of(2026, 6, 1, 0, 0));
        purchase.setNetTotal(new BigDecimal("1000.00"));
        purchase.setPaidAmount(BigDecimal.ZERO);
        purchase.setDueAmount(new BigDecimal("1000.00"));
        purchase.setStatus(PurchaseStatus.RECEIVED);
        return purchase;
    }

    private PurchaseReturn purchaseReturn(Long id, Supplier supplier) {
        PurchaseReturn purchaseReturn = new PurchaseReturn();
        purchaseReturn.setId(id);
        purchaseReturn.setSupplier(supplier);
        purchaseReturn.setReturnCode("PR-" + id);
        purchaseReturn.setReturnDate(LocalDateTime.of(2026, 6, 2, 0, 0));
        purchaseReturn.setTotalAmount(new BigDecimal("200.00"));
        return purchaseReturn;
    }

    private SupplierPayment payment(Long id, Supplier supplier, SupplierPaymentStatus status) {
        SupplierPayment payment = new SupplierPayment();
        payment.setId(id);
        payment.setSupplier(supplier);
        payment.setPaymentNo("SP-" + id);
        payment.setPaymentDate(LocalDate.of(2026, 6, 3));
        payment.setAmount(new BigDecimal("1000.00"));
        payment.setPaymentMethod(SupplierPaymentMethod.CASH);
        payment.setAllocationMode(SupplierPaymentAllocationMode.AUTO);
        payment.setStatus(status);
        return payment;
    }

    private SupplierPaymentAllocation allocation(SupplierPayment payment, PurchaseOrder purchase, BigDecimal amount) {
        SupplierPaymentAllocation allocation = new SupplierPaymentAllocation();
        allocation.setSupplierPayment(payment);
        allocation.setPurchaseOrder(purchase);
        allocation.setAllocatedAmount(amount);
        return allocation;
    }

    private void stubStatementSources(Supplier supplier, List<PurchaseOrder> purchases,
                                      List<PurchaseReturn> purchaseReturns, List<SupplierPayment> payments) {
        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(purchaseOrderRepository.findPostedBySupplierForLedger(supplier.getId(), null, null)).thenReturn(purchases);
        when(purchaseReturnRepository.findBySupplierForLedger(supplier.getId(), null, null)).thenReturn(purchaseReturns);
        when(supplierPaymentRepository.findStatementPaymentsBySupplier(supplier.getId())).thenReturn(payments);
    }

    private Account account() {
        Account account = new Account();
        account.setId(10L);
        account.setAccountCode("2000");
        account.setAccountName("Accounts Payable");
        account.setAccountType(AccountType.LIABILITY);
        return account;
    }

    private JournalEntryLine apLine(Account account, String sourceType, Long sourceId, BigDecimal debit, BigDecimal credit) {
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setId(sourceId == null ? 999L : sourceId);
        journalEntry.setJournalNo("JRN-" + (sourceId == null ? 999 : sourceId));
        journalEntry.setJournalDate(LocalDate.of(2026, 6, 3));
        journalEntry.setStatus(JournalStatus.POSTED);
        journalEntry.setSourceType(sourceType);
        journalEntry.setSourceId(sourceId);

        JournalEntryLine line = new JournalEntryLine();
        line.setJournalEntry(journalEntry);
        line.setAccount(account);
        line.setDebit(debit);
        line.setCredit(credit);
        return line;
    }
}
