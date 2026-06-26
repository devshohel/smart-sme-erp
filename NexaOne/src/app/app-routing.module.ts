import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { AuthGuard } from './modules/auth/auth.guard';
import { PermissionGuard } from './modules/auth/permission.guard';
import { UnauthorizedComponent } from './unauthorized/unauthorized.component';

const routes: Routes = [
  { path: 'login', loadChildren: () => import('./modules/auth/auth.module').then(m => m.AuthModule) },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'unauthorized', component: UnauthorizedComponent, data: { breadcrumb: 'Unauthorized' } },
      { path: 'dashboard', canActivate: [PermissionGuard], data: { permissions: ['DASHBOARD_VIEW'], breadcrumb: 'Dashboard' }, loadChildren: () => import('./modules/dashboard/dashboard.module').then(m => m.DashboardModule) },
      { path: 'notifications', canActivate: [PermissionGuard], data: { permissions: ['NOTIFICATION_VIEW'], breadcrumb: 'Notifications' }, loadChildren: () => import('./modules/notifications/notifications.module').then(m => m.NotificationsModule) },
      { path: 'products', canActivate: [PermissionGuard], data: { anyPermissions: ['PRODUCT_VIEW', 'PRODUCT_CREATE', 'PRODUCT_EDIT', 'CATEGORY_VIEW', 'BRAND_VIEW', 'UOM_VIEW'], breadcrumb: 'Products' }, loadChildren: () => import('./modules/products/products.module').then(m => m.ProductsModule) },
      { path: 'inventory', canActivate: [PermissionGuard], data: { anyPermissions: ['INVENTORY_VIEW', 'STOCK_ADJUSTMENT_VIEW', 'STOCK_ADJUSTMENT_CREATE', 'TRANSFER_VIEW', 'TRANSFER_CREATE', 'TRANSFER_EDIT'], breadcrumb: 'Inventory' }, loadChildren: () => import('./modules/inventory/inventory.module').then(m => m.InventoryModule) },
      { path: 'sales', canActivate: [PermissionGuard], data: { anyPermissions: ['SALES_VIEW', 'SALES_CREATE', 'SALES_EDIT', 'SALES_ORDER_VIEW', 'SALES_ORDER_CREATE', 'SALES_ORDER_EDIT', 'SALES_INVOICE_VIEW', 'SALES_INVOICE_CREATE', 'SALES_INVOICE_EDIT', 'SALES_RETURN_VIEW', 'SALES_RETURN_CREATE', 'SALES_RETURN_EDIT'], breadcrumb: 'Sales' }, loadChildren: () => import('./modules/sales/sales.module').then(m => m.SalesModule) },
      { path: 'customers', canActivate: [PermissionGuard], data: { anyPermissions: ['CUSTOMER_VIEW', 'CUSTOMER_CREATE', 'CUSTOMER_EDIT', 'CUSTOMER_AGING_VIEW', 'CUSTOMER_LEDGER_VIEW', 'CUSTOMER_RECEIPT_VIEW', 'CUSTOMER_RECEIPT_CREATE', 'CUSTOMER_RECEIPT_EDIT'], breadcrumb: 'Customers' }, loadChildren: () => import('./modules/customers/customers.module').then(m => m.CustomersModule) },
      { path: 'suppliers', canActivate: [PermissionGuard], data: { anyPermissions: ['SUPPLIER_VIEW', 'SUPPLIER_CREATE', 'SUPPLIER_EDIT', 'SUPPLIER_PAYMENT_VIEW', 'SUPPLIER_PAYMENT_CREATE', 'SUPPLIER_PAYMENT_EDIT', 'SUPPLIER_LEDGER_VIEW'], breadcrumb: 'Suppliers' }, loadChildren: () => import('./modules/suppliers/suppliers.module').then(m => m.SuppliersModule) },
      { path: 'purchases', canActivate: [PermissionGuard], data: { anyPermissions: ['PURCHASE_VIEW', 'PURCHASE_CREATE', 'PURCHASE_EDIT', 'PURCHASE_ORDER_VIEW', 'PURCHASE_ORDER_CREATE', 'PURCHASE_ORDER_EDIT', 'PURCHASE_RECEIVE_VIEW', 'PURCHASE_RECEIVE_CREATE', 'PURCHASE_INVOICE_VIEW', 'PURCHASE_INVOICE_CREATE', 'PURCHASE_INVOICE_EDIT', 'PURCHASE_RETURN_VIEW', 'PURCHASE_RETURN_CREATE', 'PURCHASE_RETURN_EDIT'], breadcrumb: 'Purchases' }, loadChildren: () => import('./modules/purchases/purchases.module').then(m => m.PurchasesModule) },
      { path: 'expenses', canActivate: [PermissionGuard], data: { anyPermissions: ['EXPENSE_VIEW', 'EXPENSE_CREATE', 'EXPENSE_EDIT', 'EXPENSE_SUBMIT', 'EXPENSE_APPROVE', 'EXPENSE_REJECT', 'EXPENSE_POST', 'EXPENSE_CANCEL', 'EXPENSE_REPORT_VIEW'], breadcrumb: 'Expenses' }, loadChildren: () => import('./modules/expenses/expenses.module').then(m => m.ExpensesModule) },
      { path: 'accounting', canActivate: [PermissionGuard], data: { anyPermissions: ['ACCOUNTING_VIEW', 'ACCOUNTING_CREATE', 'ACCOUNTING_EDIT', 'REPORT_VIEW', 'COST_CENTER_VIEW', 'COST_CENTER_CREATE', 'COST_CENTER_EDIT', 'BUDGET_VIEW', 'BUDGET_CREATE', 'BUDGET_EDIT', 'FINANCIAL_DASHBOARD_VIEW', 'ACCOUNTING_PERIOD_VIEW', 'YEAR_END_VIEW'], breadcrumb: 'Accounting' }, loadChildren: () => import('./modules/accounting/accounting.module').then(m => m.AccountingModule) },
      { path: 'reports', canActivate: [PermissionGuard], data: { permissions: ['REPORT_VIEW'], breadcrumb: 'Reports' }, loadChildren: () => import('./modules/reports/reports.module').then(m => m.ReportsModule) },
      { path: 'settings', redirectTo: 'settings/company', pathMatch: 'full' },
      { path: 'settings', canActivate: [PermissionGuard], data: { anyPermissions: ['SETTINGS_VIEW', 'SETTINGS_EDIT', 'USER_VIEW', 'USER_CREATE', 'USER_EDIT', 'ROLE_VIEW', 'ACTIVITY_LOG_VIEW', 'ACTIVITY_VIEW', 'AUDIT_VIEW', 'LOGIN_HISTORY_VIEW'], breadcrumb: 'Settings' }, loadChildren: () => import('./modules/settings/settings.module').then(m => m.SettingsModule) },
    ]
  }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
