package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.dto.BookEntryDTO;
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
    public List<BookEntryDTO> getCashBook() {
        return buildBook("Cash", AccountingPaymentMethod.CASH, "Cash");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookEntryDTO> getBankBook() {
        return buildBook("Bank", AccountingPaymentMethod.BANK, "Bank");
    }

    private List<BookEntryDTO> buildBook(String accountName, AccountingPaymentMethod method, String label) {
        Account account = accountRepository.findByAccountNameIgnoreCase(accountName)
                .orElseThrow(() -> new ResourceNotFoundException(accountName + " account not found"));
        List<BookSource> sources = new ArrayList<>();

        for (JournalEntryLine line : lineRepository.findBookLines(account.getId(), JournalStatus.POSTED)) {
            sources.add(new BookSource(line.getJournalEntry().getJournalDate(), line.getJournalEntry().getJournalNo(),
                    label + " journal: " + nullToEmpty(line.getDescription()),
                    safe(line.getDebit()), safe(line.getCredit())));
        }

        sources.sort(Comparator.comparing(BookSource::date).thenComparing(BookSource::reference));
        List<BookEntryDTO> rows = new ArrayList<>();
        BigDecimal balance = BigDecimal.ZERO;
        for (BookSource source : sources) {
            balance = balance.add(source.moneyIn()).subtract(source.moneyOut());
            rows.add(new BookEntryDTO(source.date(), source.reference(), source.description(), source.moneyIn(), source.moneyOut(), balance));
        }
        return rows;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record BookSource(LocalDate date, String reference, String description, BigDecimal moneyIn, BigDecimal moneyOut) {}
}
