import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountingComponent } from './accounting/accounting.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  { path: '', redirectTo: 'expenses', pathMatch: 'full' },
  { path: 'expense-categories', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'categories', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'expenses', redirectTo: '/expenses', pathMatch: 'full' },
  { path: 'accounts', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'accounts', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'journal-entries', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'journals', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'cash-book', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'cash-book', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'bank-book', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'bank-book', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'customer-ledger', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'customer-ledger', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'supplier-ledger', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'supplier-ledger', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'general-ledger', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'general-ledger', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'trial-balance', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'trial-balance', permissions: ['ACCOUNTING_VIEW'] } },
  { path: 'balance-sheet', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'balance-sheet', permissions: ['ACCOUNTING_VIEW'] } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccountingRoutingModule {}
