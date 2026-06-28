-- Invoice-led sales return metadata. Safe for existing returns through defaults/nullability.
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

CALL sme_add_column_if_missing('sales_returns', 'refund_method', 'refund_method VARCHAR(40) NOT NULL DEFAULT ''ADJUST_DUE''');
CALL sme_add_column_if_missing('sales_returns', 'cancellation_reason', 'cancellation_reason VARCHAR(500) NULL');
CALL sme_add_column_if_missing('sales_return_items', 'invoice_item_id', 'invoice_item_id BIGINT NULL');
CALL sme_add_column_if_missing('sales_return_items', 'discount_amount', 'discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0');
CALL sme_add_column_if_missing('sales_return_items', 'tax_amount', 'tax_amount DECIMAL(15,2) NOT NULL DEFAULT 0');
CALL sme_add_column_if_missing('sales_return_items', 'return_reason', 'return_reason VARCHAR(500) NULL');
CALL sme_add_column_if_missing('sales_return_items', 'item_condition', 'item_condition VARCHAR(40) NOT NULL DEFAULT ''RESELLABLE''');
CALL sme_add_column_if_missing('sales_return_items', 'restock', 'restock BOOLEAN NOT NULL DEFAULT TRUE');

DROP PROCEDURE IF EXISTS sme_add_column_if_missing;
