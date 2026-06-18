package com.sme.erp.accounting.dto;

import com.sme.erp.accounting.enums.AccountType;
import java.math.BigDecimal;
import java.util.List;

public record AccountLedgerDTO(Long accountId, String accountCode, String accountName,
                               AccountType accountType, BigDecimal openingBalance,
                               BigDecimal closingBalance, List<AccountLedgerEntryDTO> transactions) {}
