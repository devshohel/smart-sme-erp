package com.sme.erp.dashboard.service;

import com.sme.erp.dashboard.dto.DashboardSummaryDTO;
import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    DashboardSummaryDTO getSummary();
    DashboardSummaryDTO getSummary(String period, LocalDate fromDate, LocalDate toDate);
    List<DashboardSummaryDTO.TopSellingProductDTO> getTopSellingProducts(String period, LocalDate fromDate, LocalDate toDate);
    List<DashboardSummaryDTO.CustomerAnalyticsDTO> getTopCustomers(String period, LocalDate fromDate, LocalDate toDate);
    List<DashboardSummaryDTO.SupplierAnalyticsDTO> getTopSuppliers(String period, LocalDate fromDate, LocalDate toDate);
    List<DashboardSummaryDTO.MonthlySalesPurchaseDTO> getMonthlySalesTrend(String period, LocalDate fromDate, LocalDate toDate);
    List<DashboardSummaryDTO.MonthlySalesPurchaseDTO> getMonthlyPurchaseTrend(String period, LocalDate fromDate, LocalDate toDate);
    List<DashboardSummaryDTO.ChartPointDTO> getMonthlyExpenseTrend(String period, LocalDate fromDate, LocalDate toDate);
}
