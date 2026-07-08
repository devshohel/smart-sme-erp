-- Sales return approval workflow cleanup.
-- Do not run until reviewed and approved for the target database.
--
-- Pre-migration impact checks:
-- SELECT status, COUNT(*) AS row_count, COALESCE(SUM(total_amount), 0) AS total_amount
-- FROM sales_returns
-- GROUP BY status
-- ORDER BY status;
--
-- SELECT r.return_code, r.status, r.total_amount,
--        COUNT(DISTINCT sm.id) AS stock_movements,
--        COUNT(DISTINCT j.id) AS journals
-- FROM sales_returns r
-- LEFT JOIN stock_movements sm
--   ON sm.reference_type = 'SALES_RETURN'
--  AND sm.reference_no = r.return_code
-- LEFT JOIN accounting_journal_entries j
--   ON j.source_type = 'SALES_RETURN'
--  AND j.source_id = r.id
-- GROUP BY r.id, r.return_code, r.status, r.total_amount
-- ORDER BY r.return_code;
--
-- Safe mapping strategy:
-- 1. Existing APPROVED rows with SALES_RETURN stock movement or SALES_RETURN journal stay APPROVED final.
--    The application will not replay stock or journals for those rows.
-- 2. Existing APPROVED rows without stock movement and without journal become PENDING for review.
-- 3. DRAFT and SUBMITTED become PENDING.
-- 4. POSTED becomes APPROVED.
-- 5. CANCELLED and REVERSED become REJECTED.
--
-- Rollback consideration:
-- This migration collapses the historical return workflow statuses into a simplified
-- approval workflow. Recovering the exact previous status values is not deterministic
-- after execution unless a database backup or pre-migration status export exists.

ALTER TABLE sales_returns
  MODIFY status ENUM('PENDING','APPROVED','REJECTED','DRAFT','SUBMITTED','POSTED','REVERSED','CANCELLED')
  NOT NULL DEFAULT 'PENDING';

UPDATE sales_returns
SET status = 'PENDING'
WHERE status IN ('DRAFT', 'SUBMITTED');

UPDATE sales_returns
SET status = 'APPROVED'
WHERE status = 'POSTED';

UPDATE sales_returns
SET status = 'REJECTED'
WHERE status IN ('CANCELLED', 'REVERSED');

UPDATE sales_returns r
SET r.status = 'PENDING'
WHERE r.status = 'APPROVED'
  AND NOT EXISTS (
    SELECT 1
    FROM stock_movements sm
    WHERE sm.reference_type = 'SALES_RETURN'
      AND sm.reference_no = r.return_code
  )
  AND NOT EXISTS (
    SELECT 1
    FROM accounting_journal_entries j
    WHERE j.source_type = 'SALES_RETURN'
      AND j.source_id = r.id
  );

ALTER TABLE sales_returns
  MODIFY status ENUM('PENDING','APPROVED','REJECTED')
  NOT NULL DEFAULT 'PENDING';

INSERT INTO accounting_accounts (account_code, account_name, account_type, status)
SELECT '4010', 'Sales Returns and Allowances', 'EXPENSE', 'ACTIVE'
WHERE NOT EXISTS (
  SELECT 1 FROM accounting_accounts WHERE account_code = '4010' OR account_name = 'Sales Returns and Allowances'
);

INSERT INTO accounting_accounts (account_code, account_name, account_type, status)
SELECT '2110', 'Customer Credit', 'LIABILITY', 'ACTIVE'
WHERE NOT EXISTS (
  SELECT 1 FROM accounting_accounts WHERE account_code = '2110' OR account_name = 'Customer Credit'
);

INSERT INTO permissions (name, module, action, description, created_at)
SELECT 'SALES_RETURN_DELETE', 'SALES_RETURN', 'DELETE', 'SALES RETURN DELETE', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM permissions WHERE name = 'SALES_RETURN_DELETE'
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name = 'SALES_RETURN_DELETE'
WHERE r.role_name IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );

-- Post-migration verification:
-- SELECT status, COUNT(*) AS row_count, COALESCE(SUM(total_amount), 0) AS total_amount
-- FROM sales_returns
-- GROUP BY status
-- ORDER BY status;
--
-- SELECT r.return_code, r.status, r.total_amount,
--        COUNT(DISTINCT sm.id) AS stock_movements,
--        COUNT(DISTINCT j.id) AS journals
-- FROM sales_returns r
-- LEFT JOIN stock_movements sm
--   ON sm.reference_type = 'SALES_RETURN'
--  AND sm.reference_no = r.return_code
-- LEFT JOIN accounting_journal_entries j
--   ON j.source_type = 'SALES_RETURN'
--  AND j.source_id = r.id
-- GROUP BY r.id, r.return_code, r.status, r.total_amount
-- ORDER BY r.return_code;
