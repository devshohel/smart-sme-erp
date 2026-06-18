import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { ExpenseDetailsComponent } from './expense-details/expense-details.component';
import { ExpenseFormComponent } from './expense-form/expense-form.component';
import { ExpenseListComponent } from './expense-list/expense-list.component';
import { ExpensesRoutingModule } from './expenses-routing.module';

@NgModule({
  declarations: [ExpenseListComponent, ExpenseFormComponent, ExpenseDetailsComponent],
  imports: [CommonModule, FormsModule, SharedModule, ExpensesRoutingModule]
})
export class ExpensesModule {}
