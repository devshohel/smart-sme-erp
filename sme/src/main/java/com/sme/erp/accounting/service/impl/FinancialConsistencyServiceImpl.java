package com.sme.erp.accounting.service.impl;

import com.sme.erp.accounting.dto.FinancialValidationDTO;
import com.sme.erp.accounting.service.FinancialConsistencyService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class FinancialConsistencyServiceImpl implements FinancialConsistencyService {
    @Override
    public FinancialValidationDTO validate(BigDecimal left, BigDecimal right) {
        BigDecimal difference = safe(left).subtract(safe(right));
        return new FinancialValidationDTO(difference.compareTo(BigDecimal.ZERO) != 0, difference);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
