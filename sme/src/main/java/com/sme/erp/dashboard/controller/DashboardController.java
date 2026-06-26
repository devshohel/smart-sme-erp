package com.sme.erp.dashboard.controller;

import com.sme.erp.dashboard.dto.DashboardSummaryDTO;
import com.sme.erp.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<DashboardSummaryDTO> getSummary(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(dashboardService.getSummary(period, fromDate, toDate));
    }

    @GetMapping("/analytics/top-selling-products")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW') or hasAuthority('REPORT_VIEW')")
    public ResponseEntity<List<DashboardSummaryDTO.TopSellingProductDTO>> topSellingProducts(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(dashboardService.getTopSellingProducts(period, fromDate, toDate));
    }

    @GetMapping("/analytics/top-customers")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW') or hasAuthority('REPORT_VIEW')")
    public ResponseEntity<List<DashboardSummaryDTO.CustomerAnalyticsDTO>> topCustomers(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(dashboardService.getTopCustomers(period, fromDate, toDate));
    }

    @GetMapping("/analytics/top-suppliers")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW') or hasAuthority('REPORT_VIEW')")
    public ResponseEntity<List<DashboardSummaryDTO.SupplierAnalyticsDTO>> topSuppliers(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(dashboardService.getTopSuppliers(period, fromDate, toDate));
    }

    @GetMapping("/analytics/monthly-sales-trend")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW') or hasAuthority('REPORT_VIEW')")
    public ResponseEntity<List<DashboardSummaryDTO.MonthlySalesPurchaseDTO>> monthlySalesTrend(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(dashboardService.getMonthlySalesTrend(period, fromDate, toDate));
    }

    @GetMapping("/analytics/monthly-purchase-trend")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW') or hasAuthority('REPORT_VIEW')")
    public ResponseEntity<List<DashboardSummaryDTO.MonthlySalesPurchaseDTO>> monthlyPurchaseTrend(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(dashboardService.getMonthlyPurchaseTrend(period, fromDate, toDate));
    }

    @GetMapping("/analytics/monthly-expense-trend")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW') or hasAuthority('REPORT_VIEW')")
    public ResponseEntity<List<DashboardSummaryDTO.ChartPointDTO>> monthlyExpenseTrend(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(dashboardService.getMonthlyExpenseTrend(period, fromDate, toDate));
    }
}
