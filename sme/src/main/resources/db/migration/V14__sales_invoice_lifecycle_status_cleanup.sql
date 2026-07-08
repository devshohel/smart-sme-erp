-- Sales invoice lifecycle cleanup.
-- Do not run until reviewed and approved for the target database.
--
-- Affected row count checks:
-- SELECT status, COUNT(*) AS row_count
-- FROM sales_invoices
-- GROUP BY status
-- ORDER BY status;
--
-- SELECT payment_status, COUNT(*) AS row_count
-- FROM sales_invoices
-- GROUP BY payment_status
-- ORDER BY payment_status;
--
-- Rollback consideration:
-- This migration collapses historical workflow/payment-derived invoice statuses into
-- lifecycle statuses. Reconstructing the original status values is not deterministic
-- after execution unless a database backup or pre-migration status export exists.

UPDATE sales_invoices
SET status = 'POSTED'
WHERE status IN (
  'SUBMITTED',
  'APPROVED',
  'CONFIRMED',
  'COMPLETED',
  'PAID',
  'PARTIAL_PAID'
);

-- PENDING was a legacy unfinalized state and is migrated to the only
-- unfinalized lifecycle status in the corrected workflow.
UPDATE sales_invoices
SET status = 'DRAFT'
WHERE status = 'PENDING';

UPDATE sales_invoices
SET status = 'CANCELLED'
WHERE status = 'REVERSED';

ALTER TABLE sales_invoices
  MODIFY status ENUM('DRAFT','POSTED','CANCELLED','RETURNED')
  NOT NULL DEFAULT 'DRAFT';

ALTER TABLE sales_invoices
  MODIFY payment_status ENUM('DUE','PARTIAL','PAID')
  NOT NULL DEFAULT 'DUE';

-- Post-migration verification:
-- SELECT status, COUNT(*) AS row_count
-- FROM sales_invoices
-- GROUP BY status
-- ORDER BY status;
--
-- SELECT payment_status, COUNT(*) AS row_count
-- FROM sales_invoices
-- GROUP BY payment_status
-- ORDER BY payment_status;
