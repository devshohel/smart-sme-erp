package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.dto.ExpenseDTO;
import com.sme.erp.accounting.dto.ExpensePageDTO;
import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.accounting.entity.ExpenseCategory;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;
import com.sme.erp.accounting.mapper.AccountingMapper;
import com.sme.erp.accounting.repository.ExpenseCategoryRepository;
import com.sme.erp.accounting.repository.ExpenseRepository;
import com.sme.erp.accounting.repository.JournalEntryRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseRepository repository;
    private final ExpenseCategoryRepository categoryRepository;
    private final AccountingMapper mapper;
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private final AccountingPostingService accountingPostingService;
    private final JournalEntryRepository journalEntryRepository;

    public ExpenseServiceImpl(ExpenseRepository repository, ExpenseCategoryRepository categoryRepository, AccountingMapper mapper, ActivityLogService activityLogService, AuditLogService auditLogService, AccountingPostingService accountingPostingService, JournalEntryRepository journalEntryRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.accountingPostingService = accountingPostingService;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDTO> getAll(LocalDate fromDate, LocalDate toDate, Long categoryId, AccountingPaymentMethod paymentMethod) {
        return repository.search(fromDate, toDate, categoryId, paymentMethod).stream().map(mapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpensePageDTO searchPage(String keyword, LocalDate fromDate, LocalDate toDate, Long categoryId,
                                     AccountingPaymentMethod paymentMethod, ExpenseStatus status,
                                     int page, int size, String sort, String direction) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("From date cannot be after to date.");
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), buildSort(sort, direction));
        Page<Expense> result = repository.searchPage(RequestValueUtils.normalize(keyword), fromDate, toDate,
                categoryId, paymentMethod, status, pageable);
        return new ExpensePageDTO(
                result.getContent().stream().map(mapper::toDTO).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
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
        expense.setStatus(ExpenseStatus.DRAFT);
        Expense savedEntity = repository.save(expense);
        ExpenseDTO saved = mapper.toDTO(savedEntity);
        activityLogService.log("EXPENSE_CREATE", "ACCOUNTING", "accounting_expenses", saved.getId(), "Created expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), null, auditLogService.toJson(saved), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO update(Long id, ExpenseDTO dto) {
        Expense expense = find(id);
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Only draft expenses can be edited");
        }
        ExpenseDTO oldData = mapper.toDTO(expense);
        apply(dto, expense);
        expense.setStatus(ExpenseStatus.DRAFT);
        ExpenseDTO saved = mapper.toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_UPDATE", "ACCOUNTING", "accounting_expenses", saved.getId(), "Updated expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO cancel(Long id) {
        Expense expense = find(id);
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Only draft expenses can be cancelled");
        }
        ExpenseDTO oldData = mapper.toDTO(expense);
        expense.setStatus(ExpenseStatus.CANCELLED);
        ExpenseDTO saved = mapper.toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_CANCEL", "ACCOUNTING", "accounting_expenses", saved.getId(), "Cancelled expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), auditLogService.toJson(oldData), auditLogService.toJson(saved), "CANCEL");
        return saved;
    }

    @Override
    @Transactional
    public ExpenseDTO post(Long id) {
        Expense expense = find(id);
        if (expense.getStatus() == ExpenseStatus.POSTED || journalEntryRepository.existsBySourceTypeAndSourceId("EXPENSE", id)) {
            throw new BadRequestException("Expense is already posted");
        }
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Only draft expenses can be posted");
        }
        if (expense.getCategory() == null || expense.getCategory().getAccount() == null) {
            throw new BadRequestException("Expense category must have a mapped GL account before posting");
        }
        accountingPostingService.postExpense(expense);
        expense.setStatus(ExpenseStatus.POSTED);
        ExpenseDTO saved = mapper.toDTO(repository.save(expense));
        activityLogService.log("EXPENSE_POST", "ACCOUNTING", "accounting_expenses", saved.getId(), "Posted expense " + saved.getExpenseNo());
        auditLogService.log("accounting_expenses", saved.getId(), null, auditLogService.toJson(saved), "POST");
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

    private Sort buildSort(String sort, String direction) {
        Set<String> allowed = Set.of("id", "expenseNo", "expenseDate", "amount", "paymentMethod", "status", "createdAt");
        String property = allowed.contains(sort) ? sort : "expenseDate";
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, property).and(Sort.by(Sort.Direction.DESC, "id"));
    }
}
