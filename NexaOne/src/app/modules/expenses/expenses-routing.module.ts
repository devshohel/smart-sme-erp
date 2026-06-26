import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from '../auth/permission.guard';
import { ExpenseApprovalQueueComponent } from './expense-approval-queue/expense-approval-queue.component';
import { ExpenseDetailsComponent } from './expense-details/expense-details.component';
import { ExpenseFormComponent } from './expense-form/expense-form.component';
import { ExpenseListComponent } from './expense-list/expense-list.component';
import { ExpenseReportsComponent } from './expense-reports/expense-reports.component';

const routes: Routes = [
  { path: '', component: ExpenseListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['EXPENSE_VIEW', 'EXPENSE_CREATE', 'EXPENSE_EDIT', 'EXPENSE_SUBMIT', 'EXPENSE_APPROVE', 'EXPENSE_REJECT', 'EXPENSE_POST', 'EXPENSE_CANCEL', 'EXPENSE_REVERSE', 'EXPENSE_REPORT_VIEW'], breadcrumb: 'Expense List' } },
  { path: 'approval-queue', component: ExpenseApprovalQueueComponent, canActivate: [PermissionGuard], data: { permissions: ['EXPENSE_APPROVE'], breadcrumb: 'Approvals' } },
  { path: 'reports', component: ExpenseReportsComponent, canActivate: [PermissionGuard], data: { permissions: ['EXPENSE_REPORT_VIEW'], breadcrumb: 'Reports' } },
  { path: 'create', component: ExpenseFormComponent, canActivate: [PermissionGuard], data: { permissions: ['EXPENSE_CREATE'], breadcrumb: 'Create Expense' } },
  { path: 'edit/:id', component: ExpenseFormComponent, canActivate: [PermissionGuard], data: { permissions: ['EXPENSE_EDIT'], breadcrumb: 'Edit Expense' } },
  { path: 'details/:id', component: ExpenseDetailsComponent, canActivate: [PermissionGuard], data: { permissions: ['EXPENSE_VIEW'], breadcrumb: 'Details' } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ExpensesRoutingModule {}
