package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.dto.ExpenseDTO;
import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.accounting.entity.ExpenseCategory;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;
import com.sme.erp.accounting.mapper.AccountingMapper;
import com.sme.erp.accounting.repository.ExpenseCategoryRepository;
import com.sme.erp.accounting.repository.ExpenseRepository;
import com.sme.erp.accounting.service.AccountingPostingService;
import com.sme.erp.accounting.service.ExpenseService;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.enums.Status;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseRepository repository;
    private final ExpenseCategoryRepository categoryRepository;
    private final AccountingMapper mapper;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private final AccountingPostingService accountingPostingService;

    public ExpenseServiceImpl(ExpenseRepository repository, ExpenseCategoryRepository categoryRepository, AccountingMapper mapper, ActivityLogService activityLogService, AuditLogService auditLogService, AccountingPostingService accountingPostingService) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.accountingPostingService = accountingPostingService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDTO> getAll(LocalDate fromDate, LocalDate toDate, Long categoryId, AccountingPaymentMethod paymentMethod) {
        return repository.search(fromDate, toDate, categoryId, paymentMethod).stream().map(mapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO getById(Long id) {
        return mapper.toDTO(find(id));
    }

    @Override
    @Transactional
    public ExpenseDTO create(ExpenseDTO dto) {
        Expense expense = new Expense();
        expense.setExpenseNo(nextExpenseNo());
        expense.setCreatedBy(currentUsername());
        apply(dto, expense);
        Expense savedEntity = repository.save(expense);
        accountingPostingService.postExpense(savedEntity);
        ExpenseDTO saved = mapper.toDTO(savedEntity);
        activityLogService.log("EXPENSE_CREATE", "ACCOUNTING", "accounting_expenses", saved.getId(), "Created expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO update(Long id, ExpenseDTO dto) {
        Expense expense = find(id);
        if (expense.getStatus() == ExpenseStatus.CANCELLED) {
            throw new BadRequestException("Cancelled expense cannot be edited");
        }
        ExpenseDTO oldData = mapper.toDTO(expense);
        apply(dto, expense);
        ExpenseDTO saved = mapper.toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_UPDATE", "ACCOUNTING", "accounting_expenses", saved.getId(), "Updated expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO cancel(Long id) {
        Expense expense = find(id);
        ExpenseDTO oldData = mapper.toDTO(expense);
        expense.setStatus(ExpenseStatus.CANCELLED);
        ExpenseDTO saved = mapper.toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_CANCEL", "ACCOUNTING", "accounting_expenses", saved.getId(), "Cancelled expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "CANCEL");
        return saved;
    }

    private void apply(ExpenseDTO dto, Expense expense) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        ExpenseCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense category not found with id: " + dto.getCategoryId()));
        if (category.getStatus() != Status.ACTIVE) {
            throw new BadRequestException("Expense category must be active");
        }
        expense.setExpenseDate(dto.getExpenseDate() != null ? dto.getExpenseDate() : LocalDate.now());
        expense.setCategory(category);
        expense.setAmount(dto.getAmount());
        expense.setPaymentMethod(dto.getPaymentMethod());
        expense.setReferenceNo(RequestValueUtils.normalize(dto.getReferenceNo()));
        expense.setNotes(RequestValueUtils.normalize(dto.getNotes()));
        expense.setStatus(dto.getStatus() != null ? dto.getStatus() : ExpenseStatus.ACTIVE);
    }

    private String nextExpenseNo() {
        long next = repository.findMaxId() + 1;
        String value = String.format("EXP-%04d", next);
        while (repository.existsByExpenseNo(value)) {
            next++;
            value = String.format("EXP-%04d", next);
        }
        return value;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    private Expense find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }
}
