-- Data-preserving compatibility migration converted from db/manual scripts.
-- All operations are conditional and skip absent tables/columns.

DELIMITER $$

DROP PROCEDURE IF EXISTS sme_add_column_if_missing $$
CREATE PROCEDURE sme_add_column_if_missing(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128),
    IN p_column_definition TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = p_table_name
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DROP PROCEDURE IF EXISTS sme_modify_column_if_exists $$
CREATE PROCEDURE sme_modify_column_if_exists(
    IN p_table_name VARCHAR(128),
    IN p_column_name VARCHAR(128),
    IN p_column_definition TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` MODIFY COLUMN ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DROP PROCEDURE IF EXISTS sme_exec_if_table_exists $$
CREATE PROCEDURE sme_exec_if_table_exists(
    IN p_table_name VARCHAR(128),
    IN p_sql TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = p_table_name
    ) THEN
        SET @ddl = p_sql;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DROP PROCEDURE IF EXISTS sme_exec_if_columns_exist $$
CREATE PROCEDURE sme_exec_if_columns_exist(
    IN p_table_name VARCHAR(128),
    IN p_column_a VARCHAR(128),
    IN p_column_b VARCHAR(128),
    IN p_sql TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = p_table_name AND column_name = p_column_a
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = p_table_name AND column_name = p_column_b
    ) THEN
        SET @ddl = p_sql;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DROP PROCEDURE IF EXISTS sme_create_index_if_missing $$
CREATE PROCEDURE sme_create_index_if_missing(
    IN p_table_name VARCHAR(128),
    IN p_index_name VARCHAR(128),
    IN p_sql TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = p_table_name
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @ddl = p_sql;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DELIMITER ;

CALL sme_add_column_if_missing('customers', 'customer_code', 'customer_code VARCHAR(255) NULL');
CALL sme_add_column_if_missing('customers', 'company_name', 'company_name VARCHAR(255) NULL');
CALL sme_add_column_if_missing('customers', 'contact_person', 'contact_person VARCHAR(255) NULL');
CALL sme_add_column_if_missing('customers', 'postal_code', 'postal_code VARCHAR(50) NULL');
CALL sme_add_column_if_missing('customers', 'credit_limit', 'credit_limit DECIMAL(15,2) NOT NULL DEFAULT 0');
CALL sme_add_column_if_missing('customers', 'opening_balance', 'opening_balance DECIMAL(15,2) NOT NULL DEFAULT 0');
CALL sme_add_column_if_missing('customers', 'current_balance', 'current_balance DECIMAL(15,2) NOT NULL DEFAULT 0');
CALL sme_add_column_if_missing('customers', 'tax_number', 'tax_number VARCHAR(100) NULL');
CALL sme_add_column_if_missing('customers', 'status', 'status VARCHAR(50) NULL');
CALL sme_add_column_if_missing('customers', 'is_deleted', 'is_deleted BOOLEAN NOT NULL DEFAULT FALSE');
CALL sme_add_column_if_missing('customers', 'created_by', 'created_by BIGINT NULL');
CALL sme_add_column_if_missing('customers', 'created_at', 'created_at DATETIME NULL');
CALL sme_add_column_if_missing('customers', 'updated_at', 'updated_at DATETIME NULL');
CALL sme_exec_if_table_exists('customers', 'UPDATE customers SET is_deleted = FALSE WHERE is_deleted IS NULL');
CALL sme_exec_if_table_exists('customers', 'UPDATE customers SET status = ''ACTIVE'' WHERE status IS NULL');
CALL sme_exec_if_table_exists('customers', 'UPDATE customers SET credit_limit = 0 WHERE credit_limit IS NULL');
CALL sme_exec_if_table_exists('customers', 'UPDATE customers SET opening_balance = 0 WHERE opening_balance IS NULL');
CALL sme_exec_if_table_exists('customers', 'UPDATE customers SET current_balance = COALESCE(opening_balance, 0) WHERE current_balance IS NULL');

CALL sme_add_column_if_missing('suppliers', 'supplier_code', 'supplier_code VARCHAR(50) NULL');
CALL sme_add_column_if_missing('suppliers', 'company_name', 'company_name VARCHAR(255) NULL');
CALL sme_add_column_if_missing('suppliers', 'contact_person', 'contact_person VARCHAR(255) NULL');
CALL sme_add_column_if_missing('suppliers', 'postal_code', 'postal_code VARCHAR(50) NULL');
CALL sme_add_column_if_missing('suppliers', 'opening_balance', 'opening_balance DECIMAL(15,2) NOT NULL DEFAULT 0');
CALL sme_add_column_if_missing('suppliers', 'current_balance', 'current_balance DECIMAL(15,2) NOT NULL DEFAULT 0');
CALL sme_add_column_if_missing('suppliers', 'tax_number', 'tax_number VARCHAR(100) NULL');
CALL sme_add_column_if_missing('suppliers', 'bank_account', 'bank_account VARCHAR(255) NULL');
CALL sme_add_column_if_missing('suppliers', 'payment_terms', 'payment_terms VARCHAR(255) NULL');
CALL sme_add_column_if_missing('suppliers', 'status', 'status VARCHAR(50) NULL');
CALL sme_add_column_if_missing('suppliers', 'is_deleted', 'is_deleted BOOLEAN NOT NULL DEFAULT FALSE');
CALL sme_add_column_if_missing('suppliers', 'created_by', 'created_by BIGINT NULL');
CALL sme_add_column_if_missing('suppliers', 'created_at', 'created_at DATETIME NULL');
CALL sme_add_column_if_missing('suppliers', 'updated_at', 'updated_at DATETIME NULL');
CALL sme_exec_if_table_exists('suppliers', 'UPDATE suppliers SET status = ''ACTIVE'' WHERE status IS NULL');
CALL sme_exec_if_table_exists('suppliers', 'UPDATE suppliers SET is_deleted = FALSE WHERE is_deleted IS NULL');
CALL sme_exec_if_table_exists('suppliers', 'UPDATE suppliers SET opening_balance = 0 WHERE opening_balance IS NULL');
CALL sme_exec_if_table_exists('suppliers', 'UPDATE suppliers SET current_balance = COALESCE(current_balance, opening_balance, 0) WHERE current_balance IS NULL');

CALL sme_add_column_if_missing('warehouses', 'warehouse_code', 'warehouse_code VARCHAR(255) NULL');
CALL sme_add_column_if_missing('warehouses', 'warehouse_name', 'warehouse_name VARCHAR(255) NULL');
CALL sme_add_column_if_missing('warehouses', 'location', 'location VARCHAR(255) NULL');
CALL sme_exec_if_columns_exist('warehouses', 'warehouse_code', 'code', 'UPDATE warehouses SET warehouse_code = code WHERE warehouse_code IS NULL AND code IS NOT NULL');
CALL sme_exec_if_columns_exist('warehouses', 'warehouse_name', 'name', 'UPDATE warehouses SET warehouse_name = name WHERE warehouse_name IS NULL AND name IS NOT NULL');
CALL sme_exec_if_columns_exist('warehouses', 'location', 'address', 'UPDATE warehouses SET location = address WHERE location IS NULL AND address IS NOT NULL');
CALL sme_exec_if_columns_exist('warehouses', 'code', 'warehouse_code', 'UPDATE warehouses SET code = warehouse_code WHERE code IS NULL AND warehouse_code IS NOT NULL');

CALL sme_add_column_if_missing('sales_orders', 'notes', 'notes VARCHAR(500) NULL');
CALL sme_add_column_if_missing('sales_orders', 'grand_total', 'grand_total DECIMAL(15,2) NOT NULL DEFAULT 0.00');
CALL sme_add_column_if_missing('sales_orders', 'updated_at', 'updated_at DATETIME NULL');
CALL sme_add_column_if_missing('sales_items', 'order_id', 'order_id BIGINT NULL');
CALL sme_modify_column_if_exists('sales_items', 'invoice_id', 'invoice_id BIGINT NULL');
CALL sme_create_index_if_missing('sales_items', 'idx_sales_items_order_id', 'CREATE INDEX idx_sales_items_order_id ON sales_items(order_id)');

DROP PROCEDURE IF EXISTS sme_create_index_if_missing;
DROP PROCEDURE IF EXISTS sme_exec_if_columns_exist;
DROP PROCEDURE IF EXISTS sme_exec_if_table_exists;
DROP PROCEDURE IF EXISTS sme_modify_column_if_exists;
DROP PROCEDURE IF EXISTS sme_add_column_if_missing;
