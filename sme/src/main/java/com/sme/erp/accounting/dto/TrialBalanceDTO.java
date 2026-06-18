package com.sme.erp.accounting.dto;

import java.math.BigDecimal;
import java.util.List;

public class TrialBalanceDTO {
    private List<TrialBalanceRowDTO> rows;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;

    public TrialBalanceDTO(List<TrialBalanceRowDTO> rows, BigDecimal totalDebit, BigDecimal totalCredit) {
        this.rows = rows;
        this.totalDebit = totalDebit;
        this.totalCredit = totalCredit;
    }

    public List<TrialBalanceRowDTO> getRows() { return rows; }
    public BigDecimal getTotalDebit() { return totalDebit; }
    public BigDecimal getTotalCredit() { return totalCredit; }
    public BigDecimal getDifference() { return totalDebit.subtract(totalCredit); }
    public boolean isBalanced() { return getDifference().compareTo(BigDecimal.ZERO) == 0; }
    public boolean isOutOfBalance() { return !isBalanced(); }
    public BigDecimal getDifferenceAmount() { return getDifference(); }
}
