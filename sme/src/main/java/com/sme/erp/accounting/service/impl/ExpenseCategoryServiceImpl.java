package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.dto.ExpenseCategoryDTO;
import com.sme.erp.accounting.entity.ExpenseCategory;
import com.sme.erp.accounting.mapper.AccountingMapper;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.ExpenseCategoryRepository;
import com.sme.erp.accounting.service.ExpenseCategoryService;
import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.enums.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpenseCategoryServiceImpl implements ExpenseCategoryService {
    private final ExpenseCategoryRepository repository;
    private final AccountRepository accountRepository;
    private final AccountingMapper mapper;

    public ExpenseCategoryServiceImpl(ExpenseCategoryRepository repository, AccountRepository accountRepository, AccountingMapper mapper) {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseCategoryDTO> getAll(Status status) {
        return repository.search(status).stream().map(mapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseCategoryDTO getById(Long id) {
        return mapper.toDTO(find(id));
    }

    @Override
    @Transactional
    public ExpenseCategoryDTO create(ExpenseCategoryDTO dto) {
        String name = RequestValueUtils.normalizeRequired(dto.getName(), "Category name");
        if (repository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Expense category already exists: " + name);
        }
        ExpenseCategory category = new ExpenseCategory();
        category.setName(name);
        category.setDescription(RequestValueUtils.normalize(dto.getDescription()));
        category.setAccount(resolveAccount(dto.getAccountId()));
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        return mapper.toDTO(repository.save(category));
    }

    @Override
    @Transactional
    public ExpenseCategoryDTO update(Long id, ExpenseCategoryDTO dto) {
        ExpenseCategory category = find(id);
        String name = RequestValueUtils.normalizeRequired(dto.getName(), "Category name");
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Expense category already exists: " + name);
        }
        category.setName(name);
        category.setDescription(RequestValueUtils.normalize(dto.getDescription()));
        category.setAccount(resolveAccount(dto.getAccountId()));
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        return mapper.toDTO(repository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ExpenseCategory category = find(id);
        category.setStatus(Status.INACTIVE);
        repository.save(category);
    }

    private ExpenseCategory find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense category not found with id: " + id));
    }

    private Account resolveAccount(Long accountId) {
        if (accountId == null) {
            return null;
        }
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }
}
