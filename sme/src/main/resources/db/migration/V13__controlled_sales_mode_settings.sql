-- Optional controlled-sales flags. Defaults preserve the Simple Retail/POS experience.
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
        WHERE table_schema = DATABASE() AND table_name = p_table_name AND column_name = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DELIMITER ;

CALL sme_add_column_if_missing('system_settings', 'enable_controlled_sales_mode', 'enable_controlled_sales_mode BOOLEAN NOT NULL DEFAULT FALSE');
CALL sme_add_column_if_missing('system_settings', 'enable_sales_orders', 'enable_sales_orders BOOLEAN NOT NULL DEFAULT FALSE');
CALL sme_add_column_if_missing('system_settings', 'enable_quotations', 'enable_quotations BOOLEAN NOT NULL DEFAULT FALSE');
CALL sme_add_column_if_missing('system_settings', 'enable_delivery_notes', 'enable_delivery_notes BOOLEAN NOT NULL DEFAULT FALSE');
CALL sme_add_column_if_missing('system_settings', 'enable_sales_approval', 'enable_sales_approval BOOLEAN NOT NULL DEFAULT FALSE');
CALL sme_add_column_if_missing('system_settings', 'enable_manual_allocation', 'enable_manual_allocation BOOLEAN NOT NULL DEFAULT FALSE');
CALL sme_add_column_if_missing('system_settings', 'enable_advanced_invoice', 'enable_advanced_invoice BOOLEAN NOT NULL DEFAULT FALSE');

DROP PROCEDURE IF EXISTS sme_add_column_if_missing;
