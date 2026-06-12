# Smart SME ERP - Frontend and Backend Structure

This document reflects the actual project structure after cleanup.

## Frontend

Root: `NexaOne/`

### Key Angular Structure

- `src/app/app-routing.module.ts`
- `src/app/app.module.ts`
- `src/app/layouts/main-layout/`
  - `main-layout`
  - `navbar`
  - `sidebar`
- `src/app/shared/`
  - shared module
  - reusable components
  - API response/error utilities
- `src/app/models/`
  - product, category, brand, UOM
  - customer, supplier
  - inventory stock and warehouse
  - purchase
  - sales order, invoice, return
- `src/app/services/`
  - product, category, brand, UOM
  - customer, supplier
  - inventory warehouse and stock
  - purchase
  - sales order, invoice, return
- `src/app/modules/`
  - `auth`
  - `dashboard`
  - `products`
  - `inventory`
  - `customers`
  - `suppliers`
  - `purchases`
  - `sales`
  - `reports`
  - `accounting`

### Routing Summary

- `/login`
- `/dashboard`
- `/products`
- `/inventory`
- `/customers`
- `/suppliers`
- `/purchases`
- `/sales`
- `/reports`
- `/accounting`
- `/settings`

The active reports implementation is now under `src/app/modules/reports`.

### Sidebar Menus

- Dashboard
- Products
  - Products
  - Add Product
  - Categories
  - Brands
  - UOM
- Inventory
  - Stock
  - Stock Adjustments
  - Stock Movements
  - Warehouses
- Customers
- Suppliers
- Purchases
  - Purchase Orders
  - Purchase Invoices
  - Purchase Returns
- Sales
  - Orders
  - Invoices
  - Returns
- Reports
  - Sales Report
  - Purchase Report
  - Stock Report
  - Customer Due Report
  - Supplier Due Report
  - Profit and Loss
- Accounting
  - Expenses
  - Expense Categories
  - Cash Book
  - Bank Book
  - Journal Entries
  - Chart of Accounts
  - Customer Ledger
  - Supplier Ledger
  - General Ledger
  - Trial Balance
  - Balance Sheet
- Settings
  - Company Settings
  - Invoice Settings
  - Tax Settings
  - System Settings
  - Users
  - Roles and Permissions
  - Change Password
  - Activity Logs
  - Audit Logs
  - Login History

## Backend

Root: `sme/`

### Main Package

`sme/src/main/java/com/sme/erp`

### Implemented Backend Packages

- `auth`
  - controller
  - service
  - repository
  - entity
  - dto
  - security
  - config
- `audit`
  - controller
  - service
  - repository
  - entity
  - dto
  - config
- `common`
  - exception
  - response
  - util
- `dashboard`
  - controller
  - service
  - dto
- `product`
  - controller
  - service
  - repository
  - entity
  - dto
  - mapper
- `inventory`
  - controller
  - service
  - repository
  - entity
  - dto
  - mapper
- `customer`
- `supplier`
- `purchase`
- `sales`
- `reports`
- `accounting`
- `settings`
- `enums`

### API Base URL

Angular uses:

```text
http://localhost:8080/api/v1
```

This is configured in:
- `NexaOne/src/environments/environment.ts`
- `NexaOne/src/environments/environment.prod.ts`

### Security

- Backend security is JWT based.
- Frontend token storage key: `sme_access_token`.
- `JwtInterceptor` sends `Authorization: Bearer <token>`.
- Backend routes require ERP roles and method-level permissions.

## Duplicate / Legacy Frontend Structure

- Reports have been migrated into `src/app/modules/reports`.
- No active `pages/customers` or `pages/sales` folders were found.
- Confirmed active feature code remains in `src/app/modules`.
