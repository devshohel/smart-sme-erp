package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.dto.JournalEntryDTO;
import com.sme.erp.accounting.dto.JournalEntryLineDTO;
import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.JournalStatus;
import com.sme.erp.accounting.mapper.AccountingMapper;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
import com.sme.erp.accounting.repository.CostCenterRepository;
import com.sme.erp.accounting.service.JournalEntryService;
import com.sme.erp.accounting.service.AccountingPeriodService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.enums.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;

@Service
public class JournalEntryServiceImpl implements JournalEntryService {
    private final JournalEntryRepository repository;
    private final AccountRepository accountRepository;
    private final AccountingMapper mapper;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private CostCenterRepository costCenterRepository;
    private AccountingPeriodService periodService;

    public JournalEntryServiceImpl(JournalEntryRepository repository, AccountRepository accountRepository, AccountingMapper mapper, ActivityLogService activityLogService, AuditLogService auditLogService) {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.mapper = mapper;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JournalEntryDTO> getAll(JournalStatus status) {
        return repository.search(status).stream().map(mapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JournalEntryDTO getById(Long id) {
        return mapper.toDTO(findWithLines(id));
    }

    @Override
    @Transactional
    public JournalEntryDTO create(JournalEntryDTO dto) {
        validateLines(dto.getLines(), false);
        JournalEntry entry = new JournalEntry();
        entry.setJournalNo(nextJournalNo());
        entry.setJournalDate(dto.getJournalDate() != null ? dto.getJournalDate() : LocalDate.now());
        entry.setReferenceNo(RequestValueUtils.normalize(dto.getReferenceNo()));
        entry.setReferenceType(RequestValueUtils.normalize(dto.getReferenceType()));
        entry.setDescription(RequestValueUtils.normalize(dto.getDescription()));
        entry.setCreatedBy(currentUsername());
        entry.setStatus(JournalStatus.DRAFT);
        dto.getLines().forEach(lineDto -> entry.addLine(toLine(lineDto)));
        updateTotals(entry);
        JournalEntryDTO saved = mapper.toDTO(repository.save(entry));
        activityLogService.log("JOURNAL_CREATE", "ACCOUNTING", "accounting_journal_entries", saved.getId(), "Created journal " + saved.getJournalNo());
        auditLogService.log("accounting_journal_entries", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public void setCostCenterRepository(CostCenterRepository repository) { this.costCenterRepository = repository; }
    @org.springframework.beans.factory.annotation.Autowired
    public void setPeriodService(AccountingPeriodService service) { this.periodService = service; }

    @Override
    @Transactional
    public JournalEntryDTO update(Long id, JournalEntryDTO dto) {
        JournalEntry entry = findWithLines(id);
        if (entry.getStatus() != JournalStatus.DRAFT) {
            throw new BadRequestException("Only draft journal entries can be edited");
        }
        if (entry.getSourceType() != null) {
            throw new BadRequestException("Auto-generated journal entries cannot be edited manually");
        }
        validateLines(dto.getLines(), false);
        JournalEntryDTO oldData = mapper.toDTO(entry);
        entry.setJournalDate(dto.getJournalDate());
        entry.setReferenceNo(RequestValueUtils.normalize(dto.getReferenceNo()));
        entry.setReferenceType(RequestValueUtils.normalize(dto.getReferenceType()));
        entry.setDescription(RequestValueUtils.normalize(dto.getDescription()));
        entry.getLines().clear();
        dto.getLines().forEach(lineDto -> entry.addLine(toLine(lineDto)));
        updateTotals(entry);
        JournalEntryDTO saved = mapper.toDTO(repository.save(entry));
        activityLogService.log("JOURNAL_UPDATE", "ACCOUNTING", "accounting_journal_entries", id, "Updated journal " + saved.getJournalNo());
        auditLogService.log("accounting_journal_entries", id, auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public JournalEntryDTO post(Long id) {
        JournalEntry entry = findWithLines(id);
        if (periodService != null) periodService.assertOpen(entry.getJournalDate());
        if (entry.getStatus() != JournalStatus.DRAFT) {
            throw new BadRequestException("Only draft journal entries can be posted");
        }
        validateLines(entry.getLines().stream().map(mapper::toDTO).toList(), true);
        JournalEntryDTO oldData = mapper.toDTO(entry);
        entry.setStatus(JournalStatus.POSTED);
        entry.setPostedAt(LocalDateTime.now());
        entry.setPostedBy(currentUsername());
        updateTotals(entry);
        JournalEntryDTO saved = mapper.toDTO(repository.save(entry));
        activityLogService.log("JOURNAL_POST", "ACCOUNTING", "accounting_journal_entries", saved.getId(), "Posted journal " + saved.getJournalNo());
        auditLogService.log("accounting_journal_entries", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "POST");
        return saved;
    }

    @Override
    @Transactional
    public JournalEntryDTO cancel(Long id) {
        JournalEntry entry = findWithLines(id);
        if (periodService != null) periodService.assertOpen(entry.getJournalDate());
        if (entry.getStatus() == JournalStatus.CANCELLED) {
            throw new BadRequestException("Journal entry is already cancelled");
        }
        if (entry.getStatus() == JournalStatus.POSTED) {
            throw new BadRequestException("Posted journal cancellation requires a reversal journal and is not supported in Accounting Batch-1");
        }
        JournalEntryDTO oldData = mapper.toDTO(entry);
        entry.setStatus(JournalStatus.CANCELLED);
        entry.setCancelledAt(LocalDateTime.now());
        entry.setCancelledBy(currentUsername());
        JournalEntryDTO saved = mapper.toDTO(repository.save(entry));
        activityLogService.log("JOURNAL_CANCEL", "ACCOUNTING", "accounting_journal_entries", saved.getId(), "Cancelled journal " + saved.getJournalNo());
        auditLogService.log("accounting_journal_entries", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "CANCEL");
        return saved;
    }

    private JournalEntryLine toLine(JournalEntryLineDTO dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + dto.getAccountId()));
        if (account.getStatus() != Status.ACTIVE) {
            throw new BadRequestException("Journal line account must be active");
        }
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account);
        if (dto.getCostCenterId() != null) {
            if (costCenterRepository == null) throw new BadRequestException("Cost center service is unavailable");
            line.setCostCenter(costCenterRepository.findById(dto.getCostCenterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cost center not found with id: " + dto.getCostCenterId())));
        }
        line.setDebit(safe(dto.getDebit()));
        line.setCredit(safe(dto.getCredit()));
        line.setDescription(RequestValueUtils.normalize(dto.getDescription()));
        return line;
    }

    private void validateLines(List<JournalEntryLineDTO> lines, boolean requireBalanced) {
        if (lines == null || lines.size() < 2) {
            throw new BadRequestException("At least two journal lines are required");
        }
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (JournalEntryLineDTO line : lines) {
            BigDecimal debit = safe(line.getDebit());
            BigDecimal credit = safe(line.getCredit());
            if (debit.compareTo(BigDecimal.ZERO) < 0 || credit.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Debit and credit cannot be negative");
            }
            if (debit.compareTo(BigDecimal.ZERO) > 0 && credit.compareTo(BigDecimal.ZERO) > 0) {
                throw new BadRequestException("Debit and credit cannot both be filled in one line");
            }
            if (debit.compareTo(BigDecimal.ZERO) == 0 && credit.compareTo(BigDecimal.ZERO) == 0) {
                throw new BadRequestException("Each journal line must have debit or credit");
            }
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }
        if (requireBalanced && totalDebit.compareTo(totalCredit) != 0) {
            throw new BadRequestException("Total debit must equal total credit");
        }
    }

    private void updateTotals(JournalEntry entry) {
        entry.setTotalDebit(entry.getLines().stream().map(JournalEntryLine::getDebit).map(this::safe).reduce(BigDecimal.ZERO, BigDecimal::add));
        entry.setTotalCredit(entry.getLines().stream().map(JournalEntryLine::getCredit).map(this::safe).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || !authentication.isAuthenticated() ? null : authentication.getName();
    }

    private String nextJournalNo() {
        long next = repository.findMaxId() + 1;
        String value = String.format("JRN-%04d", next);
        while (repository.existsByJournalNo(value)) {
            next++;
            value = String.format("JRN-%04d", next);
        }
        return value;
    }

    private JournalEntry findWithLines(Long id) {
        return repository.findWithLinesById(id).orElseThrow(() -> new ResourceNotFoundException("Journal entry not found with id: " + id));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
