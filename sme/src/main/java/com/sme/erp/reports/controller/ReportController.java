package com.sme.erp.reports.controller;

import com.sme.erp.reports.dto.CustomerDueReportDTO;
import com.sme.erp.reports.dto.ProfitLossSummaryDTO;
import com.sme.erp.reports.dto.PurchaseReportDTO;
import com.sme.erp.reports.dto.SalesReportDTO;
import com.sme.erp.reports.dto.StockReportDTO;
import com.sme.erp.reports.dto.SupplierDueReportDTO;
import com.sme.erp.reports.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    public ResponseEntity<SalesReportDTO> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(reportService.getSalesReport(startDate, endDate, customerId, productId));
    }

    @GetMapping("/purchases")
    public ResponseEntity<PurchaseReportDTO> getPurchaseReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long supplierId) {
        return ResponseEntity.ok(reportService.getPurchaseReport(startDate, endDate, supplierId));
    }

    @GetMapping("/stock")
    public ResponseEntity<StockReportDTO> getStockReport(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(reportService.getStockReport(warehouseId, productId));
    }

    @GetMapping("/customer-dues")
    public ResponseEntity<CustomerDueReportDTO> getCustomerDueReport() {
        return ResponseEntity.ok(reportService.getCustomerDueReport());
    }

    @GetMapping("/supplier-dues")
    public ResponseEntity<SupplierDueReportDTO> getSupplierDueReport() {
        return ResponseEntity.ok(reportService.getSupplierDueReport());
    }

    @GetMapping("/profit-loss")
    public ResponseEntity<ProfitLossSummaryDTO> getProfitLossSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getProfitLossSummary(startDate, endDate));
    }
}
