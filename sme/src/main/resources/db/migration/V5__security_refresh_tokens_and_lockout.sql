-- Phase 4 security hardening.
-- Adds persisted refresh tokens, access-token blacklist, and user lockout state.

CREATE TABLE IF NOT EXISTS auth_refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    replaced_by_token_hash VARCHAR(64) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_refresh_tokens_hash (token_hash),
    KEY idx_auth_refresh_tokens_user (user_id),
    KEY idx_auth_refresh_tokens_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS auth_blacklisted_access_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_blacklisted_access_tokens_hash (token_hash),
    KEY idx_auth_blacklisted_access_tokens_expires (expires_at)
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

CALL sme_add_column_if_missing('users', 'failed_login_attempts', 'failed_login_attempts INT NOT NULL DEFAULT 0');
CALL sme_add_column_if_missing('users', 'locked_until', 'locked_until DATETIME NULL');
CALL sme_add_column_if_missing('users', 'token_version', 'token_version INT NOT NULL DEFAULT 0');
CALL sme_exec_if_table_exists('users', 'UPDATE users SET failed_login_attempts = 0 WHERE failed_login_attempts IS NULL');
CALL sme_exec_if_table_exists('users', 'UPDATE users SET token_version = 0 WHERE token_version IS NULL');

DROP PROCEDURE IF EXISTS sme_exec_if_table_exists;
DROP PROCEDURE IF EXISTS sme_add_column_if_missing;
