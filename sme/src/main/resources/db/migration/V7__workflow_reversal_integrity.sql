-- Phase 6 workflow reversal integrity.
-- Adds safe reversal metadata and enum values for posted return/transfer reversal flows.

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

DELIMITER ;

CALL sme_add_column_if_missing('sales_returns', 'reversed_by', 'reversed_by VARCHAR(120) NULL');
CALL sme_add_column_if_missing('sales_returns', 'reversed_at', 'reversed_at DATETIME NULL');
CALL sme_add_column_if_missing('sales_returns', 'reversal_reason', 'reversal_reason VARCHAR(500) NULL');

CALL sme_add_column_if_missing('purchase_returns', 'reversed_by', 'reversed_by VARCHAR(120) NULL');
CALL sme_add_column_if_missing('purchase_returns', 'reversed_at', 'reversed_at DATETIME NULL');
CALL sme_add_column_if_missing('purchase_returns', 'reversal_reason', 'reversal_reason VARCHAR(500) NULL');

CALL sme_add_column_if_missing('stock_transfers', 'reversed_by', 'reversed_by VARCHAR(120) NULL');
CALL sme_add_column_if_missing('stock_transfers', 'reversed_at', 'reversed_at DATETIME NULL');
CALL sme_add_column_if_missing('stock_transfers', 'reversal_reason', 'reversal_reason VARCHAR(500) NULL');

CALL sme_add_column_if_missing('stock_adjustments', 'status', 'status ENUM(''DRAFT'',''APPROVED'',''POSTED'',''CANCELLED'',''REVERSED'') NOT NULL DEFAULT ''DRAFT''');
CALL sme_add_column_if_missing('stock_adjustments', 'created_at', 'created_at DATETIME NULL');
CALL sme_add_column_if_missing('stock_adjustments', 'approved_by', 'approved_by VARCHAR(120) NULL');
CALL sme_add_column_if_missing('stock_adjustments', 'approved_at', 'approved_at DATETIME NULL');
CALL sme_add_column_if_missing('stock_adjustments', 'posted_by', 'posted_by VARCHAR(120) NULL');
CALL sme_add_column_if_missing('stock_adjustments', 'posted_at', 'posted_at DATETIME NULL');
CALL sme_add_column_if_missing('stock_adjustments', 'cancelled_by', 'cancelled_by VARCHAR(120) NULL');
CALL sme_add_column_if_missing('stock_adjustments', 'cancelled_at', 'cancelled_at DATETIME NULL');
CALL sme_add_column_if_missing('stock_adjustments', 'reversed_by', 'reversed_by VARCHAR(120) NULL');
CALL sme_add_column_if_missing('stock_adjustments', 'reversed_at', 'reversed_at DATETIME NULL');
CALL sme_add_column_if_missing('stock_adjustments', 'reversal_reason', 'reversal_reason VARCHAR(500) NULL');

CALL sme_modify_column_if_exists('sales_returns', 'status', 'status ENUM(''DRAFT'',''SUBMITTED'',''APPROVED'',''REJECTED'',''POSTED'',''REVERSED'',''CANCELLED'') NOT NULL');
CALL sme_modify_column_if_exists('purchase_returns', 'status', 'status ENUM(''DRAFT'',''SUBMITTED'',''PENDING'',''APPROVED'',''REJECTED'',''PARTIAL_RECEIVED'',''RECEIVED'',''POSTED'',''PARTIAL_PAID'',''PAID'',''REVERSED'',''CANCELLED'') NOT NULL');
CALL sme_modify_column_if_exists('stock_transfers', 'status', 'status ENUM(''DRAFT'',''PENDING'',''APPROVED'',''IN_TRANSIT'',''RECEIVED'',''REVERSED'',''CANCELLED'') NOT NULL');
CALL sme_modify_column_if_exists('stock_adjustments', 'status', 'status ENUM(''DRAFT'',''APPROVED'',''POSTED'',''CANCELLED'',''REVERSED'') NOT NULL DEFAULT ''DRAFT''');
CALL sme_exec_if_table_exists('stock_adjustments', 'UPDATE stock_adjustments SET status = ''POSTED'', posted_at = COALESCE(posted_at, created_at, NOW()) WHERE status = ''DRAFT''');

CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''STOCK_ADJUSTMENT_POST'', ''STOCK_ADJUSTMENT'', ''POST'', ''STOCK ADJUSTMENT POST'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''STOCK_ADJUSTMENT_POST'')');
CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''STOCK_ADJUSTMENT_CANCEL'', ''STOCK_ADJUSTMENT'', ''CANCEL'', ''STOCK ADJUSTMENT CANCEL'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''STOCK_ADJUSTMENT_CANCEL'')');

DROP PROCEDURE IF EXISTS sme_exec_if_table_exists;
DROP PROCEDURE IF EXISTS sme_modify_column_if_exists;
DROP PROCEDURE IF EXISTS sme_add_column_if_missing;
