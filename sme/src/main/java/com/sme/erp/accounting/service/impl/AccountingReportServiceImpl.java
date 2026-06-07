package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.dto.BalanceSheetDTO;
import com.sme.erp.accounting.dto.LedgerEntryDTO;
import com.sme.erp.accounting.dto.TrialBalanceDTO;
import com.sme.erp.accounting.dto.TrialBalanceRowDTO;
import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryLineRepository;
import com.sme.erp.accounting.service.AccountingReportService;
import com.sme.erp.enums.Status;
import com.sme.erp.inventory.repository.StockRepository;
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
    private final StockRepository stockRepository;

    public AccountingReportServiceImpl(AccountRepository accountRepository,
                                       JournalEntryLineRepository lineRepository,
                                       SalesInvoiceRepository salesInvoiceRepository,
                                       SalesReturnRepository salesReturnRepository,
                                       PurchaseOrderRepository purchaseOrderRepository,
                                       PurchaseReturnRepository purchaseReturnRepository,
                                       StockRepository stockRepository) {
        this.accountRepository = accountRepository;
        this.lineRepository = lineRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesReturnRepository = salesReturnRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.stockRepository = stockRepository;
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
            if (!matchesSupplier(purchaseReturn, supplierId) || !within(purchaseReturn.getReturnDate().toLocalDate(), fromDate, toDate)) {
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
    public List<LedgerEntryDTO> getGeneralLedger(Long accountId, LocalDate fromDate, LocalDate toDate) {
        List<RawLedgerEntry> rows = lineRepository.findPostedLedgerLines(accountId, fromDate, toDate).stream()
                .map(line -> new RawLedgerEntry(
                        line.getJournalEntry().getJournalDate(),
                        line.getAccount().getAccountCode() + " - " + line.getAccount().getAccountName(),
                        reference(line),
                        line.getDescription() != null ? line.getDescription() : line.getJournalEntry().getDescription(),
                        safe(line.getDebit()),
                        safe(line.getCredit())))
                .toList();
        return withBalance(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public TrialBalanceDTO getTrialBalance(LocalDate fromDate, LocalDate toDate) {
        List<TrialBalanceRowDTO> rows = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (Account account : accountRepository.search(null, Status.ACTIVE)) {
            BigDecimal debit = safe(lineRepository.sumDebit(account.getId(), fromDate, toDate));
            BigDecimal credit = safe(lineRepository.sumCredit(account.getId(), fromDate, toDate));
            rows.add(new TrialBalanceRowDTO(account.getAccountCode(), account.getAccountName(), account.getAccountType(), debit, credit, debit.subtract(credit)));
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }
        return new TrialBalanceDTO(rows, totalDebit, totalCredit);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceSheetDTO getBalanceSheet() {
        BigDecimal cash = debitBalance("Cash");
        BigDecimal bank = debitBalance("Bank");
        BigDecimal receivable = debitBalance("Accounts Receivable");
        BigDecimal inventory = safe(stockRepository.sumInventoryValue());
        BigDecimal payable = creditBalance("Accounts Payable");
        BigDecimal ownerEquity = creditBalance("Owner Equity");
        BigDecimal income = creditBalance("Sales Income");
        BigDecimal purchaseCost = debitBalance("Purchase Cost");
        BigDecimal operatingExpense = debitBalance("Operating Expense");
        BigDecimal retainedEarnings = income.subtract(purchaseCost).subtract(operatingExpense);
        return new BalanceSheetDTO(cash, bank, receivable, inventory, payable, ownerEquity, retainedEarnings);
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
                && (invoice.getStatus() == SalesInvoiceStatus.CONFIRMED || invoice.getStatus() == SalesInvoiceStatus.COMPLETED);
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
