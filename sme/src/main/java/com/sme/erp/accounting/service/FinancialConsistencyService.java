package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.FinancialValidationDTO;
import java.math.BigDecimal;

public interface FinancialConsistencyService {
    FinancialValidationDTO validate(BigDecimal left, BigDecimal right);
}
