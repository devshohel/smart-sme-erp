package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.accounting.entity.ExpenseCategory;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.JournalStatus;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.settings.entity.TaxSettings;
import com.sme.erp.settings.repository.TaxSettingsRepository;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.entity.PurchaseReturn;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.entity.SalesReturn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class AccountingPostingServiceImpl implements AccountingPostingService {
    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final ActivityLogService activityLogService;
    private final TaxSettingsRepository taxSettingsRepository;

    public AccountingPostingServiceImpl(JournalEntryRepository journalEntryRepository, AccountRepository accountRepository, ActivityLogService activityLogService, TaxSettingsRepository taxSettingsRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.accountRepository = accountRepository;
        this.activityLogService = activityLogService;
        this.taxSettingsRepository = taxSettingsRepository;
    }

    @Override
    @Transactional
    public void postExpense(Expense expense) {
        if (expense == null || expense.getId() == null || isPosted("EXPENSE", expense.getId())) {
            return;
        }
        Account paymentAccount = paymentAccount(expense.getPaymentMethod());
        Account expenseAccount = expenseAccount(expense.getCategory());
        BigDecimal netAmount = expenseNetAmount(expense);
        BigDecimal taxAmount = expenseTaxAmount(expense);
        BigDecimal grossAmount = expenseGrossAmount(expense);
        JournalEntry entry = baseEntry("EXPENSE", expense.getId(), expense.getExpenseNo(), expense.getExpenseDate(), "Expense posting " + expense.getExpenseNo());
        addLine(entry, expenseAccount, netAmount, BigDecimal.ZERO, "Expense");
        if (taxAmount.signum() > 0) {
            addLine(entry, taxReceivableAccount(), taxAmount, BigDecimal.ZERO, "Expense tax receivable");
        }
        addLine(entry, paymentAccount, BigDecimal.ZERO, grossAmount, "Expense payment");
        saveIfBalanced(entry);
        activityLogService.log("ACCOUNTING_POST_EXPENSE", "ACCOUNTING", "accounting_journal_entries", entry.getId(), "Posted expense " + expense.getExpenseNo());
    }

    @Override
    @Transactional
    public void reverseExpense(Expense expense, String reversalReason) {
        if (expense == null || expense.getId() == null || isPosted("EXPENSE_REVERSAL", expense.getId())) {
            return;
        }
        JournalEntry original = journalEntryRepository.findBySource("EXPENSE", expense.getId())
                .orElseThrow(() -> new BadRequestException("Original expense journal not found"));
        Account paymentAccount = paymentAccount(expense.getPaymentMethod());
        Account expenseAccount = expenseAccount(expense.getCategory());
        BigDecimal netAmount = expenseNetAmount(expense);
        BigDecimal taxAmount = expenseTaxAmount(expense);
        BigDecimal grossAmount = expenseGrossAmount(expense);
        JournalEntry entry = baseEntry("EXPENSE_REVERSAL", expense.getId(), expense.getExpenseNo() + "-REV", LocalDate.now(),
                "Expense reversal " + expense.getExpenseNo() + " against journal " + original.getJournalNo());
        addLine(entry, paymentAccount, grossAmount, BigDecimal.ZERO, "Reverse expense payment");
        addLine(entry, expenseAccount, BigDecimal.ZERO, netAmount, "Reverse expense");
        if (taxAmount.signum() > 0) {
            addLine(entry, taxReceivableAccount(), BigDecimal.ZERO, taxAmount, "Reverse expense tax");
        }
        saveIfBalanced(entry);
        activityLogService.log("ACCOUNTING_REVERSE_EXPENSE", "ACCOUNTING", "accounting_journal_entries", entry.getId(),
                "Reversed expense " + expense.getExpenseNo() + ": " + reversalReason);
    }

    @Override
    @Transactional
    public void postSalesInvoice(SalesInvoice invoice) {
        if (invoice == null || invoice.getId() == null || isPosted("SALES_INVOICE", invoice.getId())) {
            return;
        }
        BigDecimal netTotal = safe(invoice.getNetTotal());
        if (netTotal.signum() <= 0) {
            return;
        }
        JournalEntry entry = baseEntry("SALES_INVOICE", invoice.getId(), invoice.getInvoiceNo(), invoice.getSaleDate().toLocalDate(), "Sales invoice posting " + invoice.getInvoiceNo());
        addLine(entry, account("Accounts Receivable"), netTotal, BigDecimal.ZERO, "Sales receivable");
        addLine(entry, account("Sales Income"), BigDecimal.ZERO, netTotal, "Sales income");
        saveIfBalanced(entry);
        activityLogService.log("ACCOUNTING_POST_SALES_INVOICE", "ACCOUNTING", "accounting_journal_entries", entry.getId(), "Posted sales invoice " + invoice.getInvoiceNo());
    }

    @Override
    @Transactional
    public void reverseSalesInvoice(SalesInvoice invoice, String reversalReason) {
        if (invoice == null || invoice.getId() == null || isPosted("SALES_INVOICE_REVERSAL", invoice.getId())) {
            return;
        }
        JournalEntry original = journalEntryRepository.findBySource("SALES_INVOICE", invoice.getId())
                .orElseThrow(() -> new BadRequestException("Original sales invoice journal not found"));
        BigDecimal netTotal = safe(invoice.getNetTotal());
        if (netTotal.signum() <= 0) {
            return;
        }
        JournalEntry entry = baseEntry("SALES_INVOICE_REVERSAL", invoice.getId(), invoice.getInvoiceNo() + "-REV", LocalDate.now(),
                "Sales invoice reversal " + invoice.getInvoiceNo() + " against journal " + original.getJournalNo());
        addLine(entry, account("Sales Income"), netTotal, BigDecimal.ZERO, "Reverse sales income");
        addLine(entry, account("Accounts Receivable"), BigDecimal.ZERO, netTotal, "Reverse receivable");
        saveIfBalanced(entry);
        activityLogService.log("ACCOUNTING_REVERSE_SALES_INVOICE", "ACCOUNTING", "accounting_journal_entries", entry.getId(),
                "Reversed sales invoice " + invoice.getInvoiceNo() + ": " + reversalReason);
    }

    @Override
    @Transactional
    public void postPurchase(PurchaseOrder purchaseOrder) {
        if (purchaseOrder == null || purchaseOrder.getId() == null || isPosted("PURCHASE", purchaseOrder.getId())) {
            return;
        }
        BigDecimal netTotal = safe(purchaseOrder.getNetTotal());
        if (netTotal.signum() <= 0) {
            return;
        }
        JournalEntry entry = baseEntry("PURCHASE", purchaseOrder.getId(), purchaseOrder.getPurchaseCode(), purchaseOrder.getPurchaseDate().toLocalDate(), "Purchase posting " + purchaseOrder.getPurchaseCode());
        addLine(entry, account("Purchase Cost"), netTotal, BigDecimal.ZERO, "Purchase cost");
        addLine(entry, account("Accounts Payable"), BigDecimal.ZERO, netTotal, "Supplier payable");
        BigDecimal paid = safe(purchaseOrder.getPaidAmount()).min(netTotal);
        if (paid.signum() > 0) {
            // Purchases do not currently store payment method, so paid amount posts to Cash by default.
            addLine(entry, account("Accounts Payable"), paid, BigDecimal.ZERO, "Supplier payment");
            addLine(entry, account("Cash"), BigDecimal.ZERO, paid, "Purchase payment");
        }
        saveIfBalanced(entry);
        activityLogService.log("ACCOUNTING_POST_PURCHASE", "ACCOUNTING", "accounting_journal_entries", entry.getId(), "Posted purchase " + purchaseOrder.getPurchaseCode());
    }

    @Override
    @Transactional
    public void postSalesReturn(SalesReturn salesReturn) {
        if (salesReturn == null || salesReturn.getId() == null || isPosted("SALES_RETURN", salesReturn.getId())) {
            return;
        }
        BigDecimal amount = safe(salesReturn.getTotalAmount());
        if (amount.signum() <= 0) {
            return;
        }
        JournalEntry entry = baseEntry("SALES_RETURN", salesReturn.getId(), salesReturn.getReturnCode(), salesReturn.getReturnDate().toLocalDate(), "Sales return posting " + salesReturn.getReturnCode());
        addLine(entry, account("Sales Income"), amount, BigDecimal.ZERO, "Sales return adjustment");
        addLine(entry, account("Accounts Receivable"), BigDecimal.ZERO, amount, "Receivable reduction");
        saveIfBalanced(entry);
        activityLogService.log("ACCOUNTING_POST_SALES_INVOICE", "ACCOUNTING", "accounting_journal_entries", entry.getId(), "Posted sales return " + salesReturn.getReturnCode());
    }

    @Override
    @Transactional
    public void postPurchaseReturn(PurchaseReturn purchaseReturn) {
        if (purchaseReturn == null || purchaseReturn.getId() == null || isPosted("PURCHASE_RETURN", purchaseReturn.getId())) {
            return;
        }
        BigDecimal amount = safe(purchaseReturn.getTotalAmount());
        if (amount.signum() <= 0) {
            return;
        }
        JournalEntry entry = baseEntry("PURCHASE_RETURN", purchaseReturn.getId(), purchaseReturn.getReturnCode(), purchaseReturn.getReturnDate().toLocalDate(), "Purchase return posting " + purchaseReturn.getReturnCode());
        addLine(entry, account("Accounts Payable"), amount, BigDecimal.ZERO, "Payable reduction");
        addLine(entry, account("Purchase Cost"), BigDecimal.ZERO, amount, "Purchase return adjustment");
        saveIfBalanced(entry);
        activityLogService.log("ACCOUNTING_POST_PURCHASE", "ACCOUNTING", "accounting_journal_entries", entry.getId(), "Posted purchase return " + purchaseReturn.getReturnCode());
    }

    private JournalEntry baseEntry(String sourceType, Long sourceId, String sourceNo, LocalDate date, String description) {
        JournalEntry entry = new JournalEntry();
        entry.setJournalNo(nextJournalNo());
        entry.setJournalDate(date != null ? date : LocalDate.now());
        entry.setReferenceNo(sourceNo);
        entry.setDescription(description);
        entry.setSourceType(sourceType);
        entry.setSourceId(sourceId);
        entry.setSourceNo(sourceNo);
        entry.setStatus(JournalStatus.POSTED);
        return entry;
    }

    private void addLine(JournalEntry entry, Account account, BigDecimal debit, BigDecimal credit, String description) {
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account);
        line.setDebit(safe(debit));
        line.setCredit(safe(credit));
        line.setDescription(description);
        entry.addLine(line);
    }

    private void saveIfBalanced(JournalEntry entry) {
        BigDecimal debit = entry.getLines().stream().map(JournalEntryLine::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credit = entry.getLines().stream().map(JournalEntryLine::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (debit.compareTo(credit) == 0 && debit.signum() > 0) {
            journalEntryRepository.save(entry);
        }
    }

    private boolean isPosted(String sourceType, Long sourceId) {
        return journalEntryRepository.existsBySourceTypeAndSourceId(sourceType, sourceId);
    }

    private Account paymentAccount(AccountingPaymentMethod method) {
        if (method == AccountingPaymentMethod.CASH) {
            return account("Cash");
        }
        if (method == AccountingPaymentMethod.BANK || method == AccountingPaymentMethod.MOBILE_BANKING) {
            return account("Bank");
        }
        if (method == AccountingPaymentMethod.OTHER) {
            return account("Expense Clearing");
        }
        throw new BadRequestException("Payment method is required for expense posting");
    }

    private Account expenseAccount(ExpenseCategory category) {
        if (category == null || category.getAccount() == null) {
            throw new BadRequestException("Expense category must have a mapped GL account before posting");
        }
        return category.getAccount();
    }

    private Account taxReceivableAccount() {
        TaxSettings settings = taxSettingsRepository.findById(1L)
                .orElseThrow(() -> new BadRequestException("Tax settings are required for expense tax posting"));
        if (settings.getTaxReceivableAccount() == null) {
            throw new BadRequestException("Tax receivable account must be configured before posting taxable expenses");
        }
        return settings.getTaxReceivableAccount();
    }

    private BigDecimal expenseNetAmount(Expense expense) {
        BigDecimal value = safe(expense.getNetAmount());
        return value.signum() > 0 ? value : safe(expense.getAmount());
    }

    private BigDecimal expenseTaxAmount(Expense expense) {
        return safe(expense.getTaxAmount());
    }

    private BigDecimal expenseGrossAmount(Expense expense) {
        BigDecimal value = safe(expense.getGrossAmount());
        return value.signum() > 0 ? value : safe(expense.getAmount());
    }

    private Account account(String name) {
        return accountRepository.findByAccountNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Required account not found: " + name));
    }

    private String nextJournalNo() {
        long next = journalEntryRepository.findMaxId() + 1;
        String value = String.format("JRN-%04d", next);
        while (journalEntryRepository.existsByJournalNo(value)) {
            next++;
            value = String.format("JRN-%04d", next);
        }
        return value;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
