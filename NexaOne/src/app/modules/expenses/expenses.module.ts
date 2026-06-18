import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { ExpenseApprovalQueueComponent } from './expense-approval-queue/expense-approval-queue.component';
import { ExpenseDetailsComponent } from './expense-details/expense-details.component';
import { ExpenseFormComponent } from './expense-form/expense-form.component';
import { ExpenseListComponent } from './expense-list/expense-list.component';
import { ExpenseReportsComponent } from './expense-reports/expense-reports.component';
import { ExpensesRoutingModule } from './expenses-routing.module';

@NgModule({
  declarations: [ExpenseListComponent, ExpenseFormComponent, ExpenseDetailsComponent, ExpenseApprovalQueueComponent, ExpenseReportsComponent],
  imports: [CommonModule, FormsModule, SharedModule, ExpensesRoutingModule]
})
export class ExpensesModule {}
