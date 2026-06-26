-- Phase 8 reporting permissions.
-- Idempotent permission seed for existing installations where the Java seeder already ran.

DELIMITER $$

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

CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''REPORT_VIEW'', ''REPORT'', ''VIEW'', ''REPORT VIEW'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''REPORT_VIEW'')');
CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''REPORT_EXPORT'', ''REPORT'', ''EXPORT'', ''REPORT EXPORT'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''REPORT_EXPORT'')');
CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''REPORT_EXPORT_CSV'', ''REPORT'', ''EXPORT_CSV'', ''REPORT EXPORT CSV'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''REPORT_EXPORT_CSV'')');
CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''REPORT_EXPORT_EXCEL'', ''REPORT'', ''EXPORT_EXCEL'', ''REPORT EXPORT EXCEL'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''REPORT_EXPORT_EXCEL'')');
CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''REPORT_EXPORT_PDF'', ''REPORT'', ''EXPORT_PDF'', ''REPORT EXPORT PDF'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''REPORT_EXPORT_PDF'')');

DROP PROCEDURE IF EXISTS sme_exec_if_table_exists;
