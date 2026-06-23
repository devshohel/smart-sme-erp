-- Align supplier_payments.status with the backend SupplierPaymentStatus enum.
-- Run this manually in MySQL Workbench against the target database.
-- Existing DRAFT, POSTED, and CANCELLED rows remain valid.

SHOW COLUMNS FROM supplier_payments LIKE 'status';
SELECT status, COUNT(*) AS row_count FROM supplier_payments GROUP BY status;

ALTER TABLE supplier_payments
  MODIFY status ENUM('DRAFT','POSTED','CANCELLED','REVERSED')
  NOT NULL
  DEFAULT 'DRAFT';
