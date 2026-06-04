-- Smart SME ERP
-- Manual customers table compatibility fix.
--
-- IMPORTANT:
-- 1. Take a database backup before running any ALTER TABLE statement.
-- 2. Run the PRE-CHECK section first in MySQL Workbench.
-- 3. Apply only the FIX section statements that match the pre-check output.
-- 4. This script touches the customers table only and does not drop columns.

USE sme;

-- =========================================================
-- PRE-CHECK: confirm current customers table contract
-- =========================================================

SELECT
    column_name,
    column_type,
    is_nullable,
    column_default,
    extra
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'customers'
ORDER BY ordinal_position;

SELECT
    index_name,
    non_unique,
    GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'customers'
GROUP BY index_name, non_unique
ORDER BY index_name;

SELECT
    COUNT(*) AS total_rows,
    SUM(id IS NULL) AS null_id_rows,
    SUM(customer_code IS NULL OR customer_code = '') AS blank_code_rows,
    SUM(name IS NULL OR name = '') AS blank_name_rows,
    SUM(status IS NULL) AS null_status_rows,
    SUM(is_deleted IS NULL) AS null_deleted_rows,
    SUM(credit_limit IS NULL) AS null_credit_limit_rows,
    SUM(opening_balance IS NULL) AS null_opening_balance_rows,
    SUM(current_balance IS NULL) AS null_current_balance_rows
FROM customers;

SELECT customer_code, COUNT(*) AS duplicate_count
FROM customers
WHERE customer_code IS NOT NULL AND customer_code <> ''
GROUP BY customer_code
HAVING COUNT(*) > 1;

-- =========================================================
-- FIX: safe customer-table normalization
-- =========================================================

UPDATE customers
SET is_deleted = FALSE
WHERE is_deleted IS NULL;

UPDATE customers
SET status = 'ACTIVE'
WHERE status IS NULL;

UPDATE customers
SET credit_limit = 0
WHERE credit_limit IS NULL;

UPDATE customers
SET opening_balance = 0
WHERE opening_balance IS NULL;

UPDATE customers
SET current_balance = COALESCE(opening_balance, 0)
WHERE current_balance IS NULL;

-- Confirm the PRE-CHECK shows:
-- column_name = id
-- column_type = bigint
-- is_nullable = NO
-- extra does NOT contain auto_increment
--
-- Runtime error fixed by this statement:
-- Field 'id' doesn't have a default value
ALTER TABLE customers
    MODIFY id BIGINT NOT NULL AUTO_INCREMENT;

-- Optional only if PRE-CHECK shows is_deleted remains nullable and you want
-- the database default to match the backend default.
ALTER TABLE customers
    MODIFY is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- =========================================================
-- POST-CHECK
-- =========================================================

SELECT
    column_name,
    column_type,
    is_nullable,
    column_default,
    extra
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'customers'
  AND column_name IN ('id', 'is_deleted', 'status', 'credit_limit', 'opening_balance', 'current_balance')
ORDER BY ordinal_position;

SELECT
    COUNT(*) AS null_deleted_rows
FROM customers
WHERE is_deleted IS NULL;
