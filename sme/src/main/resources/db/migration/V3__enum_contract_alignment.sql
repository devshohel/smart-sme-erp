-- Align MySQL enum contracts with Java and Angular enum values.
-- These changes are non-destructive for rows already using supported values.

DELIMITER $$

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

CALL sme_exec_if_table_exists('sales_orders', 'UPDATE sales_orders SET status = ''DRAFT'' WHERE status IS NULL OR status = ''''');
CALL sme_modify_column_if_exists('sales_orders', 'status', 'status ENUM(''DRAFT'',''SUBMITTED'',''APPROVED'',''REJECTED'',''CONVERTED'',''CANCELLED'',''PENDING'') NOT NULL DEFAULT ''DRAFT''');

CALL sme_exec_if_table_exists('sales_invoices', 'UPDATE sales_invoices SET status = ''DRAFT'' WHERE status IS NULL OR status = ''''');
CALL sme_exec_if_table_exists('sales_invoices', 'UPDATE sales_invoices SET payment_status = ''DUE'' WHERE payment_status IS NULL OR payment_status = ''''');
CALL sme_modify_column_if_exists('sales_invoices', 'status', 'status ENUM(''DRAFT'',''SUBMITTED'',''APPROVED'',''POSTED'',''PARTIAL_PAID'',''PAID'',''CANCELLED'',''REVERSED'',''PENDING'',''CONFIRMED'',''COMPLETED'') NOT NULL DEFAULT ''DRAFT''');
CALL sme_modify_column_if_exists('sales_invoices', 'payment_status', 'payment_status ENUM(''PAID'',''PARTIAL'',''DUE'') NOT NULL DEFAULT ''DUE''');

CALL sme_exec_if_table_exists('purchase_orders', 'UPDATE purchase_orders SET status = ''DRAFT'' WHERE status IS NULL OR status = ''''');
CALL sme_modify_column_if_exists('purchase_orders', 'status', 'status ENUM(''DRAFT'',''SUBMITTED'',''PENDING'',''APPROVED'',''REJECTED'',''PARTIAL_RECEIVED'',''RECEIVED'',''POSTED'',''PARTIAL_PAID'',''PAID'',''REVERSED'',''CANCELLED'') NOT NULL DEFAULT ''DRAFT''');

CALL sme_exec_if_table_exists('supplier_payments', 'UPDATE supplier_payments SET status = ''DRAFT'' WHERE status IS NULL OR status = ''''');
CALL sme_modify_column_if_exists('supplier_payments', 'status', 'status ENUM(''DRAFT'',''POSTED'',''CANCELLED'',''REVERSED'') NOT NULL DEFAULT ''DRAFT''');

CALL sme_exec_if_table_exists('accounting_expenses', 'UPDATE accounting_expenses SET payment_method = ''OTHER'' WHERE payment_method IS NULL OR payment_method = ''''');
CALL sme_exec_if_table_exists('accounting_expenses', 'UPDATE accounting_expenses SET status = ''DRAFT'' WHERE status IS NULL OR status = ''''');
CALL sme_modify_column_if_exists('accounting_expenses', 'payment_method', 'payment_method ENUM(''CASH'',''BANK'',''MOBILE_BANKING'',''OTHER'') NOT NULL DEFAULT ''OTHER''');
CALL sme_modify_column_if_exists('accounting_expenses', 'status', 'status ENUM(''DRAFT'',''SUBMITTED'',''APPROVED'',''REJECTED'',''POSTED'',''REVERSED'',''CANCELLED'') NOT NULL DEFAULT ''DRAFT''');

CALL sme_exec_if_table_exists('customer_receipts', 'UPDATE customer_receipts SET status = ''DRAFT'' WHERE status IS NULL OR status = ''''');
CALL sme_exec_if_table_exists('customer_receipts', 'UPDATE customer_receipts SET payment_method = ''OTHER'' WHERE payment_method IS NULL OR payment_method = ''''');
CALL sme_modify_column_if_exists('customer_receipts', 'status', 'status ENUM(''DRAFT'',''POSTED'',''CANCELLED'') NOT NULL DEFAULT ''DRAFT''');
CALL sme_modify_column_if_exists('customer_receipts', 'payment_method', 'payment_method ENUM(''CASH'',''BANK'',''MOBILE_BANKING'',''CHEQUE'',''OTHER'') NOT NULL DEFAULT ''OTHER''');

CALL sme_exec_if_table_exists('supplier_payments', 'UPDATE supplier_payments SET payment_method = ''OTHER'' WHERE payment_method IS NULL OR payment_method = ''''');
CALL sme_modify_column_if_exists('supplier_payments', 'payment_method', 'payment_method ENUM(''CASH'',''BANK'',''MOBILE_BANKING'',''CHEQUE'',''OTHER'') NOT NULL DEFAULT ''OTHER''');

DROP PROCEDURE IF EXISTS sme_exec_if_table_exists;
DROP PROCEDURE IF EXISTS sme_modify_column_if_exists;
