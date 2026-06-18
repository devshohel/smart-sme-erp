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
      { path: 'unauthorized', component: UnauthorizedComponent },
      { path: 'dashboard', canActivate: [PermissionGuard], data: { permissions: ['DASHBOARD_VIEW'] }, loadChildren: () => import('./modules/dashboard/dashboard.module').then(m => m.DashboardModule) },
      { path: 'products', canActivate: [PermissionGuard], data: { anyPermissions: ['PRODUCT_VIEW', 'PRODUCT_CREATE', 'PRODUCT_EDIT'] }, loadChildren: () => import('./modules/products/products.module').then(m => m.ProductsModule) },
      { path: 'inventory', canActivate: [PermissionGuard], data: { anyPermissions: ['INVENTORY_VIEW', 'TRANSFER_VIEW', 'STOCK_ADJUSTMENT_CREATE'] }, loadChildren: () => import('./modules/inventory/inventory.module').then(m => m.InventoryModule) },
      { path: 'sales', canActivate: [PermissionGuard], data: { anyPermissions: ['SALES_VIEW', 'SALES_CREATE', 'SALES_EDIT'] }, loadChildren: () => import('./modules/sales/sales.module').then(m => m.SalesModule) },
      { path: 'customers', canActivate: [PermissionGuard], data: { anyPermissions: ['CUSTOMER_VIEW', 'CUSTOMER_CREATE', 'CUSTOMER_RECEIPT_VIEW'] }, loadChildren: () => import('./modules/customers/customers.module').then(m => m.CustomersModule) },
      { path: 'suppliers', canActivate: [PermissionGuard], data: { anyPermissions: ['SUPPLIER_VIEW', 'SUPPLIER_CREATE', 'SUPPLIER_EDIT', 'SUPPLIER_PAYMENT_VIEW', 'SUPPLIER_PAYMENT_CREATE', 'SUPPLIER_LEDGER_VIEW'] }, loadChildren: () => import('./modules/suppliers/suppliers.module').then(m => m.SuppliersModule) },
      { path: 'purchases', canActivate: [PermissionGuard], data: { anyPermissions: ['PURCHASE_VIEW', 'PURCHASE_CREATE', 'PURCHASE_EDIT'] }, loadChildren: () => import('./modules/purchases/purchases.module').then(m => m.PurchasesModule) },
      { path: 'expenses', canActivate: [PermissionGuard], data: { anyPermissions: ['EXPENSE_VIEW', 'EXPENSE_CREATE', 'EXPENSE_EDIT', 'EXPENSE_SUBMIT', 'EXPENSE_APPROVE', 'EXPENSE_REJECT', 'EXPENSE_POST', 'EXPENSE_CANCEL', 'EXPENSE_REVERSE', 'EXPENSE_REPORT_VIEW'] }, loadChildren: () => import('./modules/expenses/expenses.module').then(m => m.ExpensesModule) },
      { path: 'accounting', canActivate: [PermissionGuard], data: { permissions: ['ACCOUNTING_VIEW'] }, loadChildren: () => import('./modules/accounting/accounting.module').then(m => m.AccountingModule) },
      { path: 'reports', canActivate: [PermissionGuard], data: { permissions: ['REPORT_VIEW'] }, loadChildren: () => import('./modules/reports/reports.module').then(m => m.ReportsModule) },
      { path: 'settings', redirectTo: 'settings/company', pathMatch: 'full' },
      { path: 'settings', canActivate: [PermissionGuard], data: { anyPermissions: ['SETTINGS_VIEW', 'USER_VIEW', 'ROLE_VIEW', 'ACTIVITY_VIEW', 'AUDIT_VIEW', 'LOGIN_HISTORY_VIEW'] }, loadChildren: () => import('./modules/settings/settings.module').then(m => m.SettingsModule) },
    ]
  }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
