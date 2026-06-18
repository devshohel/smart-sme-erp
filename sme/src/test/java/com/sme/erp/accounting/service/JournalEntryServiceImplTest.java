package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.JournalEntryDTO;
import com.sme.erp.accounting.dto.JournalEntryLineDTO;
import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.entity.CostCenter;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.enums.JournalStatus;
import com.sme.erp.accounting.mapper.AccountingMapper;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
import com.sme.erp.accounting.repository.CostCenterRepository;
import com.sme.erp.accounting.service.impl.JournalEntryServiceImpl;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JournalEntryServiceImplTest {
    @Mock JournalEntryRepository repository;
    @Mock AccountRepository accountRepository;
    @Mock ActivityLogService activityLogService;
    @Mock AuditLogService auditLogService;
    @Mock CostCenterRepository costCenterRepository;
    private JournalEntryServiceImpl service;

    @BeforeEach void setUp() {
        service = new JournalEntryServiceImpl(repository, accountRepository, new AccountingMapper(), activityLogService, auditLogService);
        service.setCostCenterRepository(costCenterRepository);
        lenient().when(repository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test void balancedJournalCanPost() {
        JournalEntry entry = entry(JournalStatus.DRAFT, "100.00", "100.00");
        when(repository.findWithLinesById(1L)).thenReturn(Optional.of(entry));
        assertThat(service.post(1L).getStatus()).isEqualTo(JournalStatus.POSTED);
    }

    @Test void unbalancedJournalCannotPost() {
        JournalEntry entry = entry(JournalStatus.DRAFT, "100.00", "90.00");
        when(repository.findWithLinesById(1L)).thenReturn(Optional.of(entry));
        assertThatThrownBy(() -> service.post(1L)).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Total debit must equal total credit");
    }

    @Test void postedJournalCannotEdit() {
        when(repository.findWithLinesById(1L)).thenReturn(Optional.of(entry(JournalStatus.POSTED, "100.00", "100.00")));
        assertThatThrownBy(() -> service.update(1L, dto())).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only draft");
    }

    @Test void duplicatePostIsBlocked() {
        when(repository.findWithLinesById(1L)).thenReturn(Optional.of(entry(JournalStatus.POSTED, "100.00", "100.00")));
        assertThatThrownBy(() -> service.post(1L)).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only draft");
    }

    @Test void manualJournalLineSupportsOptionalCostCenter() {
        CostCenter center = new CostCenter(); center.setId(7L); center.setCode("OPS"); center.setName("Operations"); center.setStatus(Status.ACTIVE);
        when(repository.findMaxId()).thenReturn(0L); when(repository.existsByJournalNo("JRN-0001")).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1L,"1000","Cash")));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(account(2L,"3000","Equity")));
        when(costCenterRepository.findById(7L)).thenReturn(Optional.of(center));
        JournalEntryDTO request=dto(); request.getLines().get(0).setCostCenterId(7L);
        JournalEntryDTO saved=service.create(request);
        assertThat(saved.getLines().get(0).getCostCenterCode()).isEqualTo("OPS");
        assertThat(saved.getLines().get(1).getCostCenterId()).isNull();
    }

    private JournalEntry entry(JournalStatus status, String debit, String credit) {
        Account cash = account(1L, "1000", "Cash");
        Account equity = account(2L, "3000", "Owner Equity");
        JournalEntry entry = new JournalEntry();
        entry.setId(1L); entry.setJournalNo("JRN-0001"); entry.setJournalDate(LocalDate.now()); entry.setStatus(status);
        entry.addLine(line(cash, debit, "0"));
        entry.addLine(line(equity, "0", credit));
        return entry;
    }

    private JournalEntryLine line(Account account, String debit, String credit) {
        JournalEntryLine line = new JournalEntryLine(); line.setAccount(account);
        line.setDebit(new BigDecimal(debit)); line.setCredit(new BigDecimal(credit)); return line;
    }

    private Account account(Long id, String code, String name) {
        Account account = new Account(); account.setId(id); account.setAccountCode(code); account.setAccountName(name);
        account.setAccountType(AccountType.ASSET); account.setStatus(Status.ACTIVE); return account;
    }

    private JournalEntryDTO dto() {
        JournalEntryDTO dto = new JournalEntryDTO(); dto.setJournalDate(LocalDate.now());
        JournalEntryLineDTO debit = new JournalEntryLineDTO(); debit.setAccountId(1L); debit.setDebit(new BigDecimal("100")); debit.setCredit(BigDecimal.ZERO);
        JournalEntryLineDTO credit = new JournalEntryLineDTO(); credit.setAccountId(2L); credit.setDebit(BigDecimal.ZERO); credit.setCredit(new BigDecimal("100"));
        dto.setLines(List.of(debit, credit)); return dto;
    }
}
