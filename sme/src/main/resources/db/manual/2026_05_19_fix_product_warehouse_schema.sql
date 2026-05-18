-- Smart SME ERP
-- Manual migration only. Do NOT run automatically from the application.
-- File: 2026_05_19_fix_product_warehouse_schema.sql
--
-- Purpose:
-- 1. Clean broken product foreign-key references that crash GET /api/v1/products
-- 2. Align live warehouses data toward the final contract:
--    warehouse_code, warehouse_name, location
--
-- IMPORTANT
-- - Take a full backup before running this script.
-- - Review every pre-check query result before executing the related UPDATE/ALTER step.
-- - Run in a maintenance window if this database is shared.
-- - This script avoids dropping legacy columns blindly.
--
-- Preferred backup command outside MySQL:
-- mysqldump -u root -p sme > sme_backup_before_2026_05_19_fix.sql


-- =========================================================
-- 0. PRE-CHECKS
-- =========================================================

USE sme;

-- Confirm current warehouses schema
SELECT
    table_name,
    column_name,
    is_nullable,
    column_type,
    column_default
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'warehouses'
ORDER BY ordinal_position;

-- Confirm current products schema
SELECT
    table_name,
    column_name,
    is_nullable,
    column_type,
    column_default
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'products'
ORDER BY ordinal_position;

-- Check broken product -> category references
SELECT
    p.id,
    p.product_code,
    p.product_name,
    p.category_id
FROM products p
LEFT JOIN product_categories c
    ON p.category_id = c.id
WHERE p.category_id IS NOT NULL
  AND c.id IS NULL
ORDER BY p.id;

-- Check old nullable soft-delete/status values in product-side master tables
SELECT 'products' AS table_name, COUNT(*) AS null_is_deleted_rows
FROM products
WHERE is_deleted IS NULL
UNION ALL
SELECT 'product_categories' AS table_name, COUNT(*) AS null_is_deleted_rows
FROM product_categories
WHERE is_deleted IS NULL
UNION ALL
SELECT 'product_brands' AS table_name, COUNT(*) AS null_is_deleted_rows
FROM product_brands
WHERE is_deleted IS NULL;

SELECT 'products' AS table_name, COUNT(*) AS null_status_rows
FROM products
WHERE status IS NULL
UNION ALL
SELECT 'product_categories' AS table_name, COUNT(*) AS null_status_rows
FROM product_categories
WHERE status IS NULL
UNION ALL
SELECT 'product_brands' AS table_name, COUNT(*) AS null_status_rows
FROM product_brands
WHERE status IS NULL
UNION ALL
SELECT 'uoms' AS table_name, COUNT(*) AS null_status_rows
FROM uoms
WHERE status IS NULL;

-- Optional check for nullable warehouse active flag if that column exists in the live DB.
-- Comment out if the live warehouses table does not have `active`.
SELECT COUNT(*) AS null_warehouse_active_rows
FROM warehouses
WHERE active IS NULL;

-- Optional: check broken product -> brand references
SELECT
    p.id,
    p.product_code,
    p.product_name,
    p.brand_id
FROM products p
LEFT JOIN product_brands b
    ON p.brand_id = b.id
WHERE p.brand_id IS NOT NULL
  AND b.id IS NULL
ORDER BY p.id;

-- Optional: check broken product -> uom references
SELECT
    p.id,
    p.product_code,
    p.product_name,
    p.uom_id
FROM products p
LEFT JOIN uoms u
    ON p.uom_id = u.id
WHERE p.uom_id IS NOT NULL
  AND u.id IS NULL
ORDER BY p.id;

-- Preview warehouses data with both final and possible legacy columns.
-- If some legacy columns do not exist, MySQL will error on this SELECT.
-- In that case, inspect the schema result above and adjust manually.
SELECT
    id,
    warehouse_code,
    warehouse_name,
    location,
    code,
    name,
    address
FROM warehouses
LIMIT 20;


-- =========================================================
-- 1. PRODUCT REFERENCE CLEANUP
-- =========================================================

-- Product rows must not point to missing category rows.
-- We preserve the product row and null the invalid reference only.
UPDATE products p
LEFT JOIN product_categories c
    ON p.category_id = c.id
SET p.category_id = NULL
WHERE p.category_id IS NOT NULL
  AND c.id IS NULL;

-- Optional but recommended: clean broken brand references too.
UPDATE products p
LEFT JOIN product_brands b
    ON p.brand_id = b.id
SET p.brand_id = NULL
WHERE p.brand_id IS NOT NULL
  AND b.id IS NULL;

-- Optional but recommended: clean broken UOM references too.
UPDATE products p
LEFT JOIN uoms u
    ON p.uom_id = u.id
SET p.uom_id = NULL
WHERE p.uom_id IS NOT NULL
  AND u.id IS NULL;

-- Normalize nullable soft-delete/status values so old rows behave consistently.
UPDATE products
SET is_deleted = FALSE
WHERE is_deleted IS NULL;

UPDATE product_categories
SET is_deleted = FALSE
WHERE is_deleted IS NULL;

UPDATE product_brands
SET is_deleted = FALSE
WHERE is_deleted IS NULL;

UPDATE products
SET status = 'ACTIVE'
WHERE status IS NULL;

UPDATE product_categories
SET status = 'ACTIVE'
WHERE status IS NULL;

UPDATE product_brands
SET status = 'ACTIVE'
WHERE status IS NULL;

UPDATE uoms
SET status = 'ACTIVE'
WHERE status IS NULL;

-- Verify cleanup results
SELECT
    p.id,
    p.product_code,
    p.product_name,
    p.category_id
FROM products p
LEFT JOIN product_categories c
    ON p.category_id = c.id
WHERE p.category_id IS NOT NULL
  AND c.id IS NULL
ORDER BY p.id;


-- =========================================================
-- 2. WAREHOUSE SCHEMA ALIGNMENT
-- =========================================================

-- Step 2.1: add final-contract columns if missing.
-- MySQL 8.0 supports IF NOT EXISTS for ADD COLUMN.
ALTER TABLE warehouses
    ADD COLUMN IF NOT EXISTS warehouse_code VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS warehouse_name VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS location VARCHAR(255) NULL;

-- Step 2.2: backfill final columns from legacy columns if they exist.
-- These UPDATE statements assume legacy columns code/name/address exist.
-- If a column does not exist in your live DB, comment out that statement manually.
UPDATE warehouses
SET warehouse_code = code
WHERE warehouse_code IS NULL
  AND code IS NOT NULL;

UPDATE warehouses
SET warehouse_name = name
WHERE warehouse_name IS NULL
  AND name IS NOT NULL;

UPDATE warehouses
SET location = address
WHERE location IS NULL
  AND address IS NOT NULL;

-- Step 2.3: if legacy code is still required by the live table design,
-- keep it populated from warehouse_code for backward safety.
-- If the live DB does not have legacy code, comment this out manually.
UPDATE warehouses
SET code = warehouse_code
WHERE code IS NULL
  AND warehouse_code IS NOT NULL;

-- Step 2.3b: normalize nullable active values for old rows if the live table has `active`.
-- Comment out if the live warehouses table does not have `active`.
UPDATE warehouses
SET active = TRUE
WHERE active IS NULL;

-- Step 2.4: verify no rows are still missing final required values.
SELECT
    id,
    warehouse_code,
    warehouse_name,
    location
FROM warehouses
WHERE warehouse_code IS NULL
   OR warehouse_name IS NULL
ORDER BY id;

-- Step 2.5: make final-contract columns required only after verifying backfill.
ALTER TABLE warehouses
    MODIFY warehouse_code VARCHAR(255) NOT NULL,
    MODIFY warehouse_name VARCHAR(255) NOT NULL;

-- Step 2.6: if legacy code column exists and is currently NOT NULL,
-- it can continue to block inserts unless it is also kept synchronized or relaxed.
-- Safest immediate option: make legacy code nullable if it still exists.
--
-- IMPORTANT:
-- - Uncomment ONLY if your pre-check shows a legacy `code` column.
-- - If another legacy process still depends on code being NOT NULL, keep using the sync UPDATE above instead.
--
-- ALTER TABLE warehouses
--     MODIFY code VARCHAR(255) NULL;

-- Step 2.7: optional safe unique index for final warehouse_code.
-- Run only after checking for duplicates.
SELECT warehouse_code, COUNT(*) AS duplicate_count
FROM warehouses
WHERE warehouse_code IS NOT NULL
GROUP BY warehouse_code
HAVING COUNT(*) > 1;

-- If no duplicates are returned, you may add a unique index:
-- ALTER TABLE warehouses
--     ADD CONSTRAINT uk_warehouses_warehouse_code UNIQUE (warehouse_code);


-- =========================================================
-- 3. POST-CHECKS
-- =========================================================

-- Product rows should no longer point to missing categories.
SELECT COUNT(*) AS broken_product_category_refs
FROM products p
LEFT JOIN product_categories c
    ON p.category_id = c.id
WHERE p.category_id IS NOT NULL
  AND c.id IS NULL;

-- Warehouse final columns should now be populated.
SELECT
    id,
    warehouse_code,
    warehouse_name,
    location
FROM warehouses
ORDER BY id
LIMIT 20;

-- Confirm final warehouse schema after migration
SELECT
    table_name,
    column_name,
    is_nullable,
    column_type,
    column_default
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'warehouses'
ORDER BY ordinal_position;


-- =========================================================
-- 4. ROLLBACK NOTES
-- =========================================================

-- This script is intentionally data-preserving and avoids DROP COLUMN.
-- Practical rollback strategy:
-- 1. Restore from full backup if the migration causes broader issues.
-- 2. If only product reference cleanup must be rolled back, restore the affected rows from backup.
-- 3. If warehouse backfill needs rollback, you can copy values back from legacy columns manually if they still exist.
-- 4. Do not drop legacy columns until the backend and runtime are fully verified against final-contract columns.
