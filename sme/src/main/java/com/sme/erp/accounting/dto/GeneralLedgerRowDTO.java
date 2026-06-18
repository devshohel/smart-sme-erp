package com.sme.erp.accounting.dto;

import com.sme.erp.accounting.enums.AccountType;
import java.math.BigDecimal;

public record GeneralLedgerRowDTO(Long accountId, String accountCode, String accountName,
                                  AccountType accountType, BigDecimal openingBalance,
                                  BigDecimal totalDebit, BigDecimal totalCredit,
                                  BigDecimal closingBalance) {}
