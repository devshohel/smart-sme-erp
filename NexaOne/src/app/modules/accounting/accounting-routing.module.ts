import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountingComponent } from './accounting/accounting.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  { path: '', redirectTo: 'expenses', pathMatch: 'full' },
  { path: 'expense-categories', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'categories', permissions: ['ACCOUNTING_VIEW'], breadcrumb: 'Expense Categories' } },
  { path: 'expenses', redirectTo: '/expenses', pathMatch: 'full' },
  { path: 'accounts', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'accounts', permissions: ['ACCOUNTING_VIEW'], breadcrumb: 'Chart of Accounts' } },
  { path: 'journal-entries', redirectTo: 'journals', pathMatch: 'full' },
  { path: 'journals', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'journals', mode: 'list', permissions: ['ACCOUNTING_VIEW'], breadcrumb: 'Journal Entries' } },
  { path: 'journals/create', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'journals', mode: 'create', permissions: ['ACCOUNTING_CREATE'], breadcrumb: 'Create Journal' } },
  { path: 'journals/edit/:id', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'journals', mode: 'edit', permissions: ['ACCOUNTING_EDIT'], breadcrumb: 'Edit Journal' } },
  { path: 'journals/details/:id', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'journals', mode: 'details', permissions: ['ACCOUNTING_VIEW'], breadcrumb: 'Journal Details' } },
  { path: 'cash-book', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'cash-book', permissions: ['ACCOUNTING_VIEW'], breadcrumb: 'Cash Book' } },
  { path: 'bank-book', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'bank-book', permissions: ['ACCOUNTING_VIEW'], breadcrumb: 'Bank Book' } },
  { path: 'customer-ledger', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'customer-ledger', permissions: ['ACCOUNTING_VIEW'], breadcrumb: 'Customer Ledger' } },
  { path: 'supplier-ledger', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'supplier-ledger', permissions: ['ACCOUNTING_VIEW'], breadcrumb: 'Supplier Ledger' } },
  { path: 'general-ledger', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'general-ledger', anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'], breadcrumb: 'General Ledger' } },
  { path: 'account-ledger/:id', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'account-ledger', anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'], breadcrumb: 'Account Ledger' } },
  { path: 'profit-loss', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'profit-loss', anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'], breadcrumb: 'Profit & Loss' } },
  { path: 'trial-balance', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'trial-balance', permissions: ['ACCOUNTING_VIEW'], breadcrumb: 'Trial Balance' } },
  { path: 'balance-sheet', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'balance-sheet', anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'], breadcrumb: 'Balance Sheet' } },
  { path: 'cost-centers', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'cost-centers', permissions: ['COST_CENTER_VIEW'], breadcrumb: 'Cost Centers' } },
  { path: 'budgets', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'budgets', mode: 'list', permissions: ['BUDGET_VIEW'], breadcrumb: 'Budgets' } },
  { path: 'budgets/create', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'budgets', mode: 'create', permissions: ['BUDGET_CREATE'], breadcrumb: 'Create Budget' } },
  { path: 'budgets/edit/:id', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'budgets', mode: 'edit', permissions: ['BUDGET_EDIT'], breadcrumb: 'Edit Budget' } },
  { path: 'budgets/details/:id', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'budgets', mode: 'details', permissions: ['BUDGET_VIEW'], breadcrumb: 'Budget Details' } },
  { path: 'budget-vs-actual', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'budget-vs-actual', permissions: ['BUDGET_VIEW'], breadcrumb: 'Budget vs Actual' } },
  { path: 'financial-dashboard', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'financial-dashboard', permissions: ['FINANCIAL_DASHBOARD_VIEW'], breadcrumb: 'Financial Dashboard' } },
  { path: 'periods', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'periods', permissions: ['ACCOUNTING_PERIOD_VIEW'], breadcrumb: 'Accounting Periods' } },
  { path: 'year-end-closings', component: AccountingComponent, canActivate: [PermissionGuard], data: { section: 'year-end-closings', permissions: ['YEAR_END_VIEW'], breadcrumb: 'Year-End Closing' } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccountingRoutingModule {}
