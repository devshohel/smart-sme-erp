package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.JournalStatus;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.common.exception.ResourceNotFoundException;
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

    public AccountingPostingServiceImpl(JournalEntryRepository journalEntryRepository, AccountRepository accountRepository, ActivityLogService activityLogService) {
        this.journalEntryRepository = journalEntryRepository;
        this.accountRepository = accountRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    @Transactional
    public void postExpense(Expense expense) {
        if (expense == null || expense.getId() == null || isPosted("EXPENSE", expense.getId())) {
            return;
        }
        Account paymentAccount = paymentAccount(expense.getPaymentMethod());
        if (paymentAccount == null) {
            // TODO Add posting support for OTHER payment method once a clearing account exists.
            return;
        }
        JournalEntry entry = baseEntry("EXPENSE", expense.getId(), expense.getExpenseNo(), expense.getExpenseDate(), "Expense posting " + expense.getExpenseNo());
        addLine(entry, account("Operating Expense"), safe(expense.getAmount()), BigDecimal.ZERO, "Expense");
        addLine(entry, paymentAccount, BigDecimal.ZERO, safe(expense.getAmount()), "Expense payment");
        saveIfBalanced(entry);
        activityLogService.log("ACCOUNTING_POST_EXPENSE", "ACCOUNTING", "accounting_journal_entries", entry.getId(), "Posted expense " + expense.getExpenseNo());
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
        BigDecimal paid = safe(invoice.getPaidAmount()).min(netTotal);
        if (paid.signum() > 0) {
            // TODO Sales invoices do not currently store payment method, so paid amount posts to Cash by default.
            addLine(entry, account("Cash"), paid, BigDecimal.ZERO, "Customer payment");
            addLine(entry, account("Accounts Receivable"), BigDecimal.ZERO, paid, "Receivable collection");
        }
        saveIfBalanced(entry);
        activityLogService.log("ACCOUNTING_POST_SALES_INVOICE", "ACCOUNTING", "accounting_journal_entries", entry.getId(), "Posted sales invoice " + invoice.getInvoiceNo());
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
            // TODO Purchases do not currently store payment method, so paid amount posts to Cash by default.
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
        if (method == AccountingPaymentMethod.BANK) {
            return account("Bank");
        }
        return null;
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
