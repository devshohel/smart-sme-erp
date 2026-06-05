-- Sales status enum migration for Smart SME ERP.
-- Run after backup and before saving new DRAFT/APPROVED sales orders.

-- Inspect current values.
SHOW COLUMNS FROM sales_orders LIKE 'status';
SELECT status, COUNT(*) AS row_count FROM sales_orders GROUP BY status;

-- Convert old order workflow values into the ERP-standard purchase-like order flow.
UPDATE sales_orders SET status = 'APPROVED' WHERE status IN ('CONFIRMED', 'COMPLETED');
UPDATE sales_orders SET status = 'PENDING' WHERE status IS NULL OR status = '';

-- Allow only the Sales Order workflow statuses.
ALTER TABLE sales_orders
  MODIFY status ENUM('DRAFT','PENDING','APPROVED','CANCELLED') NOT NULL DEFAULT 'PENDING';

-- Optional invoice status alignment if sales_invoices.status is also an enum in the live database.
SHOW COLUMNS FROM sales_invoices LIKE 'status';
SELECT status, COUNT(*) AS row_count FROM sales_invoices GROUP BY status;

UPDATE sales_invoices SET status = 'PENDING' WHERE status IS NULL OR status = '' OR status = 'DRAFT';

ALTER TABLE sales_invoices
  MODIFY status ENUM('PENDING','CONFIRMED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING';
