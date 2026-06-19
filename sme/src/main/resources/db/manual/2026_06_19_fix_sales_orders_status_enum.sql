-- Align sales_orders.status with the backend SalesOrderStatus enum.
-- Run this manually in MySQL Workbench against the target database.

SHOW COLUMNS FROM sales_orders LIKE 'status';
SELECT status, COUNT(*) AS row_count FROM sales_orders GROUP BY status;

UPDATE sales_orders
SET status = 'SUBMITTED'
WHERE status = 'PENDING';

ALTER TABLE sales_orders
  MODIFY status ENUM('DRAFT','SUBMITTED','APPROVED','REJECTED','CONVERTED','CANCELLED','PENDING')
  NOT NULL
  DEFAULT 'DRAFT';
