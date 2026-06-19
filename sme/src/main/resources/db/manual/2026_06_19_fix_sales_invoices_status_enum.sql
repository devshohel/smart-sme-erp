-- Align sales_invoices.status and sales_invoices.payment_status with backend enums.
-- Run this manually in MySQL Workbench against the target database.

SHOW COLUMNS FROM sales_invoices LIKE 'status';
SHOW COLUMNS FROM sales_invoices LIKE 'payment_status';
SELECT status, COUNT(*) AS row_count FROM sales_invoices GROUP BY status;
SELECT payment_status, COUNT(*) AS row_count FROM sales_invoices GROUP BY payment_status;

UPDATE sales_invoices
SET status = 'DRAFT'
WHERE status = 'PENDING';

ALTER TABLE sales_invoices
  MODIFY status ENUM('DRAFT','SUBMITTED','APPROVED','POSTED','PARTIAL_PAID','PAID','CANCELLED','REVERSED','PENDING','CONFIRMED','COMPLETED')
  NOT NULL
  DEFAULT 'DRAFT',
  MODIFY payment_status ENUM('PAID','PARTIAL','DUE')
  NOT NULL
  DEFAULT 'DUE';
