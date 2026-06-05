-- Manual supplier schema/data safety checklist for Smart SME ERP.
-- Review before applying in any environment. Do not run blindly.

-- 1. Inspect supplier table shape.
SHOW COLUMNS FROM suppliers;

SELECT
    column_name,
    column_type,
    is_nullable,
    column_key,
    column_default,
    extra
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'suppliers'
ORDER BY ordinal_position;

-- 2. Check for duplicate values that would block unique business rules.
SELECT supplier_code, COUNT(*) AS duplicate_count
FROM suppliers
WHERE supplier_code IS NOT NULL AND supplier_code <> ''
GROUP BY supplier_code
HAVING COUNT(*) > 1;

SELECT email, COUNT(*) AS duplicate_count
FROM suppliers
WHERE email IS NOT NULL AND email <> ''
GROUP BY email
HAVING COUNT(*) > 1;

SELECT phone, COUNT(*) AS duplicate_count
FROM suppliers
WHERE phone IS NOT NULL AND phone <> ''
GROUP BY phone
HAVING COUNT(*) > 1;

-- 3. Check legacy nullable values that should be normalized.
SELECT id, supplier_code, status, is_deleted, opening_balance, current_balance
FROM suppliers
WHERE status IS NULL
   OR is_deleted IS NULL
   OR opening_balance IS NULL
   OR current_balance IS NULL;

-- 4. Optional safe backfill for legacy nulls.
-- UPDATE suppliers SET status = 'ACTIVE' WHERE status IS NULL;
-- UPDATE suppliers SET is_deleted = false WHERE is_deleted IS NULL;
-- UPDATE suppliers SET opening_balance = 0 WHERE opening_balance IS NULL;
-- UPDATE suppliers
-- SET current_balance = COALESCE(current_balance, opening_balance, 0)
-- WHERE current_balance IS NULL;

-- 5. Optional schema alignment if columns are missing in older databases.
-- Verify first with SHOW COLUMNS output before applying any ALTER TABLE.
-- ALTER TABLE suppliers ADD COLUMN supplier_code VARCHAR(50) NULL;
-- ALTER TABLE suppliers ADD COLUMN company_name VARCHAR(255) NULL;
-- ALTER TABLE suppliers ADD COLUMN contact_person VARCHAR(255) NULL;
-- ALTER TABLE suppliers ADD COLUMN postal_code VARCHAR(50) NULL;
-- ALTER TABLE suppliers ADD COLUMN opening_balance DECIMAL(15,2) NOT NULL DEFAULT 0;
-- ALTER TABLE suppliers ADD COLUMN current_balance DECIMAL(15,2) NOT NULL DEFAULT 0;
-- ALTER TABLE suppliers ADD COLUMN tax_number VARCHAR(100) NULL;
-- ALTER TABLE suppliers ADD COLUMN bank_account VARCHAR(255) NULL;
-- ALTER TABLE suppliers ADD COLUMN payment_terms VARCHAR(255) NULL;
-- ALTER TABLE suppliers ADD COLUMN status VARCHAR(50) NULL;
-- ALTER TABLE suppliers ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT false;
-- ALTER TABLE suppliers ADD COLUMN created_by BIGINT NULL;
-- ALTER TABLE suppliers ADD COLUMN created_at DATETIME NULL;
-- ALTER TABLE suppliers ADD COLUMN updated_at DATETIME NULL;

-- 6. Optional ID generation alignment.
-- Supplier.id uses GenerationType.IDENTITY, so MySQL must generate the id.
-- Apply only if the column inspection shows suppliers.id Extra does not contain auto_increment.
-- If MySQL reports Error 1833, inspect and temporarily drop foreign keys that reference suppliers.id,
-- modify the id column, then recreate those foreign keys.
--
-- ALTER TABLE suppliers
--     MODIFY id BIGINT NOT NULL AUTO_INCREMENT;

-- 7. Post-check for ID generation.
SELECT
    column_name,
    column_type,
    is_nullable,
    column_key,
    column_default,
    extra
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'suppliers'
  AND column_name = 'id';
