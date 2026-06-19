package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.dto.*;
import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryLineRepository;
import com.sme.erp.accounting.service.AccountingReportService;
import com.sme.erp.accounting.service.FinancialConsistencyService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.enums.Status;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.entity.PurchaseReturn;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.repository.PurchaseReturnRepository;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.entity.SalesReturn;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesReturnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AccountingReportServiceImpl implements AccountingReportService {
    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository lineRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesReturnRepository salesReturnRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseReturnRepository purchaseReturnRepository;
    private final FinancialConsistencyService consistencyService;

    public AccountingReportServiceImpl(AccountRepository accountRepository,
                                       JournalEntryLineRepository lineRepository,
                                       SalesInvoiceRepository salesInvoiceRepository,
                                       SalesReturnRepository salesReturnRepository,
                                       PurchaseOrderRepository purchaseOrderRepository,
                                       PurchaseReturnRepository purchaseReturnRepository,
                                       FinancialConsistencyService consistencyService) {
        this.accountRepository = accountRepository;
        this.lineRepository = lineRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesReturnRepository = salesReturnRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.consistencyService = consistencyService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerEntryDTO> getCustomerLedger(Long customerId, LocalDate fromDate, LocalDate toDate) {
        List<RawLedgerEntry> rows = new ArrayList<>();
        for (SalesInvoice invoice : salesInvoiceRepository.findAll()) {
            if (!isPostedSale(invoice) || !matchesCustomer(invoice, customerId) || !within(invoice.getSaleDate().toLocalDate(), fromDate, toDate)) {
                continue;
            }
            BigDecimal netTotal = safe(invoice.getNetTotal());
            BigDecimal paid = safe(invoice.getPaidAmount()).min(netTotal);
            rows.add(new RawLedgerEntry(invoice.getSaleDate().toLocalDate(), "Accounts Receivable", invoice.getInvoiceNo(), customerName(invoice) + " invoice", netTotal, BigDecimal.ZERO));
            if (paid.signum() > 0) {
                rows.add(new RawLedgerEntry(invoice.getSaleDate().toLocalDate(), "Accounts Receivable", invoice.getInvoiceNo(), customerName(invoice) + " payment", BigDecimal.ZERO, paid));
            }
        }
        for (SalesReturn salesReturn : salesReturnRepository.findAll()) {
            if (!matchesCustomer(salesReturn, customerId) || !within(salesReturn.getReturnDate().toLocalDate(), fromDate, toDate)) {
                continue;
            }
            rows.add(new RawLedgerEntry(salesReturn.getReturnDate().toLocalDate(), "Accounts Receivable", salesReturn.getReturnCode(), customerName(salesReturn) + " sales return", BigDecimal.ZERO, safe(salesReturn.getTotalAmount())));
        }
        if (customerId == null) {
            rows.addAll(manualAccountRows("Accounts Receivable", fromDate, toDate));
        }
        return withBalance(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerEntryDTO> getSupplierLedger(Long supplierId, LocalDate fromDate, LocalDate toDate) {
        List<RawLedgerEntry> rows = new ArrayList<>();
        for (PurchaseOrder order : purchaseOrderRepository.findAll()) {
            if (!isPostedPurchase(order) || !matchesSupplier(order, supplierId) || !within(order.getPurchaseDate().toLocalDate(), fromDate, toDate)) {
                continue;
            }
            BigDecimal netTotal = safe(order.getNetTotal());
            BigDecimal paid = safe(order.getPaidAmount()).min(netTotal);
            rows.add(new RawLedgerEntry(order.getPurchaseDate().toLocalDate(), "Accounts Payable", order.getPurchaseCode(), supplierName(order) + " purchase", BigDecimal.ZERO, netTotal));
            if (paid.signum() > 0) {
                rows.add(new RawLedgerEntry(order.getPurchaseDate().toLocalDate(), "Accounts Payable", order.getPurchaseCode(), supplierName(order) + " payment", paid, BigDecimal.ZERO));
            }
        }
        for (PurchaseReturn purchaseReturn : purchaseReturnRepository.findAll()) {
            if (purchaseReturn.getStatus() != PurchaseStatus.POSTED
                    || !matchesSupplier(purchaseReturn, supplierId)
                    || !within(purchaseReturn.getReturnDate().toLocalDate(), fromDate, toDate)) {
                continue;
            }
            rows.add(new RawLedgerEntry(purchaseReturn.getReturnDate().toLocalDate(), "Accounts Payable", purchaseReturn.getReturnCode(), supplierName(purchaseReturn) + " purchase return", safe(purchaseReturn.getTotalAmount()), BigDecimal.ZERO));
        }
        if (supplierId == null) {
            rows.addAll(manualAccountRows("Accounts Payable", fromDate, toDate));
        }
        return withBalance(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralLedgerDTO getGeneralLedger(LocalDate fromDate, LocalDate toDate) {
        validatePeriod(fromDate, toDate);
        List<GeneralLedgerRowDTO> rows = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (Account account : sortedAccounts()) {
            BigDecimal opening = openingBalance(account.getId(), fromDate);
            BigDecimal debit = safe(lineRepository.sumDebit(account.getId(), fromDate, toDate));
            BigDecimal credit = safe(lineRepository.sumCredit(account.getId(), fromDate, toDate));
            BigDecimal closing = opening.add(debit).subtract(credit);
            rows.add(new GeneralLedgerRowDTO(account.getId(), account.getAccountCode(), account.getAccountName(),
                    account.getAccountType(), opening, debit, credit, closing));
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }
        FinancialValidationDTO validation = validate(totalDebit, totalCredit);
        return new GeneralLedgerDTO(rows, totalDebit, totalCredit, validation.outOfBalance(), validation.differenceAmount());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountLedgerDTO getAccountLedger(Long accountId, LocalDate fromDate, LocalDate toDate) {
        validatePeriod(fromDate, toDate);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        BigDecimal opening = openingBalance(accountId, fromDate);
        BigDecimal running = opening;
        List<AccountLedgerEntryDTO> transactions = new ArrayList<>();
        for (JournalEntryLine line : lineRepository.findPostedLedgerLines(accountId, fromDate, toDate)) {
            BigDecimal debit = safe(line.getDebit());
            BigDecimal credit = safe(line.getCredit());
            running = running.add(debit).subtract(credit);
            String referenceType = line.getJournalEntry().getReferenceType() != null
                    ? line.getJournalEntry().getReferenceType() : line.getJournalEntry().getSourceType();
            String referenceNo = line.getJournalEntry().getReferenceNo() != null
                    ? line.getJournalEntry().getReferenceNo() : line.getJournalEntry().getSourceNo();
            transactions.add(new AccountLedgerEntryDTO(line.getJournalEntry().getJournalDate(),
                    line.getJournalEntry().getJournalNo(), referenceType, referenceNo,
                    line.getCostCenter() == null ? "Unassigned" : line.getCostCenter().getCode(),
                    line.getCostCenter() == null ? "Unassigned" : line.getCostCenter().getName(),
                    line.getDescription() != null ? line.getDescription() : line.getJournalEntry().getDescription(),
                    debit, credit, running));
        }
        return new AccountLedgerDTO(account.getId(), account.getAccountCode(), account.getAccountName(),
                account.getAccountType(), opening, running, transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfitLossDTO getProfitLoss(LocalDate fromDate, LocalDate toDate) {
        validatePeriod(fromDate, toDate);
        List<FinancialStatementLineDTO> income = new ArrayList<>();
        List<FinancialStatementLineDTO> expenses = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (Account account : sortedAccounts()) {
            BigDecimal debit = safe(lineRepository.sumDebit(account.getId(), fromDate, toDate));
            BigDecimal credit = safe(lineRepository.sumCredit(account.getId(), fromDate, toDate));
            if (account.getAccountType() == AccountType.INCOME) {
                BigDecimal amount = credit.subtract(debit);
                income.add(statementLine(account, incomeGroup(account), amount));
                totalIncome = totalIncome.add(amount);
            } else if (account.getAccountType() == AccountType.EXPENSE) {
                BigDecimal amount = debit.subtract(credit);
                expenses.add(statementLine(account, expenseGroup(account), amount));
                totalExpense = totalExpense.add(amount);
            }
        }
        BigDecimal netProfitLoss = totalIncome.subtract(totalExpense);
        return new ProfitLossDTO(income, expenses, totalIncome, totalExpense, netProfitLoss, false, BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public TrialBalanceDTO getTrialBalance(LocalDate fromDate, LocalDate toDate) {
        List<TrialBalanceRowDTO> rows = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (Account account : accountRepository.search(null, Status.ACTIVE)) {
            BigDecimal net = safe(lineRepository.sumDebit(account.getId(), fromDate, toDate))
                    .subtract(safe(lineRepository.sumCredit(account.getId(), fromDate, toDate)));
            BigDecimal debitBalance = net.signum() > 0 ? net : BigDecimal.ZERO;
            BigDecimal creditBalance = net.signum() < 0 ? net.abs() : BigDecimal.ZERO;
            if (debitBalance.signum() != 0 || creditBalance.signum() != 0) {
                rows.add(new TrialBalanceRowDTO(account.getAccountCode(), account.getAccountName(), account.getAccountType(), debitBalance, creditBalance));
            }
            totalDebit = totalDebit.add(debitBalance);
            totalCredit = totalCredit.add(creditBalance);
        }
        return new TrialBalanceDTO(rows, totalDebit, totalCredit);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceSheetDTO getBalanceSheet(LocalDate asOfDate) {
        List<FinancialStatementLineDTO> assets = new ArrayList<>();
        List<FinancialStatementLineDTO> liabilities = new ArrayList<>();
        List<FinancialStatementLineDTO> equity = new ArrayList<>();
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal postedEquity = BigDecimal.ZERO;
        BigDecimal ownerCapital = BigDecimal.ZERO;
        BigDecimal retainedEarnings = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (Account account : sortedAccounts()) {
            BigDecimal debit = safe(lineRepository.sumDebit(account.getId(), null, asOfDate));
            BigDecimal credit = safe(lineRepository.sumCredit(account.getId(), null, asOfDate));
            if (account.getAccountType() == AccountType.ASSET) {
                BigDecimal amount = debit.subtract(credit);
                assets.add(statementLine(account, assetGroup(account), amount));
                totalAssets = totalAssets.add(amount);
            } else if (account.getAccountType() == AccountType.LIABILITY) {
                BigDecimal amount = credit.subtract(debit);
                liabilities.add(statementLine(account, liabilityGroup(account), amount));
                totalLiabilities = totalLiabilities.add(amount);
            } else if (account.getAccountType() == AccountType.EQUITY) {
                BigDecimal amount = credit.subtract(debit);
                equity.add(statementLine(account, equityGroup(account), amount));
                postedEquity = postedEquity.add(amount);
                if (contains(account, "retained")) retainedEarnings = retainedEarnings.add(amount);
                else ownerCapital = ownerCapital.add(amount);
            } else if (account.getAccountType() == AccountType.INCOME) {
                totalIncome = totalIncome.add(credit.subtract(debit));
            } else if (account.getAccountType() == AccountType.EXPENSE) {
                totalExpense = totalExpense.add(debit.subtract(credit));
            }
        }
        BigDecimal currentProfitLoss = totalIncome.subtract(totalExpense);
        BigDecimal totalEquity = postedEquity.add(currentProfitLoss);
        BigDecimal liabilitiesAndEquity = totalLiabilities.add(totalEquity);
        FinancialValidationDTO validation = validate(totalAssets, liabilitiesAndEquity);
        return new BalanceSheetDTO(assets, liabilities, equity, totalAssets, totalLiabilities,
                ownerCapital, retainedEarnings, currentProfitLoss, totalEquity, liabilitiesAndEquity,
                validation.outOfBalance(), validation.differenceAmount());
    }

    private List<RawLedgerEntry> manualAccountRows(String accountName, LocalDate fromDate, LocalDate toDate) {
        Account account = accountRepository.findByAccountNameIgnoreCase(accountName).orElse(null);
        if (account == null) {
            return List.of();
        }
        return lineRepository.findPostedLedgerLines(account.getId(), fromDate, toDate).stream()
                .filter(line -> line.getJournalEntry().getSourceType() == null)
                .map(line -> new RawLedgerEntry(line.getJournalEntry().getJournalDate(), accountName, reference(line), line.getDescription(), safe(line.getDebit()), safe(line.getCredit())))
                .toList();
    }

    private List<Account> sortedAccounts() {
        return accountRepository.findAll().stream()
                .sorted(Comparator.comparing(Account::getAccountCode))
                .toList();
    }

    private BigDecimal openingBalance(Long accountId, LocalDate fromDate) {
        if (fromDate == null) return BigDecimal.ZERO;
        return safe(lineRepository.sumDebit(accountId, null, fromDate.minusDays(1)))
                .subtract(safe(lineRepository.sumCredit(accountId, null, fromDate.minusDays(1))));
    }

    private FinancialValidationDTO validate(BigDecimal left, BigDecimal right) {
        return consistencyService.validate(left, right);
    }

    private void validatePeriod(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("fromDate cannot be after toDate");
        }
    }

    private FinancialStatementLineDTO statementLine(Account account, String group, BigDecimal amount) {
        return new FinancialStatementLineDTO(account.getId(), account.getAccountCode(), account.getAccountName(), group, amount);
    }

    private String incomeGroup(Account account) {
        if (contains(account, "service")) return "Service Income";
        if (contains(account, "sales")) return "Sales Income";
        return "Other Income";
    }

    private String expenseGroup(Account account) {
        if (contains(account, "tax")) return "Tax Expenses";
        if (contains(account, "admin")) return "Administrative Expenses";
        if (contains(account, "purchase", "cost of goods", "cogs")) return "Purchase Expenses";
        if (contains(account, "operating")) return "Operating Expenses";
        return "Other Expenses";
    }

    private String assetGroup(Account account) {
        if (contains(account, "cash")) return "Cash";
        if (contains(account, "bank")) return "Bank";
        if (contains(account, "receivable")) return "Accounts Receivable";
        if (contains(account, "inventory", "stock")) return "Inventory";
        if (contains(account, "supplier advance", "advance to supplier")) return "Supplier Advance";
        return "Other Assets";
    }

    private String liabilityGroup(Account account) {
        if (contains(account, "payable") && contains(account, "tax")) return "Taxes Payable";
        if (contains(account, "accounts payable", "supplier payable")) return "Accounts Payable";
        return "Other Liabilities";
    }

    private String equityGroup(Account account) {
        return contains(account, "retained") ? "Retained Earnings" : "Owner Capital";
    }

    private boolean contains(Account account, String... tokens) {
        String parent = account.getParentAccount() == null ? "" : account.getParentAccount().getAccountName();
        String value = (account.getAccountName() + " " + account.getAccountCode() + " " + parent).toLowerCase();
        for (String token : tokens) if (value.contains(token)) return true;
        return false;
    }

    private List<LedgerEntryDTO> withBalance(List<RawLedgerEntry> rawRows) {
        List<RawLedgerEntry> sorted = rawRows.stream()
                .sorted(Comparator.comparing(RawLedgerEntry::date).thenComparing(RawLedgerEntry::referenceNo))
                .toList();
        List<LedgerEntryDTO> rows = new ArrayList<>();
        BigDecimal balance = BigDecimal.ZERO;
        for (RawLedgerEntry row : sorted) {
            balance = balance.add(row.debit()).subtract(row.credit());
            rows.add(new LedgerEntryDTO(row.date(), row.account(), row.referenceNo(), row.description(), row.debit(), row.credit(), balance));
        }
        return rows;
    }

    private BigDecimal debitBalance(String accountName) {
        Account account = requiredAccount(accountName);
        return safe(lineRepository.sumDebit(account.getId(), null, null)).subtract(safe(lineRepository.sumCredit(account.getId(), null, null)));
    }

    private BigDecimal creditBalance(String accountName) {
        Account account = requiredAccount(accountName);
        return safe(lineRepository.sumCredit(account.getId(), null, null)).subtract(safe(lineRepository.sumDebit(account.getId(), null, null)));
    }

    private Account requiredAccount(String accountName) {
        return accountRepository.findByAccountNameIgnoreCase(accountName).orElseThrow();
    }

    private boolean isPostedSale(SalesInvoice invoice) {
        return invoice != null && invoice.getSaleDate() != null
                && (invoice.getStatus() == SalesInvoiceStatus.POSTED
                || invoice.getStatus() == SalesInvoiceStatus.PARTIAL_PAID
                || invoice.getStatus() == SalesInvoiceStatus.PAID
                || invoice.getStatus() == SalesInvoiceStatus.CONFIRMED
                || invoice.getStatus() == SalesInvoiceStatus.COMPLETED);
    }

    private boolean isPostedPurchase(PurchaseOrder order) {
        return order != null && order.getPurchaseDate() != null
                && (order.getStatus() == PurchaseStatus.RECEIVED
                || order.getStatus() == PurchaseStatus.PARTIAL_PAID
                || order.getStatus() == PurchaseStatus.PAID);
    }

    private boolean within(LocalDate date, LocalDate fromDate, LocalDate toDate) {
        return date != null
                && (fromDate == null || !date.isBefore(fromDate))
                && (toDate == null || !date.isAfter(toDate));
    }

    private boolean matchesCustomer(SalesInvoice invoice, Long customerId) {
        return customerId == null || (invoice.getCustomer() != null && customerId.equals(invoice.getCustomer().getId()));
    }

    private boolean matchesCustomer(SalesReturn salesReturn, Long customerId) {
        return customerId == null || (salesReturn.getCustomer() != null && customerId.equals(salesReturn.getCustomer().getId()));
    }

    private boolean matchesSupplier(PurchaseOrder order, Long supplierId) {
        return supplierId == null || (order.getSupplier() != null && supplierId.equals(order.getSupplier().getId()));
    }

    private boolean matchesSupplier(PurchaseReturn purchaseReturn, Long supplierId) {
        return supplierId == null || (purchaseReturn.getSupplier() != null && supplierId.equals(purchaseReturn.getSupplier().getId()));
    }

    private String customerName(SalesInvoice invoice) {
        return invoice.getCustomer() == null ? "Customer" : invoice.getCustomer().getName();
    }

    private String customerName(SalesReturn salesReturn) {
        return salesReturn.getCustomer() == null ? "Customer" : salesReturn.getCustomer().getName();
    }

    private String supplierName(PurchaseOrder order) {
        return order.getSupplier() == null ? "Supplier" : order.getSupplier().getName();
    }

    private String supplierName(PurchaseReturn purchaseReturn) {
        return purchaseReturn.getSupplier() == null ? "Supplier" : purchaseReturn.getSupplier().getName();
    }

    private String reference(JournalEntryLine line) {
        return line.getJournalEntry().getSourceNo() != null ? line.getJournalEntry().getSourceNo() : line.getJournalEntry().getJournalNo();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record RawLedgerEntry(LocalDate date, String account, String referenceNo, String description, BigDecimal debit, BigDecimal credit) {}
}
