package com.sme.erp.reports.dto;

import java.math.BigDecimal;
import java.util.List;

public class CustomerDueReportDTO {
    private BigDecimal totalCustomerDue;
    private Long totalCustomersWithDue;
    private List<CustomerDueReportRowDTO> rows;

    public CustomerDueReportDTO(BigDecimal totalCustomerDue, Long totalCustomersWithDue,
                                List<CustomerDueReportRowDTO> rows) {
        this.totalCustomerDue = totalCustomerDue;
        this.totalCustomersWithDue = totalCustomersWithDue;
        this.rows = rows;
    }

    public BigDecimal getTotalCustomerDue() { return totalCustomerDue; }
    public Long getTotalCustomersWithDue() { return totalCustomersWithDue; }
    public List<CustomerDueReportRowDTO> getRows() { return rows; }
}
