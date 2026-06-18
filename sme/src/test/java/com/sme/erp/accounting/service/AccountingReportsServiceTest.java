package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.AccountingBookDTO;
import com.sme.erp.accounting.dto.TrialBalanceDTO;
import com.sme.erp.accounting.dto.AccountLedgerDTO;
import com.sme.erp.accounting.dto.BalanceSheetDTO;
import com.sme.erp.accounting.dto.GeneralLedgerDTO;
import com.sme.erp.accounting.dto.ProfitLossDTO;
import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.enums.JournalStatus;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryLineRepository;
import com.sme.erp.accounting.service.impl.AccountingBookServiceImpl;
import com.sme.erp.accounting.service.impl.AccountingReportServiceImpl;
import com.sme.erp.accounting.service.impl.FinancialConsistencyServiceImpl;
import com.sme.erp.enums.Status;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.repository.PurchaseReturnRepository;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesReturnRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountingReportsServiceTest {
    @Mock AccountRepository accountRepository;
    @Mock JournalEntryLineRepository lineRepository;
    @Mock SalesInvoiceRepository salesInvoiceRepository;
    @Mock SalesReturnRepository salesReturnRepository;
    @Mock PurchaseOrderRepository purchaseOrderRepository;
    @Mock PurchaseReturnRepository purchaseReturnRepository;

    @Test void cashBookIncludesPostedCashLinesAndOpeningBalance() {
        Account cash = account(1L, "1000", "Cash", AccountType.ASSET);
        LocalDate from = LocalDate.of(2026, 1, 1);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(cash));
        when(lineRepository.openingBalance(1L, from)).thenReturn(new BigDecimal("25"));
        when(lineRepository.findBookLines(1L, from, null)).thenReturn(List.of(line(cash, JournalStatus.POSTED, "100", "10")));

        AccountingBookDTO report = new AccountingBookServiceImpl(accountRepository, lineRepository).getCashBook(from, null);

        assertThat(report.getOpeningBalance()).isEqualByComparingTo("25");
        assertThat(report.getRows()).hasSize(1);
        assertThat(report.getClosingBalance()).isEqualByComparingTo("115");
    }

    @Test void bankBookUsesOnlyRepositoryPostedJournalQuery() {
        Account bank = account(2L, "1010", "Bank", AccountType.ASSET);
        when(accountRepository.findByAccountNameIgnoreCase("Bank")).thenReturn(Optional.of(bank));
        when(lineRepository.findBookLines(2L, null, null)).thenReturn(List.of(line(bank, JournalStatus.POSTED, "50", "0")));

        AccountingBookDTO report = new AccountingBookServiceImpl(accountRepository, lineRepository).getBankBook(null, null);

        assertThat(report.getRows()).hasSize(1);
        assertThat(report.getRows().get(0).getJournalNo()).isEqualTo("JRN-0001");
    }

    @Test void draftJournalsAreExcludedFromBooks() {
        Account cash = account(1L, "1000", "Cash", AccountType.ASSET);
        when(accountRepository.findByAccountNameIgnoreCase("Cash")).thenReturn(Optional.of(cash));
        when(lineRepository.findBookLines(1L, null, null)).thenReturn(List.of());
        assertThat(new AccountingBookServiceImpl(accountRepository, lineRepository).getCashBook(null, null).getRows()).isEmpty();
    }

    @Test void trialBalanceNetsAccountsAndBalancesTotalsFromPostedLines() {
        Account cash = account(1L, "1000", "Cash", AccountType.ASSET);
        Account income = account(2L, "4000", "Sales Income", AccountType.INCOME);
        when(accountRepository.search(null, Status.ACTIVE)).thenReturn(List.of(cash, income));
        when(lineRepository.sumDebit(1L, null, null)).thenReturn(new BigDecimal("100"));
        when(lineRepository.sumCredit(1L, null, null)).thenReturn(BigDecimal.ZERO);
        when(lineRepository.sumDebit(2L, null, null)).thenReturn(BigDecimal.ZERO);
        when(lineRepository.sumCredit(2L, null, null)).thenReturn(new BigDecimal("100"));
        AccountingReportServiceImpl service = reportService();

        TrialBalanceDTO report = service.getTrialBalance(null, null);

        assertThat(report.getTotalDebit()).isEqualByComparingTo("100");
        assertThat(report.getTotalCredit()).isEqualByComparingTo("100");
        assertThat(report.isBalanced()).isTrue();
    }

    @Test void generalLedgerCalculatesOpeningPeriodTotalsAndClosingBalance() {
        Account cash = account(1L, "1000", "Cash", AccountType.ASSET);
        LocalDate from = LocalDate.of(2026, 2, 1);
        LocalDate to = LocalDate.of(2026, 2, 28);
        when(accountRepository.findAll()).thenReturn(List.of(cash));
        when(lineRepository.sumDebit(1L, null, from.minusDays(1))).thenReturn(new BigDecimal("75"));
        when(lineRepository.sumCredit(1L, null, from.minusDays(1))).thenReturn(new BigDecimal("25"));
        when(lineRepository.sumDebit(1L, from, to)).thenReturn(new BigDecimal("100"));
        when(lineRepository.sumCredit(1L, from, to)).thenReturn(new BigDecimal("30"));

        GeneralLedgerDTO report = reportService().getGeneralLedger(from, to);

        assertThat(report.accounts().get(0).openingBalance()).isEqualByComparingTo("50");
        assertThat(report.accounts().get(0).closingBalance()).isEqualByComparingTo("120");
        assertThat(report.totalDebit()).isEqualByComparingTo("100");
    }

    @Test void accountLedgerCalculatesSequentialRunningBalance() {
        Account cash = account(1L, "1000", "Cash", AccountType.ASSET);
        LocalDate from = LocalDate.of(2026, 2, 1);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(cash));
        when(lineRepository.sumDebit(1L, null, from.minusDays(1))).thenReturn(new BigDecimal("10"));
        when(lineRepository.sumCredit(1L, null, from.minusDays(1))).thenReturn(BigDecimal.ZERO);
        when(lineRepository.findPostedLedgerLines(1L, from, null)).thenReturn(List.of(
                line(cash, JournalStatus.POSTED, "50", "0"),
                line(cash, JournalStatus.POSTED, "0", "5")));

        AccountLedgerDTO report = reportService().getAccountLedger(1L, from, null);

        assertThat(report.transactions()).extracting(row -> row.runningBalance().toPlainString())
                .containsExactly("60", "55");
        assertThat(report.closingBalance()).isEqualByComparingTo("55");
    }

    @Test void profitLossUsesIncomeAndExpenseJournalBalances() {
        Account income = account(4L, "4000", "Sales Income", AccountType.INCOME);
        Account expense = account(5L, "5000", "Operating Expense", AccountType.EXPENSE);
        when(accountRepository.findAll()).thenReturn(List.of(income, expense));
        when(lineRepository.sumDebit(4L, null, null)).thenReturn(BigDecimal.ZERO);
        when(lineRepository.sumCredit(4L, null, null)).thenReturn(new BigDecimal("500"));
        when(lineRepository.sumDebit(5L, null, null)).thenReturn(new BigDecimal("125"));
        when(lineRepository.sumCredit(5L, null, null)).thenReturn(BigDecimal.ZERO);

        ProfitLossDTO report = reportService().getProfitLoss(null, null);

        assertThat(report.totalIncome()).isEqualByComparingTo("500");
        assertThat(report.totalExpense()).isEqualByComparingTo("125");
        assertThat(report.netProfitLoss()).isEqualByComparingTo("375");
    }

    @Test void balanceSheetIncludesCurrentProfitAndRetainedEarnings() {
        Account asset = account(1L, "1000", "Cash", AccountType.ASSET);
        Account liability = account(2L, "2000", "Accounts Payable", AccountType.LIABILITY);
        Account capital = account(3L, "3000", "Owner Capital", AccountType.EQUITY);
        Account retained = account(4L, "3100", "Retained Earnings", AccountType.EQUITY);
        Account income = account(5L, "4000", "Sales Income", AccountType.INCOME);
        Account expense = account(6L, "5000", "Operating Expense", AccountType.EXPENSE);
        when(accountRepository.findAll()).thenReturn(List.of(asset, liability, capital, retained, income, expense));
        balances(1L, "1000", "0"); balances(2L, "0", "400"); balances(3L, "0", "200");
        balances(4L, "0", "100"); balances(5L, "0", "400"); balances(6L, "100", "0");

        BalanceSheetDTO report = reportService().getBalanceSheet(null);

        assertThat(report.retainedEarnings()).isEqualByComparingTo("100");
        assertThat(report.currentProfitLoss()).isEqualByComparingTo("300");
        assertThat(report.totalEquity()).isEqualByComparingTo("600");
        assertThat(report.outOfBalance()).isFalse();
    }

    @Test void postedReversalReducesProfitLossThroughOppositeJournalAmounts() {
        Account income = account(4L, "4000", "Sales Income", AccountType.INCOME);
        when(accountRepository.findAll()).thenReturn(List.of(income));
        // 100 credit original posting less 20 debit posted reversal.
        when(lineRepository.sumDebit(4L, null, null)).thenReturn(new BigDecimal("20"));
        when(lineRepository.sumCredit(4L, null, null)).thenReturn(new BigDecimal("100"));

        assertThat(reportService().getProfitLoss(null, null).netProfitLoss()).isEqualByComparingTo("80");
    }

    private AccountingReportServiceImpl reportService() {
        return new AccountingReportServiceImpl(accountRepository, lineRepository, salesInvoiceRepository,
                salesReturnRepository, purchaseOrderRepository, purchaseReturnRepository,
                new FinancialConsistencyServiceImpl());
    }

    private void balances(Long accountId, String debit, String credit) {
        when(lineRepository.sumDebit(accountId, null, null)).thenReturn(new BigDecimal(debit));
        when(lineRepository.sumCredit(accountId, null, null)).thenReturn(new BigDecimal(credit));
    }

    private JournalEntryLine line(Account account, JournalStatus status, String debit, String credit) {
        JournalEntry journal = new JournalEntry(); journal.setId(1L); journal.setJournalNo("JRN-0001");
        journal.setJournalDate(LocalDate.of(2026, 1, 2)); journal.setStatus(status); journal.setDescription("Test");
        JournalEntryLine line = new JournalEntryLine(); line.setAccount(account);
        line.setDebit(new BigDecimal(debit)); line.setCredit(new BigDecimal(credit)); journal.addLine(line); return line;
    }

    private Account account(Long id, String code, String name, AccountType type) {
        Account account = new Account(); account.setId(id); account.setAccountCode(code); account.setAccountName(name);
        account.setAccountType(type); account.setStatus(Status.ACTIVE); return account;
    }
}
