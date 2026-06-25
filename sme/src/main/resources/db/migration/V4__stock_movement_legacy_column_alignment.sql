-- Stock movement legacy column alignment.
--
-- Authoritative JPA/API columns:
--   movement_type, reference_type, reference_no
--
-- Deprecated legacy columns, if present:
--   type, reference
--
-- This migration adds missing authoritative columns and backfills from legacy
-- columns only when both sides exist. It does not drop legacy data.

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

DELIMITER ;

CALL sme_add_column_if_missing('stock_movements', 'movement_type', 'movement_type VARCHAR(50) NULL');
CALL sme_add_column_if_missing('stock_movements', 'reference_type', 'reference_type VARCHAR(100) NULL');
CALL sme_add_column_if_missing('stock_movements', 'reference_no', 'reference_no VARCHAR(255) NULL');

CALL sme_exec_if_columns_exist('stock_movements', 'movement_type', 'type', 'UPDATE stock_movements SET movement_type = type WHERE movement_type IS NULL AND type IS NOT NULL');
CALL sme_exec_if_columns_exist('stock_movements', 'reference_no', 'reference', 'UPDATE stock_movements SET reference_no = reference WHERE reference_no IS NULL AND reference IS NOT NULL');

DROP PROCEDURE IF EXISTS sme_exec_if_columns_exist;
DROP PROCEDURE IF EXISTS sme_add_column_if_missing;
