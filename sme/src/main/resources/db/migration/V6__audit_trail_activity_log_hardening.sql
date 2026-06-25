-- Phase 5 audit trail hardening.
-- Data-preserving migration for activity log metadata, audit columns, and activity permissions.

CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    username VARCHAR(255) NULL,
    action VARCHAR(255) NOT NULL,
    module VARCHAR(255) NOT NULL,
    table_name VARCHAR(255) NULL,
    record_id BIGINT NULL,
    entity_id BIGINT NULL,
    old_value LONGTEXT NULL,
    new_value LONGTEXT NULL,
    ip_address VARCHAR(255) NULL,
    user_agent VARCHAR(1000) NULL,
    details TEXT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_activity_logs_created_at (created_at),
    KEY idx_activity_logs_user_id (user_id),
    KEY idx_activity_logs_username (username),
    KEY idx_activity_logs_action (action),
    KEY idx_activity_logs_module (module)
);

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

DROP PROCEDURE IF EXISTS sme_add_audit_columns $$
CREATE PROCEDURE sme_add_audit_columns(IN p_table_name VARCHAR(128))
BEGIN
    CALL sme_add_column_if_missing(p_table_name, 'created_at', 'created_at DATETIME NULL');
    CALL sme_add_column_if_missing(p_table_name, 'created_by', 'created_by VARCHAR(120) NULL');
    CALL sme_add_column_if_missing(p_table_name, 'updated_at', 'updated_at DATETIME NULL');
    CALL sme_add_column_if_missing(p_table_name, 'updated_by', 'updated_by VARCHAR(120) NULL');
    CALL sme_add_column_if_missing(p_table_name, 'deleted_at', 'deleted_at DATETIME NULL');
    CALL sme_add_column_if_missing(p_table_name, 'deleted_by', 'deleted_by VARCHAR(120) NULL');
    CALL sme_add_column_if_missing(p_table_name, 'restored_at', 'restored_at DATETIME NULL');
    CALL sme_add_column_if_missing(p_table_name, 'restored_by', 'restored_by VARCHAR(120) NULL');
END $$

DELIMITER ;

CALL sme_add_column_if_missing('activity_logs', 'username', 'username VARCHAR(255) NULL');
CALL sme_add_column_if_missing('activity_logs', 'entity_id', 'entity_id BIGINT NULL');
CALL sme_add_column_if_missing('activity_logs', 'old_value', 'old_value LONGTEXT NULL');
CALL sme_add_column_if_missing('activity_logs', 'new_value', 'new_value LONGTEXT NULL');
CALL sme_add_column_if_missing('activity_logs', 'user_agent', 'user_agent VARCHAR(1000) NULL');

UPDATE activity_logs SET entity_id = record_id WHERE entity_id IS NULL AND record_id IS NOT NULL;

CALL sme_create_index_if_missing('activity_logs', 'idx_activity_logs_created_at', 'CREATE INDEX idx_activity_logs_created_at ON activity_logs (created_at)');
CALL sme_create_index_if_missing('activity_logs', 'idx_activity_logs_username', 'CREATE INDEX idx_activity_logs_username ON activity_logs (username)');
CALL sme_create_index_if_missing('activity_logs', 'idx_activity_logs_action', 'CREATE INDEX idx_activity_logs_action ON activity_logs (action)');
CALL sme_create_index_if_missing('activity_logs', 'idx_activity_logs_module', 'CREATE INDEX idx_activity_logs_module ON activity_logs (module)');

CALL sme_add_audit_columns('products');
CALL sme_add_audit_columns('product_categories');
CALL sme_add_audit_columns('product_brands');
CALL sme_add_audit_columns('uoms');
CALL sme_add_audit_columns('customers');
CALL sme_add_audit_columns('suppliers');
CALL sme_add_audit_columns('warehouses');
CALL sme_add_audit_columns('users');
CALL sme_add_audit_columns('accounting_expense_categories');
CALL sme_add_audit_columns('purchase_orders');
CALL sme_add_audit_columns('purchase_returns');
CALL sme_add_audit_columns('sales_orders');
CALL sme_add_audit_columns('sales_invoices');
CALL sme_add_audit_columns('sales_returns');
CALL sme_add_audit_columns('accounting_expenses');
CALL sme_add_audit_columns('supplier_payments');
CALL sme_add_audit_columns('customer_receipts');
CALL sme_add_audit_columns('stock_adjustments');
CALL sme_add_audit_columns('stock_transfers');

CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''ACTIVITY_LOG_VIEW'', ''ACTIVITY_LOG'', ''VIEW'', ''ACTIVITY LOG VIEW'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''ACTIVITY_LOG_VIEW'')');
CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''ACTIVITY_LOG_EXPORT'', ''ACTIVITY_LOG'', ''EXPORT'', ''ACTIVITY LOG EXPORT'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''ACTIVITY_LOG_EXPORT'')');

DROP PROCEDURE IF EXISTS sme_add_audit_columns;
DROP PROCEDURE IF EXISTS sme_exec_if_table_exists;
DROP PROCEDURE IF EXISTS sme_create_index_if_missing;
DROP PROCEDURE IF EXISTS sme_add_column_if_missing;
