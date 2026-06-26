-- Phase 7 audit compliance history and retention metadata.
-- Safe for existing installations: adds nullable columns, indexes, and permissions only.

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

DELIMITER ;

CALL sme_add_column_if_missing('activity_logs', 'action_type', 'action_type VARCHAR(80) NULL');
CALL sme_add_column_if_missing('activity_logs', 'entity_name', 'entity_name VARCHAR(255) NULL');
CALL sme_add_column_if_missing('activity_logs', 'archived_at', 'archived_at DATETIME NULL');
CALL sme_add_column_if_missing('activity_logs', 'archive_reason', 'archive_reason VARCHAR(500) NULL');

CALL sme_exec_if_table_exists('activity_logs', 'UPDATE activity_logs SET entity_id = record_id WHERE entity_id IS NULL AND record_id IS NOT NULL');
CALL sme_exec_if_table_exists('activity_logs', 'UPDATE activity_logs SET entity_name = table_name WHERE entity_name IS NULL AND table_name IS NOT NULL');
CALL sme_exec_if_table_exists('activity_logs', 'UPDATE activity_logs SET action_type = CASE WHEN UPPER(action) LIKE ''%CREATE%'' THEN ''CREATE'' WHEN UPPER(action) LIKE ''%UPDATE%'' THEN ''UPDATE'' WHEN UPPER(action) LIKE ''%DELETE%'' THEN ''DELETE'' WHEN UPPER(action) LIKE ''%RESTORE%'' THEN ''RESTORE'' WHEN UPPER(action) LIKE ''%APPROVE%'' THEN ''APPROVE'' WHEN UPPER(action) LIKE ''%REJECT%'' THEN ''REJECT'' WHEN UPPER(action) LIKE ''%POST%'' THEN ''POST'' WHEN UPPER(action) LIKE ''%CANCEL%'' THEN ''CANCEL'' WHEN UPPER(action) LIKE ''%REVERSE%'' THEN ''REVERSE'' WHEN UPPER(action) LIKE ''%LOGIN%'' THEN ''LOGIN'' WHEN UPPER(action) LIKE ''%LOGOUT%'' THEN ''LOGOUT'' WHEN UPPER(action) LIKE ''%PASSWORD%'' THEN ''PASSWORD_CHANGE'' ELSE UPPER(action) END WHERE action_type IS NULL');

CALL sme_add_column_if_missing('audit_logs', 'archived_at', 'archived_at DATETIME NULL');
CALL sme_add_column_if_missing('audit_logs', 'archive_reason', 'archive_reason VARCHAR(500) NULL');
CALL sme_add_column_if_missing('login_history', 'archived_at', 'archived_at DATETIME NULL');
CALL sme_add_column_if_missing('login_history', 'archive_reason', 'archive_reason VARCHAR(500) NULL');

CALL sme_create_index_if_missing('activity_logs', 'idx_activity_logs_action_type', 'CREATE INDEX idx_activity_logs_action_type ON activity_logs (action_type)');
CALL sme_create_index_if_missing('activity_logs', 'idx_activity_logs_entity_history', 'CREATE INDEX idx_activity_logs_entity_history ON activity_logs (entity_name, entity_id)');
CALL sme_create_index_if_missing('activity_logs', 'idx_activity_logs_archived_at', 'CREATE INDEX idx_activity_logs_archived_at ON activity_logs (archived_at)');
CALL sme_create_index_if_missing('audit_logs', 'idx_audit_logs_archived_at', 'CREATE INDEX idx_audit_logs_archived_at ON audit_logs (archived_at)');
CALL sme_create_index_if_missing('login_history', 'idx_login_history_archived_at', 'CREATE INDEX idx_login_history_archived_at ON login_history (archived_at)');

CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''ACTIVITY_VIEW'', ''ACTIVITY'', ''VIEW'', ''ACTIVITY VIEW'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''ACTIVITY_VIEW'')');
CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''AUDIT_VIEW'', ''AUDIT'', ''VIEW'', ''AUDIT VIEW'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''AUDIT_VIEW'')');
CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''AUDIT_EXPORT'', ''AUDIT'', ''EXPORT'', ''AUDIT EXPORT'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''AUDIT_EXPORT'')');
CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''ACTIVITY_LOG_VIEW'', ''ACTIVITY_LOG'', ''VIEW'', ''ACTIVITY LOG VIEW'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''ACTIVITY_LOG_VIEW'')');
CALL sme_exec_if_table_exists('permissions', 'INSERT INTO permissions (name, module, action, description, created_at) SELECT ''ACTIVITY_LOG_EXPORT'', ''ACTIVITY_LOG'', ''EXPORT'', ''ACTIVITY LOG EXPORT'', NOW() WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = ''ACTIVITY_LOG_EXPORT'')');

DROP PROCEDURE IF EXISTS sme_exec_if_table_exists;
DROP PROCEDURE IF EXISTS sme_create_index_if_missing;
DROP PROCEDURE IF EXISTS sme_add_column_if_missing;
