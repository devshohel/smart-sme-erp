package com.sme.erp.reports.service.impl;

import com.sme.erp.accounting.repository.ExpenseRepository;
import com.sme.erp.inventory.repository.StockMovementRepository;
import com.sme.erp.inventory.repository.StockRepository;
import com.sme.erp.inventory.repository.StockTransferRepository;
import com.sme.erp.inventory.enums.StockTransferStatus;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.purchase.repository.PurchaseReturnRepository;
import com.sme.erp.reports.dto.CustomerDueReportDTO;
import com.sme.erp.reports.dto.CustomerDueReportRowDTO;
import com.sme.erp.reports.dto.CustomerSalesReportDTO;
import com.sme.erp.reports.dto.CustomerSalesRowDTO;
import com.sme.erp.reports.dto.LowStockReportDTO;
import com.sme.erp.reports.dto.LowStockRowDTO;
import com.sme.erp.reports.dto.ProfitLossSummaryDTO;
import com.sme.erp.reports.dto.PurchaseReturnReportDTO;
import com.sme.erp.reports.dto.PurchaseByProductReportDTO;
import com.sme.erp.reports.dto.PurchaseByProductRowDTO;
import com.sme.erp.reports.dto.PurchaseReturnRowDTO;
import com.sme.erp.reports.dto.PurchaseReportDTO;
import com.sme.erp.reports.dto.PurchaseReportRowDTO;
import com.sme.erp.reports.dto.SalesReportDTO;
import com.sme.erp.reports.dto.SalesReportRowDTO;
import com.sme.erp.reports.dto.SalesReturnReportDTO;
import com.sme.erp.reports.dto.SalesReturnRowDTO;
import com.sme.erp.reports.dto.StockTransferReportDTO;
import com.sme.erp.reports.dto.StockTransferRowDTO;
import com.sme.erp.reports.dto.StockMovementReportRowDTO;
import com.sme.erp.reports.dto.StockReportDTO;
import com.sme.erp.reports.dto.StockReportRowDTO;
import com.sme.erp.reports.dto.SupplierDueReportDTO;
import com.sme.erp.reports.dto.SupplierDueReportRowDTO;
import com.sme.erp.reports.dto.SupplierPurchaseReportDTO;
import com.sme.erp.reports.dto.SupplierPurchaseRowDTO;
import com.sme.erp.reports.dto.TopSellingProductReportDTO;
import com.sme.erp.reports.dto.TopSellingProductRowDTO;
import com.sme.erp.reports.dto.WarehouseStockValuationReportDTO;
import com.sme.erp.reports.dto.WarehouseStockValuationRowDTO;
import com.sme.erp.reports.service.ReportService;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.enums.SalesReturnStatus;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesReturnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesReturnRepository salesReturnRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseReturnRepository purchaseReturnRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockTransferRepository stockTransferRepository;
    private final ExpenseRepository expenseRepository;

    public ReportServiceImpl(SalesInvoiceRepository salesInvoiceRepository,
                             SalesReturnRepository salesReturnRepository,
                             PurchaseOrderRepository purchaseOrderRepository,
                             PurchaseReturnRepository purchaseReturnRepository,
                             StockRepository stockRepository,
                             StockMovementRepository stockMovementRepository,
                             StockTransferRepository stockTransferRepository,
                             ExpenseRepository expenseRepository) {
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesReturnRepository = salesReturnRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockTransferRepository = stockTransferRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReportDTO getSalesReport(LocalDate startDate, LocalDate endDate, Long customerId, Long productId) {
        return getSalesDetail(startDate, endDate, customerId, productId, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReportDTO getSalesSummary(LocalDate fromDate, LocalDate toDate, Long customerId) {
        return buildSalesReport(
                salesInvoiceRepository.findSalesReportRows(startOfDay(fromDate), exclusiveEnd(toDate), customerId, null),
                salesReturnRepository.sumReturnAmount(startOfDay(fromDate), exclusiveEnd(toDate), customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReportDTO getSalesDetail(LocalDate fromDate, LocalDate toDate, Long customerId, Long productId,
                                         Long warehouseId, String keyword) {
        List<SalesReportRowDTO> rows = salesInvoiceRepository.findSalesDetailRows(
                startOfDay(fromDate), exclusiveEnd(toDate), customerId, productId, warehouseId, clean(keyword));
        BigDecimal returnAmount = salesReturnRepository.sumReturnAmount(startOfDay(fromDate), exclusiveEnd(toDate), customerId);
        return buildSalesReport(rows, returnAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReportDTO getSalesDetail(LocalDate fromDate, LocalDate toDate, Long customerId, Long productId,
                                         Long warehouseId, String status, String keyword) {
        List<SalesReportRowDTO> rows = salesInvoiceRepository.findSalesDetailRowsWithStatus(
                startOfDay(fromDate), exclusiveEnd(toDate), customerId, productId, warehouseId,
                parseEnum(status, SalesInvoiceStatus.class), clean(keyword));
        BigDecimal returnAmount = salesReturnRepository.sumReturnAmount(startOfDay(fromDate), exclusiveEnd(toDate), customerId);
        return buildSalesReport(rows, returnAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReturnReportDTO getSalesReturns(LocalDate fromDate, LocalDate toDate, Long customerId, Long productId,
                                                Long warehouseId, String status, String keyword) {
        List<SalesReturnRowDTO> rows = salesReturnRepository.findSalesReturnReportRows(
                startOfDay(fromDate), exclusiveEnd(toDate), customerId, productId, warehouseId,
                parseEnum(status, SalesReturnStatus.class), clean(keyword));
        return new SalesReturnReportDTO(
                (long) rows.size(),
                rows.stream().map(SalesReturnRowDTO::amount).reduce(BigDecimal.ZERO, this::add),
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public TopSellingProductReportDTO getTopSellingProducts(LocalDate fromDate, LocalDate toDate, Long productId,
                                                            Long warehouseId, Long categoryId, Long brandId,
                                                            String keyword) {
        List<TopSellingProductRowDTO> rows = salesInvoiceRepository.findTopSellingProductRows(
                startOfDay(fromDate), exclusiveEnd(toDate), productId, warehouseId, categoryId, brandId, clean(keyword));
        BigDecimal totalQuantity = rows.stream().map(TopSellingProductRowDTO::quantitySold).reduce(BigDecimal.ZERO, this::add);
        BigDecimal totalGrossSales = rows.stream().map(TopSellingProductRowDTO::grossSales).reduce(BigDecimal.ZERO, this::add);
        BigDecimal totalReturnQty = rows.stream().map(TopSellingProductRowDTO::returnQty).reduce(BigDecimal.ZERO, this::add);
        BigDecimal totalNetQty = rows.stream().map(TopSellingProductRowDTO::netQty).reduce(BigDecimal.ZERO, this::add);
        return new TopSellingProductReportDTO(totalQuantity, totalGrossSales, totalReturnQty, totalNetQty, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSalesReportDTO getCustomerSales(LocalDate fromDate, LocalDate toDate, Long customerId, String keyword) {
        List<CustomerSalesRowDTO> rows = salesInvoiceRepository.findCustomerSalesRows(
                startOfDay(fromDate), exclusiveEnd(toDate), customerId, clean(keyword));
        return new CustomerSalesReportDTO(
                (long) rows.size(),
                rows.stream().map(CustomerSalesRowDTO::totalSales).reduce(BigDecimal.ZERO, this::add),
                rows.stream().map(CustomerSalesRowDTO::paidAmount).reduce(BigDecimal.ZERO, this::add),
                rows.stream().map(CustomerSalesRowDTO::dueAmount).reduce(BigDecimal.ZERO, this::add),
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseReportDTO getPurchaseReport(LocalDate startDate, LocalDate endDate, Long supplierId) {
        return getPurchaseDetail(startDate, endDate, supplierId, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseReportDTO getPurchaseSummary(LocalDate fromDate, LocalDate toDate, Long supplierId) {
        return buildPurchaseReport(
                purchaseOrderRepository.findPurchaseReportRows(startOfDay(fromDate), exclusiveEnd(toDate), supplierId),
                purchaseReturnRepository.sumReturnAmount(startOfDay(fromDate), exclusiveEnd(toDate), supplierId));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseReportDTO getPurchaseDetail(LocalDate fromDate, LocalDate toDate, Long supplierId, Long warehouseId, String keyword) {
        List<PurchaseReportRowDTO> rows = purchaseOrderRepository.findPurchaseDetailRows(
                startOfDay(fromDate), exclusiveEnd(toDate), supplierId, warehouseId, clean(keyword));
        BigDecimal returnAmount = purchaseReturnRepository.sumReturnAmount(startOfDay(fromDate), exclusiveEnd(toDate), supplierId);
        return buildPurchaseReport(rows, returnAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseReportDTO getPurchaseDetail(LocalDate fromDate, LocalDate toDate, Long supplierId, Long warehouseId, String status, String keyword) {
        List<PurchaseReportRowDTO> rows = purchaseOrderRepository.findPurchaseDetailRowsWithStatus(
                startOfDay(fromDate), exclusiveEnd(toDate), supplierId, warehouseId, parseEnum(status, PurchaseStatus.class), clean(keyword));
        BigDecimal returnAmount = purchaseReturnRepository.sumReturnAmount(startOfDay(fromDate), exclusiveEnd(toDate), supplierId);
        return buildPurchaseReport(rows, returnAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierPurchaseReportDTO getSupplierPurchases(LocalDate fromDate, LocalDate toDate, Long supplierId, String keyword) {
        List<SupplierPurchaseRowDTO> rows = purchaseOrderRepository.findSupplierPurchaseRows(
                startOfDay(fromDate), exclusiveEnd(toDate), supplierId, clean(keyword));
        return new SupplierPurchaseReportDTO(
                (long) rows.size(),
                rows.stream().map(SupplierPurchaseRowDTO::totalPurchase).reduce(BigDecimal.ZERO, this::add),
                rows.stream().map(SupplierPurchaseRowDTO::paidAmount).reduce(BigDecimal.ZERO, this::add),
                rows.stream().map(SupplierPurchaseRowDTO::dueAmount).reduce(BigDecimal.ZERO, this::add),
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseByProductReportDTO getPurchaseByProduct(LocalDate fromDate, LocalDate toDate, Long supplierId, Long productId,
                                                           Long warehouseId, Long categoryId, Long brandId, String status, String keyword) {
        List<PurchaseByProductRowDTO> rows = purchaseOrderRepository.findPurchaseByProductRows(
                startOfDay(fromDate), exclusiveEnd(toDate), supplierId, productId, warehouseId, categoryId, brandId,
                parseEnum(status, PurchaseStatus.class), clean(keyword));
        return new PurchaseByProductReportDTO(
                rows.stream().map(PurchaseByProductRowDTO::quantityPurchased).reduce(BigDecimal.ZERO, this::add),
                rows.stream().map(PurchaseByProductRowDTO::grossPurchase).reduce(BigDecimal.ZERO, this::add),
                rows.stream().map(PurchaseByProductRowDTO::returnQty).reduce(BigDecimal.ZERO, this::add),
                rows.stream().map(PurchaseByProductRowDTO::netQty).reduce(BigDecimal.ZERO, this::add),
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseReturnReportDTO getPurchaseReturns(LocalDate fromDate, LocalDate toDate, Long supplierId, String keyword) {
        List<PurchaseReturnRowDTO> rows = purchaseReturnRepository.findPurchaseReturnRows(
                startOfDay(fromDate), exclusiveEnd(toDate), supplierId, clean(keyword));
        return new PurchaseReturnReportDTO(
                (long) rows.size(),
                rows.stream().map(PurchaseReturnRowDTO::amount).reduce(BigDecimal.ZERO, this::add),
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public StockReportDTO getStockReport(Long warehouseId, Long productId) {
        return getStockReport(warehouseId, productId, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public StockReportDTO getStockReport(Long warehouseId, Long productId, Long categoryId, Long brandId, String keyword) {
        List<StockReportRowDTO> rows = stockRepository.findStockReportRows(warehouseId, productId, categoryId, brandId, clean(keyword));
        return new StockReportDTO(
                rows.stream().map(StockReportRowDTO::getQuantity).reduce(BigDecimal.ZERO, this::add),
                rows.stream().map(StockReportRowDTO::getStockValue).reduce(BigDecimal.ZERO, this::add),
                rows.stream().filter(this::isLowStock).count(),
                rows,
                List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public StockReportDTO getStockMovements(LocalDate fromDate, LocalDate toDate, Long warehouseId, Long productId, String keyword) {
        List<StockMovementReportRowDTO> movements = stockMovementRepository.findStockMovementReportRows(
                warehouseId, productId, startOfDay(fromDate), exclusiveEnd(toDate), clean(keyword));
        return new StockReportDTO(
                movements.stream().map(StockMovementReportRowDTO::getQuantity).reduce(BigDecimal.ZERO, this::add),
                BigDecimal.ZERO,
                0L,
                List.of(),
                movements);
    }

    @Override
    @Transactional(readOnly = true)
    public StockReportDTO getNegativeStockReport(Long warehouseId, Long productId, Long categoryId, Long brandId, String keyword) {
        List<StockReportRowDTO> rows = stockRepository.findStockReportRows(warehouseId, productId, categoryId, brandId, clean(keyword)).stream()
                .filter(row -> safe(row.getQuantity()).signum() < 0)
                .toList();
        return new StockReportDTO(
                rows.stream().map(StockReportRowDTO::getQuantity).reduce(BigDecimal.ZERO, this::add),
                rows.stream().map(StockReportRowDTO::getStockValue).reduce(BigDecimal.ZERO, this::add),
                0L,
                rows,
                List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public LowStockReportDTO getLowStockReport(Long warehouseId, Long productId, Long categoryId, Long brandId, String keyword) {
        List<LowStockRowDTO> rows = stockRepository.findStockReportRows(warehouseId, productId, categoryId, brandId, clean(keyword)).stream()
                .filter(this::isLowStock)
                .map(row -> new LowStockRowDTO(
                        row.getProduct(),
                        row.getSku(),
                        row.getWarehouse(),
                        safe(row.getQuantity()),
                        safe(row.getReorderLevel()),
                        safe(row.getReorderLevel()).subtract(safe(row.getQuantity())).max(BigDecimal.ZERO)))
                .toList();
        return new LowStockReportDTO(
                (long) rows.size(),
                rows.stream().map(LowStockRowDTO::shortageQty).reduce(BigDecimal.ZERO, this::add),
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseStockValuationReportDTO getWarehouseStockValuation(Long warehouseId, Long categoryId, Long brandId, String keyword) {
        Map<String, List<StockReportRowDTO>> grouped = stockRepository.findStockReportRows(warehouseId, null, categoryId, brandId, clean(keyword)).stream()
                .collect(Collectors.groupingBy(StockReportRowDTO::getWarehouse));
        List<WarehouseStockValuationRowDTO> rows = grouped.entrySet().stream()
                .map(entry -> new WarehouseStockValuationRowDTO(
                        entry.getKey(),
                        (long) entry.getValue().size(),
                        entry.getValue().stream().map(StockReportRowDTO::getQuantity).reduce(BigDecimal.ZERO, this::add),
                        entry.getValue().stream().map(StockReportRowDTO::getStockValue).reduce(BigDecimal.ZERO, this::add)))
                .sorted(Comparator.comparing(WarehouseStockValuationRowDTO::warehouse))
                .toList();
        return new WarehouseStockValuationReportDTO(
                (long) rows.size(),
                rows.stream().map(WarehouseStockValuationRowDTO::totalQty).reduce(BigDecimal.ZERO, this::add),
                rows.stream().map(WarehouseStockValuationRowDTO::stockValue).reduce(BigDecimal.ZERO, this::add),
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransferReportDTO getStockTransfers(LocalDate fromDate, LocalDate toDate, Long warehouseId, String status, String keyword) {
        List<StockTransferRowDTO> rows = stockTransferRepository.findTransferReportRows(
                fromDate, toDate, warehouseId, parseTransferStatus(status), clean(keyword));
        return new StockTransferReportDTO(
                (long) rows.size(),
                rows.stream().map(StockTransferRowDTO::itemCount).reduce(0L, Long::sum),
                rows.stream().map(StockTransferRowDTO::totalQty).reduce(BigDecimal.ZERO, this::add),
                rows);
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

    private SalesReportDTO buildSalesReport(List<SalesReportRowDTO> rows, BigDecimal returnAmount) {
        BigDecimal totalSales = sumSales(rows);
        BigDecimal totalPaid = sumSalesPaid(rows);
        BigDecimal totalDue = sumSalesDue(rows);
        BigDecimal returns = safe(returnAmount);
        return new SalesReportDTO(
                totalSales,
                totalPaid,
                totalDue,
                (long) rows.size(),
                returns,
                totalSales.subtract(returns),
                rows);
    }

    private PurchaseReportDTO buildPurchaseReport(List<PurchaseReportRowDTO> rows, BigDecimal returnAmount) {
        BigDecimal totalPurchase = sumPurchase(rows);
        BigDecimal totalPaid = sumPurchasePaid(rows);
        BigDecimal totalDue = sumPurchaseDue(rows);
        BigDecimal returns = safe(returnAmount);
        return new PurchaseReportDTO(
                totalPurchase,
                totalPaid,
                totalDue,
                (long) rows.size(),
                returns,
                totalPurchase.subtract(returns),
                rows);
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

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private StockTransferStatus parseTransferStatus(String status) {
        String value = clean(status);
        if (value == null) {
            return null;
        }
        return StockTransferStatus.valueOf(value.toUpperCase());
    }

    private <E extends Enum<E>> E parseEnum(String status, Class<E> enumType) {
        String value = clean(status);
        return value == null ? null : Enum.valueOf(enumType, value.toUpperCase());
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
