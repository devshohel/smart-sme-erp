📦 Smart SME ERP
Subtitle: A Modular Enterprise Resource Planning System
for Small and Medium Enterprises.

🎯 Complete Feature List (Final – Production Ready)
________________________________________
🔐 1. CORE (Authentication & Authorization)
📁 core/
Authentication
•	User Login / Logout
•	Change Password
•	Reset Password (Email / OTP ভিত্তিক)
•	Last Login Tracking
•	Failed Login Attempt Tracking
Authorization (RBAC)
•	Role-based Access Control (RBAC)
•	Module-wise Permission
•	Feature-wise Permission (Create / Edit / Delete / View)
•	Backend API Security Enforcement
User Management
•	Create / Edit / Delete User
•	User Status (Active / Inactive)
•	Soft Delete Support (➕ Added)
Role Management
•	Roles (Admin, Manager, Staff)
•	Assign Permissions to Roles
Activity & Security
•	User Activity Log
•	Login History
•	Audit Trail (Who changed what & when)
________________________________________
📊 2. DASHBOARD
📁 modules/dashboard/
System Overview
•	Today Sales
•	Today Purchase
•	Today Expense
•	Total Stock Value
Business Summary
•	Total Customers
•	Total Suppliers
•	Customer Due
•	Supplier Due
Alerts & Insights
•	Low Stock Alert
•	Recent Transactions
•	Top Selling Products (➕ Added)
•	Monthly Sales Chart (➕ Added)
•	Quick Summary Cards
Notifications (➕ Added)
•	Due Alerts 
•	Low Stock Alerts
________________________________________
📦 3. PRODUCT MODULE
📁 modules/products/
________________________________________
🛍️ All Products (products/)
• Create / Edit / Delete Product (Soft Delete)
• SKU / Barcode Management
• Product Image Upload
• Product Status (Active / Inactive)
• Purchase Price / Sale Price
• Price History Tracking
• Product Variants (Size, Color)
________________________________________
🗂️ Categories (categories/)
• Create / Edit / Delete Category
• Category Hierarchy (Parent / Child)
________________________________________
🏷️ Brands (brands/)
• Create / Edit / Delete Brand
________________________________________
⚖️ Unit of Measures (uom-settings/)
• Unit of Measurement (PCS, KG, Litre)
• Conversion Support
________________________________________
📦 4. INVENTORY MODULE
📁 modules/inventory/
________________________________________
  Current Stock (stock/)
Stock Core
•	Real-time Stock Quantity
•	Reorder Level Setup
•	Low Stock Alert
Stock Operations
•	Stock In (Purchase Auto)
•	Stock Out (Sales Auto)
•	Stock Adjustment (Damage / Loss)
•	Stock Transfer (Warehouse to Warehouse)
Tracking
•	Stock Movement History
•	Batch-wise Stock (➕ future scalable)
________________________________________
📜 Stock Movement (stock-movement/)
• Complete Stock In / Out History
• Purchase, Sales, Transfer & Adjustment Tracking
________________________________________
➕ Stock Adjustment (stock-adjustments/)
• Manual Stock Correction
• Damage / Loss / Correction Entry
________________________________________
🏬 Warehouses (warehouses/)
•	Create / Edit / Delete Warehouse
•	Multi-Warehouse Support
________________________________________

💰 5. SALES MODULE
📁 modules/sales/
Orders (orders/)
•	Create Sales Order
•	Order Status Tracking
Invoices (invoices/)
•	Generate Sales Invoice
•	Auto Invoice Numbering (Configurable ➕)
•	Discount System
•	Tax/VAT Calculation (Advanced ➕)
•	Auto Stock Deduction
Returns (returns/)
•	Sales Return
•	Refund / Adjustment
Payment Features
•	Full / Partial Payment
•	Customer Due / Advance Tracking
•	Payment Reference Linking
________________________________________
🛒 6. PURCHASE MODULE
📁 modules/purchases/
Purchase Orders (purchase-orders/)
•	Create Purchase Order
•	Approval Flow
Purchase Invoices (purchase-invoices/)
•	Goods Receive
•	Purchase Invoice তৈরি
•	Auto Stock Increase
Purchase Returns (purchase-returns/)
•	Return to Supplier
•	Stock Deduction
Payment Features
•	Supplier Payment
•	Supplier Due / Advance
________________________________________
👥 7. CUSTOMER MODULE
📁 modules/customers/
Customer List
•	View All Customers
•	Search / Filter
Customer Form
•	Create / Edit / Delete Customer (Soft Delete)
Core Features
•	Customer Profile
•	Contact Info (Phone, Email, Address)
•	Opening Balance
Financial Tracking
•	Customer Ledger
•	Transaction History
•	Due / Advance Balance
________________________________________
🏭 8. SUPPLIER MODULE
📁 modules/suppliers/
Supplier List
•	View All Suppliers
•	Search / Filter
Supplier Form
•	Create / Edit / Delete Supplier
Core Features
•	Supplier Profile
•	Company Info
•	Contact Info
Financial Tracking
•	Supplier Ledger
•	Transaction History
•	Payable / Advance Balance
________________________________________
💳 9. ACCOUNTING MODULE
📁 modules/accounting/
________________________________________
💸 Transactions (transactions/)
•	All Financial Transactions
•	Customer & Supplier Transactions
•	Reference Linking (Sales/Purchase/Expense)
________________________________________
📘 Journal (journal/)
•	Create Journal Entry
•	Auto Journal (Sales / Purchase / Expense)
•	Debit / Credit System
•	Transaction Reference
________________________________________
💵 Expenses (expenses/)
•	Expense Category
•	Add / Edit / Delete Expense
•	Expense History
________________________________________
🧾 Payments (payments/)
Payment Methods
•	Cash
•	Bank
•	MFS (bKash, Nagad, Rocket)
Features
•	Receive Payment
•	Make Payment
•	Payment History
•	Multi-payment Support
________________________________________
🏦 Chart of Accounts (Sidebar aligned)
•	Account Head Create (Assets, Liabilities, Income, Expense)
•	Cash / Bank / Expense Accounts
•	Account Hierarchy
________________________________________
📊 10. REPORTS MODULE
📁 modules/reports/
________________________________________
📈 Sales Report
•	Daily / Monthly / Custom(Date Range Filter)
•	Product-wise Sales
•	Customer-wise
📦 Inventory Report
•	Stock Report
•	Stock Movement Report
•	Low Stock Report
💰 Finance Report
•	Expense Report
•	Customer Due Report
•	Supplier Due Report
•	Cash Flow Report
•	Profit & Loss
•	Balance Sheet
Export & Filters (➕ Added)
•	PDF Export 
•	Excel Export 
•	Advanced Filters
________________________________________
⚙️ 11. SETTINGS MODULE
📁 modules/settings/
________________________________________
🏢 Company
•	Company Info
•	Logo Upload
👤 Users
•	Manage Users (linked with core)
🔐 Roles & Permissions
•	Role Management
•	Permission সেট করা
________________________________________
⚙️ System Configuration
•	Currency Setting
•	Tax Settings (Advanced rules ➕)
•	Invoice Template
•	Numbering System (Invoice সিরিয়াল ➕ Added)
•	Date & Number Format (➕)
________________________________________
📎 Attachments (➕ Added)
•	Upload Files (Invoice, Expense proof)
________________________________________
💾 Backup & Security
•	Database Backup
•	Restore System
•	Auto Backup Scheduler (➕)________________________________________
🔄 12. SYSTEM CORE LOGIC (Hidden but Critical)
👉 এগুলো UI তে সরাসরি দেখা না গেলেও ERP এর backbone:
•	Real-time Stock Update Engine
•	Transaction-based Balance Calculation
•	Double Entry Accounting Logic
•	Reference Linking System (Sales ↔ Payment ↔ Ledger)
•	Soft Delete Handling
•	Audit Log Engine
