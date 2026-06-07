package com.sme.erp.accounting.mapper;

import com.sme.erp.accounting.dto.*;
import com.sme.erp.accounting.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AccountingMapper {
    public ExpenseCategoryDTO toDTO(ExpenseCategory entity) {
        if (entity == null) return null;
        ExpenseCategoryDTO dto = new ExpenseCategoryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public AccountDTO toDTO(Account entity) {
        if (entity == null) return null;
        AccountDTO dto = new AccountDTO();
        dto.setId(entity.getId());
        dto.setAccountCode(entity.getAccountCode());
        dto.setAccountName(entity.getAccountName());
        dto.setAccountType(entity.getAccountType());
        dto.setStatus(entity.getStatus());
        if (entity.getParentAccount() != null) {
            dto.setParentAccountId(entity.getParentAccount().getId());
            dto.setParentAccountName(entity.getParentAccount().getAccountName());
        }
        return dto;
    }

    public ExpenseDTO toDTO(Expense entity) {
        if (entity == null) return null;
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(entity.getId());
        dto.setExpenseNo(entity.getExpenseNo());
        dto.setExpenseDate(entity.getExpenseDate());
        dto.setAmount(entity.getAmount());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setReferenceNo(entity.getReferenceNo());
        dto.setNotes(entity.getNotes());
        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
            dto.setCategoryName(entity.getCategory().getName());
        }
        return dto;
    }

    public JournalEntryDTO toDTO(JournalEntry entity) {
        if (entity == null) return null;
        JournalEntryDTO dto = new JournalEntryDTO();
        dto.setId(entity.getId());
        dto.setJournalNo(entity.getJournalNo());
        dto.setJournalDate(entity.getJournalDate());
        dto.setReferenceNo(entity.getReferenceNo());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setLines(entity.getLines().stream().map(this::toDTO).collect(Collectors.toList()));
        return dto;
    }

    public JournalEntryLineDTO toDTO(JournalEntryLine entity) {
        JournalEntryLineDTO dto = new JournalEntryLineDTO();
        dto.setId(entity.getId());
        dto.setDebit(entity.getDebit());
        dto.setCredit(entity.getCredit());
        dto.setDescription(entity.getDescription());
        if (entity.getAccount() != null) {
            dto.setAccountId(entity.getAccount().getId());
            dto.setAccountCode(entity.getAccount().getAccountCode());
            dto.setAccountName(entity.getAccount().getAccountName());
        }
        return dto;
    }
}
