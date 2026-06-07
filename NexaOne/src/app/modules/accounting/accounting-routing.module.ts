import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountingComponent } from './accounting/accounting.component';

const routes: Routes = [
  { path: '', redirectTo: 'expenses', pathMatch: 'full' },
  { path: 'expense-categories', component: AccountingComponent, data: { section: 'categories' } },
  { path: 'expenses', component: AccountingComponent, data: { section: 'expenses' } },
  { path: 'accounts', component: AccountingComponent, data: { section: 'accounts' } },
  { path: 'journal-entries', component: AccountingComponent, data: { section: 'journals' } },
  { path: 'cash-book', component: AccountingComponent, data: { section: 'cash-book' } },
  { path: 'bank-book', component: AccountingComponent, data: { section: 'bank-book' } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccountingRoutingModule {}
