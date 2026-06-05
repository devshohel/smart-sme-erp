-- Sales order item persistence migration for Smart SME ERP.
-- Required because Sales Orders now persist item rows before invoice creation.
-- Existing schema has sales_items.invoice_id as invoice-only; order rows need order_id and nullable invoice_id.

-- Inspect current table shape.
SHOW COLUMNS FROM sales_orders;
SHOW COLUMNS FROM sales_items;

-- Add durable Sales Order header fields if Hibernate did not add them automatically.
ALTER TABLE sales_orders
  ADD COLUMN notes VARCHAR(500) NULL,
  ADD COLUMN grand_total DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN updated_at DATETIME NULL;

-- If any of these columns already exist, run the individual missing ALTER statements instead:
-- ALTER TABLE sales_orders ADD COLUMN notes VARCHAR(500) NULL;
-- ALTER TABLE sales_orders ADD COLUMN grand_total DECIMAL(15,2) NOT NULL DEFAULT 0.00;
-- ALTER TABLE sales_orders ADD COLUMN updated_at DATETIME NULL;

-- Let sales_items represent either an invoice line or a sales-order line.
ALTER TABLE sales_items
  ADD COLUMN order_id INT NULL,
  MODIFY invoice_id INT NULL;

-- Add index/foreign key only if your database does not already create it through Hibernate.
CREATE INDEX idx_sales_items_order_id ON sales_items(order_id);

ALTER TABLE sales_items
  ADD CONSTRAINT fk_sales_items_order
  FOREIGN KEY (order_id) REFERENCES sales_orders(id);
