package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.dto.BookEntryDTO;
import com.sme.erp.accounting.dto.AccountingBookDTO;
import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.JournalStatus;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryLineRepository;
import com.sme.erp.accounting.service.AccountingBookService;
import com.sme.erp.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AccountingBookServiceImpl implements AccountingBookService {
    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository lineRepository;

    public AccountingBookServiceImpl(AccountRepository accountRepository, JournalEntryLineRepository lineRepository) {
        this.accountRepository = accountRepository;
        this.lineRepository = lineRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountingBookDTO getCashBook(LocalDate fromDate, LocalDate toDate) {
        return buildBook("Cash", fromDate, toDate);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountingBookDTO getBankBook(LocalDate fromDate, LocalDate toDate) {
        return buildBook("Bank", fromDate, toDate);
    }

    private AccountingBookDTO buildBook(String accountName, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new com.sme.erp.common.exception.BadRequestException("fromDate cannot be after toDate");
        }
        Account account = accountRepository.findByAccountNameIgnoreCase(accountName)
                .orElseThrow(() -> new ResourceNotFoundException(accountName + " account not found"));
        BigDecimal openingBalance = fromDate == null ? BigDecimal.ZERO : safe(lineRepository.openingBalance(account.getId(), fromDate));
        List<BookEntryDTO> rows = new ArrayList<>();
        BigDecimal balance = openingBalance;
        for (JournalEntryLine line : lineRepository.findBookLines(account.getId(), fromDate, toDate)) {
            BigDecimal debit = safe(line.getDebit());
            BigDecimal credit = safe(line.getCredit());
            balance = balance.add(debit).subtract(credit);
            String referenceType = line.getJournalEntry().getReferenceType() != null
                    ? line.getJournalEntry().getReferenceType() : line.getJournalEntry().getSourceType();
            String referenceNo = line.getJournalEntry().getReferenceNo() != null
                    ? line.getJournalEntry().getReferenceNo() : line.getJournalEntry().getSourceNo();
            rows.add(new BookEntryDTO(line.getJournalEntry().getJournalDate(), line.getJournalEntry().getJournalNo(),
                    referenceType, referenceNo, line.getDescription() != null ? line.getDescription() : line.getJournalEntry().getDescription(),
                    debit, credit, balance));
        }
        return new AccountingBookDTO(openingBalance, balance, rows);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}
