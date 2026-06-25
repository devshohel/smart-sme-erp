package com.sme.erp.reports.controller;

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
import com.sme.erp.reports.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<SalesReportDTO> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(reportService.getSalesReport(startDate, endDate, customerId, productId));
    }

    @GetMapping("/sales-summary")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<SalesReportDTO> getSalesSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long customerId) {
        return ResponseEntity.ok(reportService.getSalesSummary(fromDate, toDate, customerId));
    }

    @GetMapping("/sales-detail")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<SalesReportDTO> getSalesDetail(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getSalesDetail(fromDate, toDate, customerId, productId, warehouseId, keyword));
    }

    @GetMapping("/top-selling-products")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<TopSellingProductReportDTO> getTopSellingProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getTopSellingProducts(fromDate, toDate, productId, warehouseId, categoryId, brandId, keyword));
    }

    @GetMapping("/customer-sales")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<CustomerSalesReportDTO> getCustomerSales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getCustomerSales(fromDate, toDate, customerId, keyword));
    }

    @GetMapping("/purchases")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<PurchaseReportDTO> getPurchaseReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long supplierId) {
        return ResponseEntity.ok(reportService.getPurchaseReport(startDate, endDate, supplierId));
    }

    @GetMapping("/purchase-summary")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<PurchaseReportDTO> getPurchaseSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long supplierId) {
        return ResponseEntity.ok(reportService.getPurchaseSummary(fromDate, toDate, supplierId));
    }

    @GetMapping("/purchase-detail")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<PurchaseReportDTO> getPurchaseDetail(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getPurchaseDetail(fromDate, toDate, supplierId, warehouseId, keyword));
    }

    @GetMapping("/supplier-purchases")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<SupplierPurchaseReportDTO> getSupplierPurchases(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getSupplierPurchases(fromDate, toDate, supplierId, keyword));
    }

    @GetMapping("/purchase-returns")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<PurchaseReturnReportDTO> getPurchaseReturns(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getPurchaseReturns(fromDate, toDate, supplierId, keyword));
    }

    @GetMapping("/stock")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<StockReportDTO> getStockReport(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getStockReport(warehouseId, productId, categoryId, brandId, keyword));
    }

    @GetMapping("/stock-movements")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<StockReportDTO> getStockMovements(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getStockMovements(fromDate, toDate, warehouseId, productId, keyword));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<LowStockReportDTO> getLowStockReport(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getLowStockReport(warehouseId, productId, categoryId, brandId, keyword));
    }

    @GetMapping("/warehouse-stock-valuation")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<WarehouseStockValuationReportDTO> getWarehouseStockValuation(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getWarehouseStockValuation(warehouseId, categoryId, brandId, keyword));
    }

    @GetMapping("/stock-transfers")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<StockTransferReportDTO> getStockTransfers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getStockTransfers(fromDate, toDate, warehouseId, status, keyword));
    }

    @GetMapping("/customer-dues")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<CustomerDueReportDTO> getCustomerDueReport() {
        return ResponseEntity.ok(reportService.getCustomerDueReport());
    }

    @GetMapping("/supplier-dues")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<SupplierDueReportDTO> getSupplierDueReport() {
        return ResponseEntity.ok(reportService.getSupplierDueReport());
    }

    @GetMapping("/profit-loss")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ProfitLossSummaryDTO> getProfitLossSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getProfitLossSummary(startDate, endDate));
    }
}
