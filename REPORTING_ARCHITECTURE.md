# Reporting Architecture

Smart SME ERP uses the existing package-by-feature modules for business data and keeps reporting read-only. Reports reuse current entities, repositories, DTOs, and service methods. Reporting does not mutate workflow, inventory, or accounting state.

## Permissions

- `REPORT_VIEW`: access report APIs and report pages.
- `REPORT_EXPORT`: export reports in any supported format.
- `REPORT_EXPORT_CSV`: export CSV.
- `REPORT_EXPORT_EXCEL`: export Excel-compatible `.xls`.
- `REPORT_EXPORT_PDF`: export PDF.

Migration `V9__reporting_export_permissions.sql` seeds these permissions idempotently for existing installations.

## Export Engine

Backend services:

- `CsvExportService`
- `ExcelExportService`
- `PdfExportService`

The export endpoint is:

- `GET /api/v1/reports/export?report={type}&format=csv|excel|pdf`

Frontend reports use a shared `downloadBlob(...)` utility and the backend export endpoint instead of duplicating CSV/Excel/PDF logic in each report component.

## Reports Implemented

Sales:

- Sales Summary: `/api/v1/reports/sales-summary`
- Sales Invoice Report: `/api/v1/reports/sales-detail`
- Sales Return Report: `/api/v1/reports/sales-returns`
- Customer Sales Report: `/api/v1/reports/customer-sales`
- Sales By Product Report: `/api/v1/reports/top-selling-products`

Purchase:

- Purchase Summary: `/api/v1/reports/purchase-summary`
- Purchase Detail: `/api/v1/reports/purchase-detail`
- Purchase Return Report: `/api/v1/reports/purchase-returns`
- Supplier Purchase Report: `/api/v1/reports/supplier-purchases`
- Purchase By Product Report: `/api/v1/reports/purchase-by-product`

Inventory:

- Current Stock Report: `/api/v1/reports/stock`
- Stock Movement Report: `/api/v1/reports/stock-movements`
- Stock Ledger Report: supported by stock movement report with product and warehouse filters.
- Low Stock Report: `/api/v1/reports/low-stock`
- Negative Stock Report: `/api/v1/reports/negative-stock`
- Warehouse Stock Report: `/api/v1/reports/warehouse-stock-valuation`
- Stock Transfer Report: `/api/v1/reports/stock-transfers`

Financial:

- Customer Due Report: `/api/v1/reports/customer-dues`
- Supplier Due Report: `/api/v1/reports/supplier-dues`
- Profit and Loss Summary: `/api/v1/reports/profit-loss`
- Expense reports: existing expense report center at `/expenses/reports`
- Cash Book and Bank Book: existing accounting routes at `/accounting/cash-book` and `/accounting/bank-book`

## Dashboard Analytics APIs

- `GET /api/v1/dashboard/analytics/top-selling-products`
- `GET /api/v1/dashboard/analytics/top-customers`
- `GET /api/v1/dashboard/analytics/top-suppliers`
- `GET /api/v1/dashboard/analytics/monthly-sales-trend`
- `GET /api/v1/dashboard/analytics/monthly-purchase-trend`
- `GET /api/v1/dashboard/analytics/monthly-expense-trend`

These reuse the existing dashboard/report aggregation services.

## Filters Supported

Sales reports support date range, customer, product, warehouse, status, and keyword where the underlying entity has those fields.

Purchase reports support date range, supplier, product, warehouse, category, brand, status, and keyword where the underlying entity has those fields.

Inventory reports support product, warehouse, category, brand, date range for movement reports, and keyword.

Due reports currently use posted/confirmed business balances from existing invoice and purchase fields.

## Unsupported or Routed Reports

No fake accounting reports were generated. Expense summary/category/payment-method/tax/trend reports remain in the existing expense report center. Cash Book and Bank Book remain in the existing accounting module.

## Limitations and Risks

PDF export is dependency-free and intended for portable printable report extracts. For advanced layout, branding, page numbers, and multi-page tabular pagination, add a dedicated PDF library in a later controlled dependency change.

Report accuracy depends on transaction modules posting correct `netTotal`, `paidAmount`, `dueAmount`, stock quantity, and return quantity fields. The reporting layer does not recalculate or repair transactional history.

The migration seeds permissions only; no report schema changes are required.
