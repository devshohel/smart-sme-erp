package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.dto.AccountDTO;
import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.mapper.AccountingMapper;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.service.AccountService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.enums.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repository;
    private final AccountingMapper mapper;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;

    public AccountServiceImpl(AccountRepository repository, AccountingMapper mapper, ActivityLogService activityLogService, AuditLogService auditLogService) {
        this.repository = repository;
        this.mapper = mapper;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> getAll(AccountType type, Status status) {
        return repository.search(type, status).stream().map(mapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO getById(Long id) {
        return mapper.toDTO(find(id));
    }

    @Override
    @Transactional
    public AccountDTO create(AccountDTO dto) {
        Account account = new Account();
        apply(dto, account, null);
        AccountDTO saved = mapper.toDTO(repository.save(account));
        activityLogService.log("ACCOUNT_CREATE", "ACCOUNTING", "accounting_accounts", saved.getId(), "Created account " + saved.getAccountName());
        auditLogService.log("accounting_accounts", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public AccountDTO update(Long id, AccountDTO dto) {
        Account account = find(id);
        AccountDTO oldData = mapper.toDTO(account);
        apply(dto, account, id);
        AccountDTO saved = mapper.toDTO(repository.save(account));
        activityLogService.log("ACCOUNT_UPDATE", "ACCOUNTING", "accounting_accounts", saved.getId(), "Updated account " + saved.getAccountName());
        auditLogService.log("accounting_accounts", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    private void apply(AccountDTO dto, Account account, Long currentId) {
        String code = RequestValueUtils.normalizeRequired(dto.getAccountCode(), "Account code");
        String name = RequestValueUtils.normalizeRequired(dto.getAccountName(), "Account name");
        if (currentId == null ? repository.existsByAccountCodeIgnoreCase(code) : repository.existsByAccountCodeIgnoreCaseAndIdNot(code, currentId)) {
            throw new DuplicateResourceException("Account code already exists: " + code);
        }
        if (dto.getParentAccountId() != null && dto.getParentAccountId().equals(currentId)) {
            throw new BadRequestException("Parent account cannot be the same account");
        }
        account.setAccountCode(code);
        account.setAccountName(name);
        account.setAccountType(dto.getAccountType());
        account.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        account.setParentAccount(dto.getParentAccountId() == null ? null : find(dto.getParentAccountId()));
    }

    private Account find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }
}
