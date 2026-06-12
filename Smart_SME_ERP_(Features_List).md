# Smart SME ERP - Implemented Features

This document describes the current implemented scope. It avoids listing planned items as completed features.

## Implemented Modules

### Authentication, Users, Roles, and Permissions
- Login and logout
- Change password
- JWT based API authentication
- Role based access control
- Permission based sidebar visibility
- Users list and user form
- Roles and permissions screen
- Activity logs, audit logs, and login history

### Dashboard
- Summary cards for sales, purchases, expenses, profit, stock value, customer due, supplier due, and entity counts
- Monthly sales/purchase/profit data
- Top selling products
- Low stock alerts
- Due alerts
- Recent transactions

### Products and Inventory
- Products
- Categories
- Brands
- UOM
- Warehouses
- Stock levels
- Stock movements
- Stock adjustments

### Customers and Suppliers
- Customer list, create, edit, delete
- Supplier list, create, edit, delete
- Search/filter support

### Purchases
- Purchase orders
- Purchase invoice/receive screen
- Purchase returns
- Stock update integration is handled by backend services where implemented

### Sales
- Sales orders
- Sales invoices
- Sales returns
- Shared sales status badge component

### Reports
- Sales report
- Purchase report
- Stock report
- Customer due report
- Supplier due report
- Profit and loss summary
- Filter controls for supported reports

### Accounting
- Expense categories
- Expenses
- Chart of accounts
- Journal entries
- Cash book
- Bank book
- Customer ledger
- Supplier ledger
- General ledger
- Trial balance
- Balance sheet

### Settings
- Company settings
- Invoice settings
- Tax settings
- System settings
- Users
- Roles and permissions
- Change password
- Activity logs
- Audit logs
- Login history

## Deferred / Future Enhancements

- Stock transfer module
- Product variants
- Full payment module with payment methods, customer receipts, supplier payments, and payment allocation
- Real PDF export
- Real Excel export
- OTP/reset-password flow
- Attachments and document upload
- Notifications
- Backup/restore UI
- Advanced approval workflows

## Known Limitations

- `environment.prod.ts` still points to `http://localhost:8080/api/v1`; change it before deploying to a hosted backend.
- Some auxiliary dropdown loaders log or silently ignore failures, so permission/API errors can appear as empty option lists.
- Reports currently render data in the UI but do not perform real PDF/Excel export.
- Payment handling is represented through amounts/statuses in sales, purchases, and accounting, but a full payment module is deferred.
