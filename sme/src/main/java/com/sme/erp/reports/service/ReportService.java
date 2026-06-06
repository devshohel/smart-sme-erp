package com.sme.erp.reports.service;

import com.sme.erp.reports.dto.CustomerDueReportDTO;
import com.sme.erp.reports.dto.ProfitLossSummaryDTO;
import com.sme.erp.reports.dto.PurchaseReportDTO;
import com.sme.erp.reports.dto.SalesReportDTO;
import com.sme.erp.reports.dto.StockReportDTO;
import com.sme.erp.reports.dto.SupplierDueReportDTO;

import java.time.LocalDate;

public interface ReportService {
    SalesReportDTO getSalesReport(LocalDate startDate, LocalDate endDate, Long customerId, Long productId);
    PurchaseReportDTO getPurchaseReport(LocalDate startDate, LocalDate endDate, Long supplierId);
    StockReportDTO getStockReport(Long warehouseId, Long productId);
    CustomerDueReportDTO getCustomerDueReport();
    SupplierDueReportDTO getSupplierDueReport();
    ProfitLossSummaryDTO getProfitLossSummary(LocalDate startDate, LocalDate endDate);
}
