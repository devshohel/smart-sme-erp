package com.sme.erp.reports.service.impl;

import com.sme.erp.accounting.repository.ExpenseRepository;
import com.sme.erp.inventory.repository.StockMovementRepository;
import com.sme.erp.inventory.repository.StockRepository;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.reports.dto.CustomerDueReportDTO;
import com.sme.erp.reports.dto.CustomerDueReportRowDTO;
import com.sme.erp.reports.dto.ProfitLossSummaryDTO;
import com.sme.erp.reports.dto.PurchaseReportDTO;
import com.sme.erp.reports.dto.PurchaseReportRowDTO;
import com.sme.erp.reports.dto.SalesReportDTO;
import com.sme.erp.reports.dto.SalesReportRowDTO;
import com.sme.erp.reports.dto.StockMovementReportRowDTO;
import com.sme.erp.reports.dto.StockReportDTO;
import com.sme.erp.reports.dto.StockReportRowDTO;
import com.sme.erp.reports.dto.SupplierDueReportDTO;
import com.sme.erp.reports.dto.SupplierDueReportRowDTO;
import com.sme.erp.reports.service.ReportService;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ExpenseRepository expenseRepository;

    public ReportServiceImpl(SalesInvoiceRepository salesInvoiceRepository,
                             PurchaseOrderRepository purchaseOrderRepository,
                             StockRepository stockRepository,
                             StockMovementRepository stockMovementRepository,
                             ExpenseRepository expenseRepository) {
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReportDTO getSalesReport(LocalDate startDate, LocalDate endDate, Long customerId, Long productId) {
        List<SalesReportRowDTO> rows = salesInvoiceRepository.findSalesReportRows(
                startOfDay(startDate), exclusiveEnd(endDate), customerId, productId);

        return new SalesReportDTO(
                sumSales(rows),
                sumSalesPaid(rows),
                sumSalesDue(rows),
                (long) rows.size(),
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseReportDTO getPurchaseReport(LocalDate startDate, LocalDate endDate, Long supplierId) {
        List<PurchaseReportRowDTO> rows = purchaseOrderRepository.findPurchaseReportRows(
                startOfDay(startDate), exclusiveEnd(endDate), supplierId);

        return new PurchaseReportDTO(
                sumPurchase(rows),
                sumPurchasePaid(rows),
                sumPurchaseDue(rows),
                (long) rows.size(),
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public StockReportDTO getStockReport(Long warehouseId, Long productId) {
        List<StockReportRowDTO> rows = stockRepository.findStockReportRows(warehouseId, productId);
        List<StockMovementReportRowDTO> movements = stockMovementRepository.findStockMovementReportRows(warehouseId, productId);

        return new StockReportDTO(
                rows.stream().map(StockReportRowDTO::getQuantity).reduce(BigDecimal.ZERO, this::add),
                rows.stream().map(StockReportRowDTO::getStockValue).reduce(BigDecimal.ZERO, this::add),
                rows.stream().filter(this::isLowStock).count(),
                rows,
                movements);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDueReportDTO getCustomerDueReport() {
        List<CustomerDueReportRowDTO> rows = salesInvoiceRepository.findCustomerDueReportRows();
        return new CustomerDueReportDTO(
                rows.stream().map(CustomerDueReportRowDTO::getDue).reduce(BigDecimal.ZERO, this::add),
                (long) rows.size(),
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDueReportDTO getSupplierDueReport() {
        List<SupplierDueReportRowDTO> rows = purchaseOrderRepository.findSupplierDueReportRows();
        return new SupplierDueReportDTO(
                rows.stream().map(SupplierDueReportRowDTO::getDue).reduce(BigDecimal.ZERO, this::add),
                (long) rows.size(),
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfitLossSummaryDTO getProfitLossSummary(LocalDate startDate, LocalDate endDate) {
        SalesReportDTO salesReport = getSalesReport(startDate, endDate, null, null);
        PurchaseReportDTO purchaseReport = getPurchaseReport(startDate, endDate, null);
        return new ProfitLossSummaryDTO(
                safe(salesReport.getTotalSales()),
                safe(purchaseReport.getTotalPurchase()),
                safe(expenseRepository.sumActiveAmountBetween(startDate, endDate)));
    }

    private LocalDateTime startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime exclusiveEnd(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay();
    }

    private boolean isLowStock(StockReportRowDTO row) {
        BigDecimal reorderLevel = safe(row.getReorderLevel());
        return reorderLevel.signum() > 0 && safe(row.getQuantity()).compareTo(reorderLevel) <= 0;
    }

    private BigDecimal sumSales(List<SalesReportRowDTO> rows) {
        return rows.stream().map(SalesReportRowDTO::getAmount).reduce(BigDecimal.ZERO, this::add);
    }

    private BigDecimal sumSalesPaid(List<SalesReportRowDTO> rows) {
        return rows.stream().map(SalesReportRowDTO::getPaid).reduce(BigDecimal.ZERO, this::add);
    }

    private BigDecimal sumSalesDue(List<SalesReportRowDTO> rows) {
        return rows.stream().map(SalesReportRowDTO::getDue).reduce(BigDecimal.ZERO, this::add);
    }

    private BigDecimal sumPurchase(List<PurchaseReportRowDTO> rows) {
        return rows.stream().map(PurchaseReportRowDTO::getAmount).reduce(BigDecimal.ZERO, this::add);
    }

    private BigDecimal sumPurchasePaid(List<PurchaseReportRowDTO> rows) {
        return rows.stream().map(PurchaseReportRowDTO::getPaid).reduce(BigDecimal.ZERO, this::add);
    }

    private BigDecimal sumPurchaseDue(List<PurchaseReportRowDTO> rows) {
        return rows.stream().map(PurchaseReportRowDTO::getDue).reduce(BigDecimal.ZERO, this::add);
    }

    private BigDecimal add(BigDecimal left, BigDecimal right) {
        return safe(left).add(safe(right));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
