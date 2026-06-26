package com.sme.erp.reports.controller;

import com.sme.erp.reports.dto.CustomerDueReportDTO;
import com.sme.erp.reports.dto.CustomerDueReportRowDTO;
import com.sme.erp.reports.dto.CustomerSalesReportDTO;
import com.sme.erp.reports.dto.CustomerSalesRowDTO;
import com.sme.erp.reports.dto.LowStockReportDTO;
import com.sme.erp.reports.dto.LowStockRowDTO;
import com.sme.erp.reports.dto.ProfitLossSummaryDTO;
import com.sme.erp.reports.dto.PurchaseByProductReportDTO;
import com.sme.erp.reports.dto.PurchaseByProductRowDTO;
import com.sme.erp.reports.dto.PurchaseReturnReportDTO;
import com.sme.erp.reports.dto.PurchaseReturnRowDTO;
import com.sme.erp.reports.dto.PurchaseReportDTO;
import com.sme.erp.reports.dto.PurchaseReportRowDTO;
import com.sme.erp.reports.dto.SalesReportDTO;
import com.sme.erp.reports.dto.SalesReportRowDTO;
import com.sme.erp.reports.dto.SalesReturnReportDTO;
import com.sme.erp.reports.dto.SalesReturnRowDTO;
import com.sme.erp.reports.dto.StockMovementReportRowDTO;
import com.sme.erp.reports.dto.StockTransferReportDTO;
import com.sme.erp.reports.dto.StockReportDTO;
import com.sme.erp.reports.dto.StockReportRowDTO;
import com.sme.erp.reports.dto.SupplierDueReportDTO;
import com.sme.erp.reports.dto.SupplierDueReportRowDTO;
import com.sme.erp.reports.dto.SupplierPurchaseReportDTO;
import com.sme.erp.reports.dto.SupplierPurchaseRowDTO;
import com.sme.erp.reports.dto.TopSellingProductReportDTO;
import com.sme.erp.reports.dto.TopSellingProductRowDTO;
import com.sme.erp.reports.dto.WarehouseStockValuationReportDTO;
import com.sme.erp.reports.export.CsvExportService;
import com.sme.erp.reports.export.ExcelExportService;
import com.sme.erp.reports.export.PdfExportService;
import com.sme.erp.reports.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final CsvExportService csvExportService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    public ReportController(ReportService reportService,
                            CsvExportService csvExportService,
                            ExcelExportService excelExportService,
                            PdfExportService pdfExportService) {
        this.reportService = reportService;
        this.csvExportService = csvExportService;
        this.excelExportService = excelExportService;
        this.pdfExportService = pdfExportService;
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
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getSalesDetail(fromDate, toDate, customerId, productId, warehouseId, status, keyword));
    }

    @GetMapping("/sales-returns")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<SalesReturnReportDTO> getSalesReturns(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getSalesReturns(fromDate, toDate, customerId, productId, warehouseId, status, keyword));
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
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getPurchaseDetail(fromDate, toDate, supplierId, warehouseId, status, keyword));
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

    @GetMapping("/purchase-by-product")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<PurchaseByProductReportDTO> getPurchaseByProduct(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getPurchaseByProduct(fromDate, toDate, supplierId, productId, warehouseId, categoryId, brandId, status, keyword));
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

    @GetMapping("/negative-stock")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<StockReportDTO> getNegativeStock(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(reportService.getNegativeStockReport(warehouseId, productId, categoryId, brandId, keyword));
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

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('REPORT_EXPORT') or hasAuthority('REPORT_EXPORT_CSV') or hasAuthority('REPORT_EXPORT_EXCEL') or hasAuthority('REPORT_EXPORT_PDF')")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String report,
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        ExportPayload payload = exportPayload(report, fromDate, toDate, customerId, supplierId, productId, warehouseId, categoryId, brandId, status, keyword);
        String normalizedFormat = format == null ? "csv" : format.trim().toLowerCase();
        byte[] body = switch (normalizedFormat) {
            case "excel", "xls" -> excelExportService.export(payload.title(), payload.rows(), payload.rowType());
            case "pdf" -> pdfExportService.export(payload.title(), payload.rows(), payload.rowType(),
                    selectedPeriod(fromDate, toDate, status, keyword));
            default -> csvExportService.export(payload.title(), payload.rows(), payload.rowType());
        };
        MediaType mediaType = switch (normalizedFormat) {
            case "excel", "xls" -> MediaType.parseMediaType("application/vnd.ms-excel;charset=UTF-8");
            case "pdf" -> MediaType.APPLICATION_PDF;
            default -> new MediaType("text", "csv");
        };
        String extension = switch (normalizedFormat) {
            case "excel", "xls" -> "xls";
            case "pdf" -> "pdf";
            default -> "csv";
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + payload.fileName() + "." + extension)
                .contentType(mediaType)
                .body(body);
    }

    private ExportPayload exportPayload(String report, LocalDate fromDate, LocalDate toDate, Long customerId,
                                        Long supplierId, Long productId, Long warehouseId, Long categoryId,
                                        Long brandId, String status, String keyword) {
        String type = report == null ? "" : report.trim().toLowerCase();
        return switch (type) {
            case "sales-summary", "sales-detail", "sales-invoices" -> new ExportPayload("Sales Report", type,
                    reportService.getSalesDetail(fromDate, toDate, customerId, productId, warehouseId, status, keyword).getRows(),
                    SalesReportRowDTO.class);
            case "sales-returns" -> new ExportPayload("Sales Return Report", type,
                    reportService.getSalesReturns(fromDate, toDate, customerId, productId, warehouseId, status, keyword).rows(),
                    SalesReturnRowDTO.class);
            case "top-selling-products", "sales-by-product" -> new ExportPayload("Sales By Product Report", type,
                    reportService.getTopSellingProducts(fromDate, toDate, productId, warehouseId, categoryId, brandId, keyword).rows(),
                    TopSellingProductRowDTO.class);
            case "customer-sales" -> new ExportPayload("Customer Sales Report", type,
                    reportService.getCustomerSales(fromDate, toDate, customerId, keyword).rows(),
                    CustomerSalesRowDTO.class);
            case "purchase-summary", "purchase-detail" -> new ExportPayload("Purchase Report", type,
                    reportService.getPurchaseDetail(fromDate, toDate, supplierId, warehouseId, status, keyword).getRows(),
                    PurchaseReportRowDTO.class);
            case "supplier-purchases" -> new ExportPayload("Supplier Purchase Report", type,
                    reportService.getSupplierPurchases(fromDate, toDate, supplierId, keyword).rows(),
                    SupplierPurchaseRowDTO.class);
            case "purchase-by-product" -> new ExportPayload("Purchase By Product Report", type,
                    reportService.getPurchaseByProduct(fromDate, toDate, supplierId, productId, warehouseId, categoryId, brandId, status, keyword).rows(),
                    PurchaseByProductRowDTO.class);
            case "purchase-returns" -> new ExportPayload("Purchase Return Report", type,
                    reportService.getPurchaseReturns(fromDate, toDate, supplierId, keyword).rows(),
                    PurchaseReturnRowDTO.class);
            case "stock", "current-stock", "warehouse-stock" -> new ExportPayload("Current Stock Report", type,
                    reportService.getStockReport(warehouseId, productId, categoryId, brandId, keyword).getRows(),
                    StockReportRowDTO.class);
            case "stock-movements", "stock-ledger" -> new ExportPayload("Stock Movement Report", type,
                    reportService.getStockMovements(fromDate, toDate, warehouseId, productId, keyword).getMovements(),
                    StockMovementReportRowDTO.class);
            case "low-stock" -> new ExportPayload("Low Stock Report", type,
                    reportService.getLowStockReport(warehouseId, productId, categoryId, brandId, keyword).rows(),
                    LowStockRowDTO.class);
            case "negative-stock" -> new ExportPayload("Negative Stock Report", type,
                    reportService.getNegativeStockReport(warehouseId, productId, categoryId, brandId, keyword).getRows(),
                    StockReportRowDTO.class);
            case "customer-dues" -> new ExportPayload("Customer Due Report", type,
                    reportService.getCustomerDueReport().getRows(),
                    CustomerDueReportRowDTO.class);
            case "supplier-dues" -> new ExportPayload("Supplier Due Report", type,
                    reportService.getSupplierDueReport().getRows(),
                    SupplierDueReportRowDTO.class);
            default -> new ExportPayload("Report", "report", java.util.List.of(), null);
        };
    }

    private String selectedPeriod(LocalDate fromDate, LocalDate toDate, String status, String keyword) {
        StringBuilder period = new StringBuilder();
        if (fromDate == null && toDate == null) {
            period.append("All dates");
        } else {
            period.append(fromDate == null ? "Start" : fromDate).append(" to ").append(toDate == null ? "Today" : toDate);
        }
        if (status != null && !status.isBlank()) {
            period.append(" | Status: ").append(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            period.append(" | Search: ").append(keyword.trim());
        }
        return period.toString();
    }

    private record ExportPayload(String title, String fileName, java.util.List<?> rows, Class<?> rowType) {}
}
