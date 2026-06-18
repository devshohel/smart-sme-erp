package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.ExpenseDTO;
import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.accounting.entity.ExpenseCategory;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;
import com.sme.erp.accounting.mapper.AccountingMapper;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.ExpenseCategoryRepository;
import com.sme.erp.accounting.repository.ExpenseRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
import com.sme.erp.accounting.service.impl.AccountingPostingServiceImpl;
import com.sme.erp.accounting.service.impl.ExpenseServiceImpl;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.enums.Status;
import com.sme.erp.file.dto.StoredFileDTO;
import com.sme.erp.file.service.FileStorageService;
import com.sme.erp.settings.entity.TaxSettings;
import com.sme.erp.settings.repository.TaxSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {
    @Mock private ExpenseRepository expenseRepository;
    @Mock private ExpenseCategoryRepository categoryRepository;
    @Mock private AccountingPostingService accountingPostingService;
    @Mock private JournalEntryRepository journalEntryRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private ActivityLogService activityLogService;
    @Mock private AuditLogService auditLogService;
    @Mock private FileStorageService fileStorageService;
    @Mock private TaxSettingsRepository taxSettingsRepository;

    private ExpenseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ExpenseServiceImpl(expenseRepository, categoryRepository, new AccountingMapper(),
                activityLogService, auditLogService, accountingPostingService, journalEntryRepository, fileStorageService);
    }

    @Test
    void create_savesDraftExpenseWithoutPosting() {
        ExpenseCategory category = category(1L, account(10L, "5100", "Fuel Expense", AccountType.EXPENSE));
        when(expenseRepository.findMaxId()).thenReturn(0L);
        when(expenseRepository.existsByExpenseNo("EXP-0001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            expense.setId(100L);
            return expense;
        });

        ExpenseDTO result = service.create(dto());

        assertThat(result.getStatus()).isEqualTo(ExpenseStatus.DRAFT);
        verify(accountingPostingService, never()).postExpense(any());
    }

    @Test
    void update_allowsDraftExpense() {
        Expense expense = expense(100L, ExpenseStatus.DRAFT, AccountingPaymentMethod.CASH);
        ExpenseCategory category = category(2L, account(11L, "5200", "Office Expense", AccountType.EXPENSE));
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDTO result = service.update(100L, dto());

        assertThat(result.getStatus()).isEqualTo(ExpenseStatus.DRAFT);
        assertThat(result.getAmount()).isEqualByComparingTo("125.00");
    }

    @Test
    void cancel_allowsDraftExpense() {
        Expense expense = expense(100L, ExpenseStatus.DRAFT, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDTO result = service.cancel(100L);

        assertThat(result.getStatus()).isEqualTo(ExpenseStatus.CANCELLED);
    }

    @Test
    void submit_submitsDraftExpense() {
        Expense expense = expense(100L, ExpenseStatus.DRAFT, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDTO result = service.submit(100L);

        assertThat(result.getStatus()).isEqualTo(ExpenseStatus.SUBMITTED);
        assertThat(result.getSubmittedAt()).isNotNull();
    }

    @Test
    void update_blocksSubmittedExpense() {
        Expense expense = expense(100L, ExpenseStatus.SUBMITTED, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> service.update(100L, dto()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only draft expenses can be edited");
    }

    @Test
    void approve_approvesSubmittedExpense() {
        Expense expense = expense(100L, ExpenseStatus.SUBMITTED, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDTO result = service.approve(100L);

        assertThat(result.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
        assertThat(result.getApprovedAt()).isNotNull();
    }

    @Test
    void reject_rejectsSubmittedExpenseWithReason() {
        Expense expense = expense(100L, ExpenseStatus.SUBMITTED, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDTO result = service.reject(100L, "Missing receipt");

        assertThat(result.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
        assertThat(result.getRejectionReason()).isEqualTo("Missing receipt");
        assertThat(result.getRejectedAt()).isNotNull();
    }

    @Test
    void post_postsApprovedExpenseAndMarksPosted() {
        Expense expense = expense(100L, ExpenseStatus.APPROVED, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("EXPENSE", 100L)).thenReturn(false);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDTO result = service.post(100L);

        verify(accountingPostingService).postExpense(expense);
        assertThat(result.getStatus()).isEqualTo(ExpenseStatus.POSTED);
    }

    @Test
    void post_blocksDraftSubmittedAndRejectedExpense() {
        assertPostBlocked(ExpenseStatus.DRAFT);
        assertPostBlocked(ExpenseStatus.SUBMITTED);
        assertPostBlocked(ExpenseStatus.REJECTED);
    }

    @Test
    void post_blocksDuplicatePost() {
        Expense expense = expense(100L, ExpenseStatus.POSTED, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> service.post(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Expense is already posted");
        verify(accountingPostingService, never()).postExpense(any());
    }

    @Test
    void approve_blocksDuplicateApproval() {
        Expense expense = expense(100L, ExpenseStatus.APPROVED, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> service.approve(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Expense is already approved");
    }

    @Test
    void update_blocksPostedExpense() {
        Expense expense = expense(100L, ExpenseStatus.POSTED, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> service.update(100L, dto()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only draft expenses can be edited");
    }

    @Test
    void cancel_blocksPostedExpense() {
        Expense expense = expense(100L, ExpenseStatus.POSTED, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> service.cancel(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only draft expenses can be cancelled");
    }

    @Test
    void post_blocksCategoryWithoutGlAccount() {
        Expense expense = expense(100L, ExpenseStatus.APPROVED, AccountingPaymentMethod.CASH);
        expense.setCategory(category(1L, null));
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("EXPENSE", 100L)).thenReturn(false);

        assertThatThrownBy(() -> service.post(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Expense category must have a mapped GL account before posting");
        verify(accountingPostingService, never()).postExpense(any());
    }

    @Test
    void create_withReceiptSavesAttachmentMetadata() {
        ExpenseCategory category = category(1L, account(10L, "5100", "Fuel Expense", AccountType.EXPENSE));
        MockMultipartFile receipt = new MockMultipartFile("receipt", "receipt.pdf", "application/pdf", "pdf".getBytes());
        when(expenseRepository.findMaxId()).thenReturn(0L);
        when(expenseRepository.existsByExpenseNo("EXP-0001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(fileStorageService.storeExpenseReceipt(receipt)).thenReturn(new StoredFileDTO(
                "receipt.pdf", "stored.pdf", "application/pdf", 3L, "uploads/expense-receipts/stored.pdf", "/api/v1/files/expenses/stored.pdf"));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            expense.setId(100L);
            return expense;
        });

        ExpenseDTO result = service.create(dto(), receipt);

        assertThat(result.getReceiptOriginalFilename()).isEqualTo("receipt.pdf");
        assertThat(result.getReceiptUrl()).isEqualTo("/api/v1/files/expenses/stored.pdf");
    }

    @Test
    void approvalQueue_returnsSubmittedExpensesOnly() {
        Expense submitted = expense(100L, ExpenseStatus.SUBMITTED, AccountingPaymentMethod.CASH);
        when(expenseRepository.approvalQueue(null, null, null, null, null, null)).thenReturn(java.util.List.of(submitted));

        var result = service.approvalQueue(null, null, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ExpenseStatus.SUBMITTED);
    }

    @Test
    void reverse_reversesPostedExpense() {
        Expense expense = expense(100L, ExpenseStatus.POSTED, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("EXPENSE_REVERSAL", 100L)).thenReturn(false);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDTO result = service.reverse(100L, "Wrong payment method");

        verify(accountingPostingService).reverseExpense(expense, "Wrong payment method");
        assertThat(result.getStatus()).isEqualTo(ExpenseStatus.REVERSED);
        assertThat(result.getReversalReason()).isEqualTo("Wrong payment method");
        assertThat(result.getReversedAt()).isNotNull();
    }

    @Test
    void reverse_blocksDuplicateReversal() {
        Expense expense = expense(100L, ExpenseStatus.REVERSED, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> service.reverse(100L, "Again"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Expense is already reversed");
    }

    @Test
    void create_calculatesTaxAmounts() {
        ExpenseCategory category = category(1L, account(10L, "5100", "Fuel Expense", AccountType.EXPENSE));
        ExpenseDTO dto = dto();
        dto.setNetAmount(new BigDecimal("100.00"));
        dto.setTaxApplicable(true);
        dto.setTaxRate(new BigDecimal("15.00"));
        when(expenseRepository.findMaxId()).thenReturn(0L);
        when(expenseRepository.existsByExpenseNo("EXP-0001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            expense.setId(100L);
            return expense;
        });

        ExpenseDTO result = service.create(dto);

        assertThat(result.getNetAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getTaxAmount()).isEqualByComparingTo("15.00");
        assertThat(result.getGrossAmount()).isEqualByComparingTo("115.00");
        assertThat(result.getAmount()).isEqualByComparingTo("115.00");
    }

    @Test
    void reportsTotalsAndFiltersWork() {
        Expense posted = expense(100L, ExpenseStatus.POSTED, AccountingPaymentMethod.CASH);
        posted.setNetAmount(new BigDecimal("100.00"));
        posted.setTaxAmount(new BigDecimal("15.00"));
        posted.setGrossAmount(new BigDecimal("115.00"));
        Expense draft = expense(101L, ExpenseStatus.DRAFT, AccountingPaymentMethod.BANK);
        when(expenseRepository.search(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1L, null))
                .thenReturn(java.util.List.of(posted, draft));

        var result = service.reportSummary(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1L, ExpenseStatus.POSTED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCount()).isEqualTo(1);
        assertThat(result.get(0).getNetAmount()).isEqualByComparingTo("100.00");
        assertThat(result.get(0).getTaxAmount()).isEqualByComparingTo("15.00");
        assertThat(result.get(0).getGrossAmount()).isEqualByComparingTo("115.00");
    }

    @Test
    void post_otherPaymentMethodUsesExpenseClearingAccount() {
        JournalEntryRepository journalRepository = org.mockito.Mockito.mock(JournalEntryRepository.class);
        AccountRepository localAccountRepository = org.mockito.Mockito.mock(AccountRepository.class);
        AccountingPostingServiceImpl realPostingService = new AccountingPostingServiceImpl(journalRepository, localAccountRepository, activityLogService, taxSettingsRepository);
        ExpenseServiceImpl realPostingExpenseService = new ExpenseServiceImpl(expenseRepository, categoryRepository, new AccountingMapper(),
                activityLogService, auditLogService, realPostingService, journalRepository, fileStorageService);
        Expense expense = expense(100L, ExpenseStatus.APPROVED, AccountingPaymentMethod.OTHER);
        Account clearing = account(20L, "2100", "Expense Clearing", AccountType.LIABILITY);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(journalRepository.existsBySourceTypeAndSourceId("EXPENSE", 100L)).thenReturn(false);
        when(journalRepository.findMaxId()).thenReturn(0L);
        when(journalRepository.existsByJournalNo("JRN-0001")).thenReturn(false);
        when(localAccountRepository.findByAccountNameIgnoreCase("Expense Clearing")).thenReturn(Optional.of(clearing));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        realPostingExpenseService.post(100L);

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getLines()).anySatisfy(line -> {
            assertThat(line.getAccount().getAccountName()).isEqualTo("Expense Clearing");
            assertThat(line.getCredit()).isEqualByComparingTo("125.00");
        });
    }

    @Test
    void posting_taxableExpenseCreatesTaxLine() {
        JournalEntryRepository journalRepository = org.mockito.Mockito.mock(JournalEntryRepository.class);
        AccountRepository localAccountRepository = org.mockito.Mockito.mock(AccountRepository.class);
        AccountingPostingServiceImpl realPostingService = new AccountingPostingServiceImpl(journalRepository, localAccountRepository, activityLogService, taxSettingsRepository);
        Expense expense = expense(100L, ExpenseStatus.APPROVED, AccountingPaymentMethod.CASH);
        expense.setNetAmount(new BigDecimal("100.00"));
        expense.setTaxAmount(new BigDecimal("15.00"));
        expense.setGrossAmount(new BigDecimal("115.00"));
        Account cash = account(1L, "1000", "Cash", AccountType.ASSET);
        Account taxReceivable = account(2L, "1300", "Tax Receivable", AccountType.ASSET);
        TaxSettings settings = new TaxSettings();
        settings.setTaxReceivableAccount(taxReceivable);
        when(journalRepository.existsBySourceTypeAndSourceId("EXPENSE", 100L)).thenReturn(false);
        when(journalRepository.findMaxId()).thenReturn(0L);
        when(journalRepository.existsByJournalNo("JRN-0001")).thenReturn(false);
        when(localAccountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(cash));
        when(taxSettingsRepository.findById(1L)).thenReturn(Optional.of(settings));

        realPostingService.postExpense(expense);

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getLines()).anySatisfy(line -> {
            assertThat(line.getAccount().getAccountName()).isEqualTo("Tax Receivable");
            assertThat(line.getDebit()).isEqualByComparingTo("15.00");
        });
        assertThat(captor.getValue().getLines()).anySatisfy(line -> {
            assertThat(line.getAccount().getAccountName()).isEqualTo("Cash");
            assertThat(line.getCredit()).isEqualByComparingTo("115.00");
        });
    }

    @Test
    void reversalJournalCreated() {
        JournalEntryRepository journalRepository = org.mockito.Mockito.mock(JournalEntryRepository.class);
        AccountRepository localAccountRepository = org.mockito.Mockito.mock(AccountRepository.class);
        AccountingPostingServiceImpl realPostingService = new AccountingPostingServiceImpl(journalRepository, localAccountRepository, activityLogService, taxSettingsRepository);
        Expense expense = expense(100L, ExpenseStatus.POSTED, AccountingPaymentMethod.CASH);
        Account cash = account(1L, "1000", "Cash", AccountType.ASSET);
        JournalEntry original = new JournalEntry();
        original.setJournalNo("JRN-0001");
        when(journalRepository.existsBySourceTypeAndSourceId("EXPENSE_REVERSAL", 100L)).thenReturn(false);
        when(journalRepository.findBySource("EXPENSE", 100L)).thenReturn(Optional.of(original));
        when(journalRepository.findMaxId()).thenReturn(1L);
        when(journalRepository.existsByJournalNo("JRN-0002")).thenReturn(false);
        when(localAccountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(cash));

        realPostingService.reverseExpense(expense, "Mistake");

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getSourceType()).isEqualTo("EXPENSE_REVERSAL");
        assertThat(captor.getValue().getLines()).anySatisfy(line -> {
            assertThat(line.getAccount().getAccountName()).isEqualTo("Cash");
            assertThat(line.getDebit()).isEqualByComparingTo("125.00");
        });
    }

    private ExpenseDTO dto() {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setExpenseDate(LocalDate.of(2026, 6, 18));
        dto.setCategoryId(1L);
        dto.setAmount(new BigDecimal("125.00"));
        dto.setPaymentMethod(AccountingPaymentMethod.CASH);
        dto.setReferenceNo("REF-1");
        dto.setNotes("Office fuel");
        return dto;
    }

    private Expense expense(Long id, ExpenseStatus status, AccountingPaymentMethod method) {
        Expense expense = new Expense();
        expense.setId(id);
        expense.setExpenseNo("EXP-0100");
        expense.setExpenseDate(LocalDate.of(2026, 6, 18));
        expense.setAmount(new BigDecimal("125.00"));
        expense.setPaymentMethod(method);
        expense.setCategory(category(1L, account(10L, "5100", "Fuel Expense", AccountType.EXPENSE)));
        expense.setStatus(status);
        return expense;
    }

    private void assertPostBlocked(ExpenseStatus status) {
        Expense expense = expense(100L, status, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("EXPENSE", 100L)).thenReturn(false);

        assertThatThrownBy(() -> service.post(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only approved expenses can be posted");
        verify(accountingPostingService, never()).postExpense(expense);
    }

    private ExpenseCategory category(Long id, Account account) {
        ExpenseCategory category = new ExpenseCategory();
        category.setId(id);
        category.setName("Fuel");
        category.setStatus(Status.ACTIVE);
        category.setAccount(account);
        return category;
    }

    private Account account(Long id, String code, String name, AccountType type) {
        Account account = new Account();
        account.setId(id);
        account.setAccountCode(code);
        account.setAccountName(name);
        account.setAccountType(type);
        account.setStatus(Status.ACTIVE);
        return account;
    }
}
