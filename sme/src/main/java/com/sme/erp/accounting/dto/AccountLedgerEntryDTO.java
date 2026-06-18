package com.sme.erp.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountLedgerEntryDTO(LocalDate date, String journalNo, String referenceType,
                                    String referenceNo, String description, BigDecimal debit,
                                    BigDecimal credit, BigDecimal runningBalance) {}
