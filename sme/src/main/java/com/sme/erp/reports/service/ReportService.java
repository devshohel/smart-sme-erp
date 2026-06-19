package com.sme.erp.reports.service;

import com.sme.erp.reports.dto.CustomerDueReportDTO;
import com.sme.erp.reports.dto.CustomerSalesReportDTO;
import com.sme.erp.reports.dto.LowStockReportDTO;
import com.sme.erp.reports.dto.ProfitLossSummaryDTO;
import com.sme.erp.reports.dto.PurchaseReturnReportDTO;
import com.sme.erp.reports.dto.PurchaseReportDTO;
import com.sme.erp.reports.dto.SalesReportDTO;
import com.sme.erp.reports.dto.StockTransferReportDTO;
import com.sme.erp.reports.dto.StockReportDTO;
import com.sme.erp.reports.dto.SupplierDueReportDTO;
import com.sme.erp.reports.dto.SupplierPurchaseReportDTO;
import com.sme.erp.reports.dto.TopSellingProductReportDTO;
import com.sme.erp.reports.dto.WarehouseStockValuationReportDTO;

import java.time.LocalDate;

public interface ReportService {
    SalesReportDTO getSalesReport(LocalDate startDate, LocalDate endDate, Long customerId, Long productId);
    SalesReportDTO getSalesSummary(LocalDate fromDate, LocalDate toDate, Long customerId);
    SalesReportDTO getSalesDetail(LocalDate fromDate, LocalDate toDate, Long customerId, Long productId, Long warehouseId, String keyword);
    TopSellingProductReportDTO getTopSellingProducts(LocalDate fromDate, LocalDate toDate, Long productId,
                                                     Long warehouseId, Long categoryId, Long brandId, String keyword);
    CustomerSalesReportDTO getCustomerSales(LocalDate fromDate, LocalDate toDate, Long customerId, String keyword);
    PurchaseReportDTO getPurchaseReport(LocalDate startDate, LocalDate endDate, Long supplierId);
    PurchaseReportDTO getPurchaseSummary(LocalDate fromDate, LocalDate toDate, Long supplierId);
    PurchaseReportDTO getPurchaseDetail(LocalDate fromDate, LocalDate toDate, Long supplierId, Long warehouseId, String keyword);
    SupplierPurchaseReportDTO getSupplierPurchases(LocalDate fromDate, LocalDate toDate, Long supplierId, String keyword);
    PurchaseReturnReportDTO getPurchaseReturns(LocalDate fromDate, LocalDate toDate, Long supplierId, String keyword);
    StockReportDTO getStockReport(Long warehouseId, Long productId);
    StockReportDTO getStockReport(Long warehouseId, Long productId, Long categoryId, Long brandId, String keyword);
    StockReportDTO getStockMovements(LocalDate fromDate, LocalDate toDate, Long warehouseId, Long productId, String keyword);
    LowStockReportDTO getLowStockReport(Long warehouseId, Long productId, Long categoryId, Long brandId, String keyword);
    WarehouseStockValuationReportDTO getWarehouseStockValuation(Long warehouseId, Long categoryId, Long brandId, String keyword);
    StockTransferReportDTO getStockTransfers(LocalDate fromDate, LocalDate toDate, Long warehouseId, String status, String keyword);
    CustomerDueReportDTO getCustomerDueReport();
    SupplierDueReportDTO getSupplierDueReport();
    ProfitLossSummaryDTO getProfitLossSummary(LocalDate startDate, LocalDate endDate);
}
