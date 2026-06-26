-- Removes unsupported reverse workflow permissions from upgraded databases.
-- Historical status values and audit rows are preserved for existing records.

DELIMITER $$

DROP PROCEDURE IF EXISTS sme_exec_if_tables_exist $$
CREATE PROCEDURE sme_exec_if_tables_exist(
    IN p_table_one VARCHAR(128),
    IN p_table_two VARCHAR(128),
    IN p_sql TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = p_table_one
    ) AND EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = p_table_two
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

DELIMITER ;

CALL sme_exec_if_tables_exist(
    'role_permissions',
    'permissions',
    'DELETE rp FROM role_permissions rp JOIN permissions p ON p.id = rp.permission_id WHERE p.name IN (''SALES_INVOICE_REVERSE'',''SALES_RETURN_REVERSE'',''PURCHASE_INVOICE_REVERSE'',''PURCHASE_RETURN_REVERSE'',''TRANSFER_REVERSE'',''STOCK_ADJUSTMENT_REVERSE'',''EXPENSE_REVERSE'')'
);

CALL sme_exec_if_table_exists(
    'permissions',
    'DELETE FROM permissions WHERE name IN (''SALES_INVOICE_REVERSE'',''SALES_RETURN_REVERSE'',''PURCHASE_INVOICE_REVERSE'',''PURCHASE_RETURN_REVERSE'',''TRANSFER_REVERSE'',''STOCK_ADJUSTMENT_REVERSE'',''EXPENSE_REVERSE'')'
);

DROP PROCEDURE IF EXISTS sme_exec_if_table_exists;
DROP PROCEDURE IF EXISTS sme_exec_if_tables_exist;
