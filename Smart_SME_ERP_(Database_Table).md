📦 Smart SME ERP

🚀 ✅ Full & Final Erp Database (Truly Complete Version)
________________________________________
🧱 🔐 ১. AUTH & PERMISSION MODULE
Table roles {
  id int [pk, increment]
  role_name varchar [unique]
  description text
  created_at datetime
  updated_at datetime
}

Table permissions {
  id int [pk, increment]
  name varchar [unique]
  module varchar
  created_at datetime
}

Table role_permissions {
  id int [pk, increment]
  role_id int
  permission_id int
}

Table users {
  id int [pk, increment]
  name varchar
  username varchar [unique]
  email varchar [unique]
  phone varchar
  password varchar
  role_id int
  status varchar
  last_login datetime
  is_deleted boolean [default: false]
  created_at datetime
  updated_at datetime
}
________________________________________
📊 ২. ACTIVITY & AUDIT MODULE
Table activity_logs {
  id int [pk, increment]
  user_id int
  action varchar
  table_name varchar
  record_id int
  ip_address varchar
  created_at datetime
}

Table audit_logs {
  id int [pk, increment]
  user_id int
  table_name varchar
  record_id int
  old_data text
  new_data text
  action varchar
  created_at datetime
}
________________________________________
📦 ৩. PRODUCT MODULE
Table uoms {
  id int [pk, increment]
  name varchar
  code varchar [unique]
  type varchar
  conversion_factor decimal
  created_at datetime
  updated_at datetime
}

Table product_categories {
  id int [pk, increment]
  category_name varchar
  code varchar [unique]
  parent_id int
  description text
  status varchar
  is_deleted boolean [default: false]
  created_at datetime
  updated_at datetime
}

Table product_brands {
  id int [pk, increment]
  brand_name varchar
  code varchar [unique]
  is_deleted boolean [default: false]
  created_at datetime
  updated_at datetime
}

Table products {
  id int [pk, increment]
  product_code varchar [unique]
  product_name varchar
  sku varchar [unique]
  barcode varchar
  category_id int
  brand_id int
  uom_id int
  type varchar
  purchase_price decimal
  sale_price decimal
  tax_percentage decimal
  reorder_level int
  image_url varchar
  status varchar
  is_deleted boolean [default: false]
  created_at datetime
  updated_at datetime
}

Table product_variants {
  id int [pk, increment]
  product_id int
  variant_name varchar
  variant_value varchar
  created_at datetime
}
________________________________________
🏬 ৪. INVENTORY MODULE
Table warehouses {
  id int [pk, increment]
  warehouse_code varchar [unique]
  warehouse_name varchar
  location varchar
  manager_id int
  created_at datetime
  updated_at datetime
}

Table stocks {
  id int [pk, increment]
  product_id int
  warehouse_id int
  quantity decimal [default: 0]
  last_updated datetime
}

Table stock_movements {
  id int [pk, increment]
  movement_code varchar [unique]
  product_id int
  warehouse_id int
  reference_type varchar
  reference_id int
  movement_type varchar
  quantity decimal
  unit_cost decimal
  batch_no varchar
  expiry_date date
  note varchar
  created_at datetime
}

Table stock_transfers {
  id int [pk, increment]
  transfer_code varchar [unique]
  from_warehouse_id int
  to_warehouse_id int
  status varchar
  created_by int
  created_at datetime
}

Table stock_transfer_items {
  id int [pk, increment]
  transfer_id int
  product_id int
  quantity decimal
}
________________________________________
👥 ৫. CONTACT MODULE
Table customers {
  id int [pk, increment]
  customer_code varchar [unique]
  name varchar
  company_name varchar
  contact_person varchar
  phone varchar
  email varchar
  address text
  city varchar
  country varchar
  postal_code varchar
  credit_limit decimal [default: 0]
  opening_balance decimal [default: 0]
  current_balance decimal [default: 0]
  tax_number varchar
  status varchar
  is_deleted boolean [default: false]
  created_by int
  created_at datetime
  updated_at datetime
}

Table suppliers {
  id int [pk, increment]
  supplier_code varchar [unique]
  name varchar
  company_name varchar
  contact_person varchar
  phone varchar
  email varchar
  address text
  city varchar
  country varchar
  postal_code varchar
  opening_balance decimal [default: 0]
  current_balance decimal [default: 0]
  tax_number varchar
  bank_account varchar
  payment_terms varchar
  status varchar
  is_deleted boolean [default: false]
  created_by int
  created_at datetime
  updated_at datetime
}
________________________________________
🛒 ৬. PURCHASE MODULE
Table purchase_orders {
  id int [pk, increment]
  purchase_code varchar [unique]
  supplier_id int
  warehouse_id int
  purchase_date datetime
  total_amount decimal
  discount_amount decimal
  tax_amount decimal
  net_total decimal
  paid_amount decimal
  due_amount decimal
  status varchar
  created_by int
  created_at datetime
  updated_at datetime
}

Table purchase_items {
  id int [pk, increment]
  purchase_id int
  product_id int
  uom_id int
  quantity decimal
  unit_price decimal
  discount decimal
  tax decimal
  sub_total decimal
}

Table purchase_returns {
  id int [pk, increment]
  return_code varchar [unique]
  purchase_id int
  supplier_id int
  return_date datetime
  total_amount decimal
  created_by int
  created_at datetime
}

Table purchase_return_items {
  id int [pk, increment]
  return_id int
  product_id int
  quantity decimal
  unit_price decimal
  total decimal
}
________________________________________
💰 ৭. SALES MODULE
Table sales_orders {
  id int [pk, increment]
  order_no varchar [unique]
  customer_id int
  warehouse_id int
  order_date datetime
  status varchar
  created_by int
  created_at datetime
}

Table sales_invoices {
  id int [pk, increment]
  invoice_no varchar [unique]
  order_id int
  customer_id int
  warehouse_id int
  sale_date datetime
  total_amount decimal
  discount_amount decimal
  tax_amount decimal
  net_total decimal
  paid_amount decimal
  due_amount decimal
  payment_status varchar
  status varchar
  created_by int
  created_at datetime
}

Table sales_items {
  id int [pk, increment]
  invoice_id int
  product_id int
  uom_id int
  quantity decimal
  unit_price decimal
  discount decimal
  tax decimal
  sub_total decimal
}

Table sales_returns {
  id int [pk, increment]
  return_code varchar [unique]
  invoice_id int
  customer_id int
  return_date datetime
  total_amount decimal
  created_by int
  created_at datetime
}

Table sales_return_items {
  id int [pk, increment]
  return_id int
  product_id int
  quantity decimal
  unit_price decimal
  total decimal
}
________________________________________
💳 ৮. PAYMENT MODULE
Table payment_methods {
  id int [pk, increment]
  method_name varchar
  code varchar [unique]
  status varchar
}

Table transactions {
  id int [pk, increment]
  trx_code varchar [unique]
  party_type varchar
  party_id int
  payment_method_id int
  trx_type varchar
  amount decimal
  reference_no varchar
  note text
  created_by int
  created_at datetime
}

Table payment_details {
  id int [pk, increment]
  transaction_id int
  reference_id int
  reference_type varchar
  amount decimal
}
________________________________________
💸 ৯. EXPENSE MODULE
Table expense_categories {
  id int [pk, increment]
  category_name varchar
  code varchar [unique]
}

Table expenses {
  id int [pk, increment]
  expense_code varchar [unique]
  category_id int
  payment_method_id int
  amount decimal
  expense_date date
  description text
  created_by int
  created_at datetime
}
________________________________________
📊 ১০. ACCOUNTING MODULE
Table accounts {
  id int [pk, increment]
  account_name varchar
  account_type varchar
  parent_id int
}

Table journal_entries {
  id int [pk, increment]
  entry_date datetime
  description text
  transaction_id int
  created_by int
}

Table journal_items {
  id int [pk, increment]
  journal_id int
  account_id int
  debit decimal
  credit decimal
}
________________________________________
⚙️ ১১. SETTINGS MODULE
Table company_settings {
  id int [pk, increment]
  name varchar
  logo varchar
  email varchar
  phone varchar
  currency varchar
  address text
}

Table tax_settings {
  id int [pk, increment]
  tax_name varchar
  tax_percentage decimal
  status varchar
}

Table numbering_settings {
  id int [pk, increment]
  module varchar
  prefix varchar
  last_number int
}
________________________________________
📎 ১২. SUPPORT MODULES
Table attachments {
  id int [pk, increment]
  reference_type varchar
  reference_id int
  file_url varchar
  uploaded_by int
  created_at datetime
}

Table notifications {
  id int [pk, increment]
  title varchar
  message text
  type varchar
  user_id int
  is_read boolean [default: false]
  created_at datetime
}
 	



🔥 Complete Database Relationship Code

Ref: users.role_id > roles.id
Ref: role_permissions.role_id > roles.id
Ref: role_permissions.permission_id > permissions.id

Ref: activity_logs.user_id > users.id
Ref: audit_logs.user_id > users.id

Ref: products.category_id > product_categories.id
Ref: products.brand_id > product_brands.id
Ref: products.uom_id > uoms.id
Ref: product_categories.parent_id > product_categories.id
Ref: product_variants.product_id > products.id

Ref: warehouses.manager_id > users.id
Ref: stocks.product_id > products.id
Ref: stocks.warehouse_id > warehouses.id

Ref: stock_movements.product_id > products.id
Ref: stock_movements.warehouse_id > warehouses.id

Ref: stock_transfers.from_warehouse_id > warehouses.id
Ref: stock_transfers.to_warehouse_id > warehouses.id
Ref: stock_transfer_items.transfer_id > stock_transfers.id

Ref: purchase_orders.supplier_id > suppliers.id
Ref: purchase_items.purchase_id > purchase_orders.id

Ref: purchase_returns.purchase_id > purchase_orders.id
Ref: purchase_return_items.return_id > purchase_returns.id

Ref: purchase_items.product_id > products.id
Ref: purchase_items.uom_id > uoms.id

Ref: sales_orders.customer_id > customers.id
Ref: sales_invoices.order_id > sales_orders.id
Ref: sales_items.invoice_id > sales_invoices.id

Ref: sales_returns.invoice_id > sales_invoices.id
Ref: sales_return_items.return_id > sales_returns.id

Ref: sales_items.product_id > products.id
Ref: sales_items.uom_id > uoms.id

Ref: transactions.payment_method_id > payment_methods.id
Ref: payment_details.transaction_id > transactions.id

Ref: expenses.category_id > expense_categories.id

Ref: expenses.payment_method_id > payment_methods.id

Ref: transactions.created_by > users.id

Ref: journal_items.journal_id > journal_entries.id
Ref: journal_items.account_id > accounts.id

Ref: journal_entries.created_by > users.id
Ref: journal_entries.transaction_id > transactions.id

Ref: attachments.uploaded_by > users.id
Ref: notifications.user_id > users.id

Ref: customers.created_by > users.id
Ref: suppliers.created_by > users.id 
