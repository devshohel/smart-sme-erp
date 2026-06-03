-- Smart SME ERP
-- Manual customer schema verification only. Do NOT run automatically.
-- File: 2026_05_19_customer_schema_check.sql
--
-- Purpose:
-- 1. Verify the customers table matches the final backend contract.
-- 2. Safely backfill nullable defaults where legacy rows may be incomplete.
-- 3. Avoid destructive changes and preserve all existing customer data.

USE sme;

-- =========================================================
-- 0. PRE-CHECKS
-- =========================================================

SELECT
    table_name,
    column_name,
    is_nullable,
    column_type,
    column_default,
    extra
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'customers'
ORDER BY ordinal_position;

SELECT
    id,
    customer_code,
    name,
    email,
    phone,
    status,
    is_deleted,
    credit_limit,
    opening_balance,
    current_balance
FROM customers
ORDER BY id
LIMIT 20;

SELECT customer_code, COUNT(*) AS duplicate_count
FROM customers
WHERE customer_code IS NOT NULL
GROUP BY customer_code
HAVING COUNT(*) > 1;

SELECT email, COUNT(*) AS duplicate_count
FROM customers
WHERE email IS NOT NULL AND email <> ''
GROUP BY email
HAVING COUNT(*) > 1;

SELECT phone, COUNT(*) AS duplicate_count
FROM customers
WHERE phone IS NOT NULL AND phone <> ''
GROUP BY phone
HAVING COUNT(*) > 1;

-- =========================================================
-- 1. SAFE BACKFILL OPTIONS
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
SET current_balance = opening_balance
WHERE current_balance IS NULL;

-- =========================================================
-- 2. OPTIONAL CONTRACT ENFORCEMENT
-- =========================================================
-- Review pre-check output carefully before uncommenting.
--
-- ALTER TABLE customers
--     MODIFY customer_code VARCHAR(255) NOT NULL,
--     MODIFY name VARCHAR(255) NOT NULL,
--     MODIFY credit_limit DECIMAL(15,2) NOT NULL DEFAULT 0,
--     MODIFY opening_balance DECIMAL(15,2) NOT NULL DEFAULT 0,
--     MODIFY current_balance DECIMAL(15,2) NOT NULL DEFAULT 0,
--     MODIFY status VARCHAR(50) NOT NULL,
--     MODIFY is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
--
-- ALTER TABLE customers
--     ADD CONSTRAINT uk_customers_customer_code UNIQUE (customer_code);
--
-- Exact runtime issue confirmed on 2026-05-19:
-- The live customers.id column exists but is not AUTO_INCREMENT, which causes:
-- "Field 'id' doesn't have a default value" on POST /api/v1/customers.
--
-- Apply only after confirming SHOW COLUMNS FROM customers shows Extra is empty for id:
-- ALTER TABLE customers
--     MODIFY id BIGINT NOT NULL AUTO_INCREMENT;
--
-- If the customer module still throws "unknown column" or similar SQL errors,
-- review and apply only the missing statements below one by one.
--
-- ALTER TABLE customers ADD COLUMN customer_code VARCHAR(255) NULL;
-- ALTER TABLE customers ADD COLUMN company_name VARCHAR(255) NULL;
-- ALTER TABLE customers ADD COLUMN contact_person VARCHAR(255) NULL;
-- ALTER TABLE customers ADD COLUMN postal_code VARCHAR(50) NULL;
-- ALTER TABLE customers ADD COLUMN credit_limit DECIMAL(15,2) NOT NULL DEFAULT 0;
-- ALTER TABLE customers ADD COLUMN opening_balance DECIMAL(15,2) NOT NULL DEFAULT 0;
-- ALTER TABLE customers ADD COLUMN current_balance DECIMAL(15,2) NOT NULL DEFAULT 0;
-- ALTER TABLE customers ADD COLUMN tax_number VARCHAR(100) NULL;
-- ALTER TABLE customers ADD COLUMN status VARCHAR(50) NULL;
-- ALTER TABLE customers ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
-- ALTER TABLE customers ADD COLUMN created_by BIGINT NULL;
-- ALTER TABLE customers ADD COLUMN created_at DATETIME NULL;
-- ALTER TABLE customers ADD COLUMN updated_at DATETIME NULL;

-- =========================================================
-- 3. POST-CHECKS
-- =========================================================

SELECT
    COUNT(*) AS null_status_rows
FROM customers
WHERE status IS NULL;

SELECT
    COUNT(*) AS null_deleted_rows
FROM customers
WHERE is_deleted IS NULL;

SELECT
    COUNT(*) AS null_current_balance_rows
FROM customers
WHERE current_balance IS NULL;
