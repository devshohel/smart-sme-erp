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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private ExpenseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ExpenseServiceImpl(expenseRepository, categoryRepository, new AccountingMapper(),
                activityLogService, auditLogService, accountingPostingService, journalEntryRepository);
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
    void post_postsDraftExpenseAndMarksPosted() {
        Expense expense = expense(100L, ExpenseStatus.DRAFT, AccountingPaymentMethod.CASH);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("EXPENSE", 100L)).thenReturn(false);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDTO result = service.post(100L);

        verify(accountingPostingService).postExpense(expense);
        assertThat(result.getStatus()).isEqualTo(ExpenseStatus.POSTED);
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
        Expense expense = expense(100L, ExpenseStatus.DRAFT, AccountingPaymentMethod.CASH);
        expense.setCategory(category(1L, null));
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));
        when(journalEntryRepository.existsBySourceTypeAndSourceId("EXPENSE", 100L)).thenReturn(false);

        assertThatThrownBy(() -> service.post(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Expense category must have a mapped GL account before posting");
        verify(accountingPostingService, never()).postExpense(any());
    }

    @Test
    void post_otherPaymentMethodUsesExpenseClearingAccount() {
        JournalEntryRepository journalRepository = org.mockito.Mockito.mock(JournalEntryRepository.class);
        AccountRepository localAccountRepository = org.mockito.Mockito.mock(AccountRepository.class);
        AccountingPostingServiceImpl realPostingService = new AccountingPostingServiceImpl(journalRepository, localAccountRepository, activityLogService);
        ExpenseServiceImpl realPostingExpenseService = new ExpenseServiceImpl(expenseRepository, categoryRepository, new AccountingMapper(),
                activityLogService, auditLogService, realPostingService, journalRepository);
        Expense expense = expense(100L, ExpenseStatus.DRAFT, AccountingPaymentMethod.OTHER);
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
