package com.sme.erp.reports.service;

import com.sme.erp.accounting.repository.ExpenseRepository;
import com.sme.erp.inventory.repository.StockMovementRepository;
import com.sme.erp.inventory.repository.StockRepository;
import com.sme.erp.inventory.repository.StockTransferRepository;
import com.sme.erp.inventory.enums.StockTransferStatus;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.repository.PurchaseReturnRepository;
import com.sme.erp.reports.dto.SalesReportRowDTO;
import com.sme.erp.reports.dto.StockMovementReportRowDTO;
import com.sme.erp.reports.dto.StockReportRowDTO;
import com.sme.erp.reports.dto.StockTransferRowDTO;
import com.sme.erp.reports.dto.TopSellingProductRowDTO;
import com.sme.erp.reports.service.impl.ReportServiceImpl;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock private SalesInvoiceRepository salesInvoiceRepository;
    @Mock private SalesReturnRepository salesReturnRepository;
    @Mock private PurchaseOrderRepository purchaseOrderRepository;
    @Mock private PurchaseReturnRepository purchaseReturnRepository;
    @Mock private StockRepository stockRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private StockTransferRepository stockTransferRepository;
    @Mock private ExpenseRepository expenseRepository;

    private ReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReportServiceImpl(
                salesInvoiceRepository,
                salesReturnRepository,
                purchaseOrderRepository,
                purchaseReturnRepository,
                stockRepository,
                stockMovementRepository,
                stockTransferRepository,
                expenseRepository);
    }

    @Test
    void salesSummaryIncludesReturnsAndNetSales() {
        when(salesInvoiceRepository.findSalesReportRows(null, null, null, null)).thenReturn(List.of(
                new SalesReportRowDTO("INV-001", "Customer A", "Main", "CONFIRMED", LocalDateTime.now(),
                        new BigDecimal("2"), new BigDecimal("100"), new BigDecimal("80"), new BigDecimal("20"))));
        when(salesReturnRepository.sumReturnAmount(null, null, null)).thenReturn(new BigDecimal("15"));

        var report = service.getSalesSummary(null, null, null);

        assertThat(report.getTotalSales()).isEqualByComparingTo("100");
        assertThat(report.getReturnAmount()).isEqualByComparingTo("15");
        assertThat(report.getNetSales()).isEqualByComparingTo("85");
    }

    @Test
    void purchaseSummaryIncludesReturnsAndNetPurchase() {
        when(purchaseOrderRepository.findPurchaseReportRows(null, null, null)).thenReturn(List.of(
                new com.sme.erp.reports.dto.PurchaseReportRowDTO("PO-001", "Supplier A", "Central", LocalDateTime.now(),
                        new BigDecimal("250"), new BigDecimal("150"), new BigDecimal("100"),
                        com.sme.erp.purchase.enums.PurchaseStatus.RECEIVED)));
        when(purchaseReturnRepository.sumReturnAmount(null, null, null)).thenReturn(new BigDecimal("50"));

        var report = service.getPurchaseSummary(null, null, null);

        assertThat(report.getTotalPurchase()).isEqualByComparingTo("250");
        assertThat(report.getReturnAmount()).isEqualByComparingTo("50");
        assertThat(report.getNetPurchase()).isEqualByComparingTo("200");
    }

    @Test
    void lowStockReportCalculatesShortageQuantity() {
        when(stockRepository.findStockReportRows(null, null, null, null, null)).thenReturn(List.of(
                new StockReportRowDTO("Rice", "SKU-1", "Food", "Own", "Main", new BigDecimal("4"), 10, "ACTIVE", new BigDecimal("40")),
                new StockReportRowDTO("Oil", "SKU-2", "Food", "Own", "Main", new BigDecimal("12"), 10, "ACTIVE", new BigDecimal("120"))));

        var report = service.getLowStockReport(null, null, null, null, null);

        assertThat(report.totalLowStockItems()).isEqualTo(1);
        assertThat(report.totalShortageQty()).isEqualByComparingTo("6");
        assertThat(report.rows().get(0).product()).isEqualTo("Rice");
    }

    @Test
    void warehouseValuationGroupsStockByWarehouse() {
        when(stockRepository.findStockReportRows(null, null, null, null, null)).thenReturn(List.of(
                new StockReportRowDTO("Rice", "SKU-1", "Food", "Own", "Main", new BigDecimal("4"), 10, "ACTIVE", new BigDecimal("40")),
                new StockReportRowDTO("Oil", "SKU-2", "Food", "Own", "Main", new BigDecimal("6"), 10, "ACTIVE", new BigDecimal("90")),
                new StockReportRowDTO("Sugar", "SKU-3", "Food", "Own", "Outlet", new BigDecimal("2"), 5, "ACTIVE", new BigDecimal("30"))));

        var report = service.getWarehouseStockValuation(null, null, null, null);

        assertThat(report.totalWarehouses()).isEqualTo(2);
        assertThat(report.totalQuantity()).isEqualByComparingTo("12");
        assertThat(report.totalStockValue()).isEqualByComparingTo("160");
    }

    @Test
    void topSellingProductsTotalsReturnedRows() {
        when(salesInvoiceRepository.findTopSellingProductRows(null, null, null, null, null, null, null)).thenReturn(List.of(
                new TopSellingProductRowDTO(1L, "Rice", "SKU-1", new BigDecimal("8"), new BigDecimal("320"), BigDecimal.ZERO, new BigDecimal("8")),
                new TopSellingProductRowDTO(2L, "Oil", "SKU-2", new BigDecimal("3"), new BigDecimal("150"), BigDecimal.ZERO, new BigDecimal("3"))));

        var report = service.getTopSellingProducts(null, null, null, null, null, null, null);

        assertThat(report.totalQuantitySold()).isEqualByComparingTo("11");
        assertThat(report.totalGrossSales()).isEqualByComparingTo("470");
        assertThat(report.rows()).hasSize(2);
    }

    @Test
    void stockMovementReportReturnsMovementRowsAndTotalQuantity() {
        when(stockMovementRepository.findStockMovementReportRows(any(), any(), any(), any(), any())).thenReturn(List.of(
                new StockMovementReportRowDTO(LocalDateTime.now(), "Rice", "Main", "IN", new BigDecimal("5"),
                        new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("15"), "PO-001"),
                new StockMovementReportRowDTO(LocalDateTime.now(), "Rice", "Main", "OUT", new BigDecimal("2"),
                        new BigDecimal("15"), new BigDecimal("-2"), new BigDecimal("13"), "SO-001")));

        var report = service.getStockMovements(LocalDate.now(), LocalDate.now(), null, null, null);

        assertThat(report.getMovements()).hasSize(2);
        assertThat(report.getTotalStockQuantity()).isEqualByComparingTo("7");
    }

    @Test
    void stockTransferReportTotalsRowsItemsAndQuantity() {
        when(stockTransferRepository.findTransferReportRows(null, null, null, null, null)).thenReturn(List.of(
                new StockTransferRowDTO("TR-001", "Main", "Outlet", "IN_TRANSIT", LocalDate.now(), 2L, new BigDecimal("9")),
                new StockTransferRowDTO("TR-002", "Main", "Warehouse 2", "RECEIVED", LocalDate.now(), 1L, new BigDecimal("3"))));

        var report = service.getStockTransfers(null, null, null, null, null);

        assertThat(report.totalTransfers()).isEqualTo(2);
        assertThat(report.totalItems()).isEqualTo(3);
        assertThat(report.totalQuantity()).isEqualByComparingTo("12");
    }

    @Test
    void stockTransferReportParsesStatusBeforeQuerying() {
        when(stockTransferRepository.findTransferReportRows(null, null, null, StockTransferStatus.IN_TRANSIT, null))
                .thenReturn(List.of());

        var report = service.getStockTransfers(null, null, null, "in_transit", null);

        assertThat(report.totalTransfers()).isZero();
    }
}
