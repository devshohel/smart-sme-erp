# Smart SME ERP - Database Tables

This document reflects the implemented Spring Boot entities in `sme/src/main/java/com/sme/erp` as of the submission cleanup.

## Implemented Tables

### Auth and Permissions
- `roles`
- `permissions`
- `role_permissions`
- `users`

### Audit
- `activity_logs`
- `audit_logs`
- `login_history`

### Product
- `products`
- `product_categories`
- `product_brands`
- `uoms`

### Inventory
- `warehouses`
- `stocks`
- `stock_movements`
- `stock_adjustments`

### Customers and Suppliers
- `customers`
- `suppliers`

### Purchases
- `purchase_orders`
- `purchase_items`
- `purchase_returns`
- `purchase_return_items`

### Sales
- `sales_orders`
- `sales_invoices`
- `sales_items`
- `sales_returns`
- `sales_return_items`

### Accounting
- `accounting_accounts`
- `accounting_expense_categories`
- `accounting_expenses`
- `accounting_journal_entries`
- `accounting_journal_entry_lines`

### Settings
- `company_settings`
- `invoice_settings`
- `tax_settings`
- `system_settings`

## Implemented Reporting Data

Reports are generated from existing transactional tables. There are DTOs for:
- Sales report
- Purchase report
- Stock report
- Customer due report
- Supplier due report
- Profit and loss summary

There are no separate persisted report tables.

## Deferred Tables / Not Implemented Yet

These were planned earlier but are not implemented as active entities:
- Product variants
- Stock transfers and stock transfer items
- Dedicated payment methods, transactions, and payment details module
- Attachments
- Notifications
- Backup/restore tables

## Notes

- Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update`.
- The database is MySQL, configured in `sme/src/main/resources/application.properties`.
- Do not present deferred tables as completed implementation.
