package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.AccountDTO;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.enums.Status;

import java.util.List;

public interface AccountService {
    List<AccountDTO> getAll(AccountType type, Status status);
    AccountDTO getById(Long id);
    AccountDTO create(AccountDTO dto);
    AccountDTO update(Long id, AccountDTO dto);
}
