-- Align purchase_orders.status with the backend PurchaseStatus enum.
-- Run this manually in MySQL Workbench against the target database.
-- This preserves existing legacy PENDING rows and only expands allowed values.

SHOW COLUMNS FROM purchase_orders LIKE 'status';
SELECT status, COUNT(*) AS row_count FROM purchase_orders GROUP BY status;

ALTER TABLE purchase_orders
  MODIFY status ENUM('DRAFT','SUBMITTED','PENDING','APPROVED','REJECTED','PARTIAL_RECEIVED','RECEIVED','POSTED','PARTIAL_PAID','PAID','REVERSED','CANCELLED')
  NOT NULL
  DEFAULT 'DRAFT';
