import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountingComponent } from './accounting/accounting.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  { path: '', redirectTo: 'expenses', pathMatch: 'full' },
  { path: 'expense-categories', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'categories', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'expenses', redirectTo: '/expenses', pathMatch: 'full' },
  { path: 'accounts', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'accounts', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'journal-entries', redirectTo: 'journals', pathMatch: 'full' },
  { path: 'journals', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'journals', mode: 'list', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'journals/create', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'journals', mode: 'create', permissions: ['ACCOUNTING_CREATE'] } },
  { path: 'journals/edit/:id', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'journals', mode: 'edit', permissions: ['ACCOUNTING_EDIT'] } },
  { path: 'journals/details/:id', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'journals', mode: 'details', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'cash-book', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'cash-book', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'bank-book', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'bank-book', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'customer-ledger', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'customer-ledger', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'supplier-ledger', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'supplier-ledger', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'general-ledger', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'general-ledger', anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] } },
  { path: 'account-ledger/:id', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'account-ledger', anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] } },
  { path: 'profit-loss', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'profit-loss', anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] } },
  { path: 'trial-balance', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'trial-balance', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'balance-sheet', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'balance-sheet', anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccountingRoutingModule {}
