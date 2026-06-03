-- Manual purchase schema/data safety checklist for Smart SME ERP.
-- Review before applying in any environment. Do not run blindly.

-- 1. Inspect purchase table shapes.
SHOW COLUMNS FROM purchase_orders;
SHOW COLUMNS FROM purchase_items;
SHOW COLUMNS FROM purchase_returns;
SHOW COLUMNS FROM purchase_return_items;

-- 2. Check duplicate business identifiers.
SELECT purchase_code, COUNT(*) AS duplicate_count
FROM purchase_orders
WHERE purchase_code IS NOT NULL AND purchase_code <> ''
GROUP BY purchase_code
HAVING COUNT(*) > 1;

SELECT return_code, COUNT(*) AS duplicate_count
FROM purchase_returns
WHERE return_code IS NOT NULL AND return_code <> ''
GROUP BY return_code
HAVING COUNT(*) > 1;

-- 3. Check nullable statuses and amount fields that should be normalized.
SELECT id, purchase_code, status, total_amount, discount_amount, tax_amount, net_total, paid_amount, due_amount
FROM purchase_orders
WHERE status IS NULL
   OR total_amount IS NULL
   OR discount_amount IS NULL
   OR tax_amount IS NULL
   OR net_total IS NULL
   OR paid_amount IS NULL
   OR due_amount IS NULL;

SELECT id, return_code, total_amount
FROM purchase_returns
WHERE total_amount IS NULL;

SELECT id, purchase_id, product_id, quantity, unit_price, discount, tax, sub_total
FROM purchase_items
WHERE quantity IS NULL
   OR unit_price IS NULL
   OR discount IS NULL
   OR tax IS NULL
   OR sub_total IS NULL;

SELECT id, return_id, product_id, quantity, unit_price, total
FROM purchase_return_items
WHERE quantity IS NULL
   OR unit_price IS NULL
   OR total IS NULL;

-- 4. Optional safe backfill for legacy nulls.
-- UPDATE purchase_orders SET status = 'PENDING' WHERE status IS NULL;
-- UPDATE purchase_orders SET total_amount = 0 WHERE total_amount IS NULL;
-- UPDATE purchase_orders SET discount_amount = 0 WHERE discount_amount IS NULL;
-- UPDATE purchase_orders SET tax_amount = 0 WHERE tax_amount IS NULL;
-- UPDATE purchase_orders SET net_total = 0 WHERE net_total IS NULL;
-- UPDATE purchase_orders SET paid_amount = 0 WHERE paid_amount IS NULL;
-- UPDATE purchase_orders SET due_amount = 0 WHERE due_amount IS NULL;
-- UPDATE purchase_returns SET total_amount = 0 WHERE total_amount IS NULL;
-- UPDATE purchase_items SET discount = 0 WHERE discount IS NULL;
-- UPDATE purchase_items SET tax = 0 WHERE tax IS NULL;

-- 5. Optional schema creation/alignment examples. Verify first before applying.
-- CREATE TABLE purchase_orders (
--   id INT PRIMARY KEY AUTO_INCREMENT,
--   purchase_code VARCHAR(50) UNIQUE,
--   supplier_id INT,
--   warehouse_id INT,
--   purchase_date DATETIME,
--   total_amount DECIMAL(15,2),
--   discount_amount DECIMAL(15,2),
--   tax_amount DECIMAL(15,2),
--   net_total DECIMAL(15,2),
--   paid_amount DECIMAL(15,2),
--   due_amount DECIMAL(15,2),
--   status VARCHAR(50),
--   created_by BIGINT,
--   created_at DATETIME,
--   updated_at DATETIME
-- );

-- CREATE TABLE purchase_items (
--   id INT PRIMARY KEY AUTO_INCREMENT,
--   purchase_id INT,
--   product_id INT,
--   uom_id INT,
--   quantity DECIMAL(15,2),
--   unit_price DECIMAL(15,2),
--   discount DECIMAL(15,2),
--   tax DECIMAL(15,2),
--   sub_total DECIMAL(15,2)
-- );

-- CREATE TABLE purchase_returns (
--   id INT PRIMARY KEY AUTO_INCREMENT,
--   return_code VARCHAR(50) UNIQUE,
--   purchase_id INT,
--   supplier_id INT,
--   return_date DATETIME,
--   total_amount DECIMAL(15,2),
--   created_by BIGINT,
--   created_at DATETIME
-- );

-- CREATE TABLE purchase_return_items (
--   id INT PRIMARY KEY AUTO_INCREMENT,
--   return_id INT,
--   product_id INT,
--   quantity DECIMAL(15,2),
--   unit_price DECIMAL(15,2),
--   total DECIMAL(15,2)
-- );
