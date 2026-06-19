package com.sme.erp.dashboard.service;

import com.sme.erp.dashboard.dto.DashboardSummaryDTO;
import java.time.LocalDate;

public interface DashboardService {
    DashboardSummaryDTO getSummary();
    DashboardSummaryDTO getSummary(String period, LocalDate fromDate, LocalDate toDate);
}
