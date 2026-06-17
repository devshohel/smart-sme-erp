import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CustomerDetailsComponent } from './customer-details/customer-details.component';
import { CustomerFormComponent } from './customer-form/customer-form.component';
import { CustomerListComponent } from './customer-list/customer-list.component';
import { CustomerReceiptDetailsComponent } from './customer-receipts/customer-receipt-details/customer-receipt-details.component';
import { CustomerReceiptFormComponent } from './customer-receipts/customer-receipt-form/customer-receipt-form.component';
import { CustomerReceiptListComponent } from './customer-receipts/customer-receipt-list/customer-receipt-list.component';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'list', component: CustomerListComponent },
      { path: 'create', component: CustomerFormComponent },
      { path: 'edit/:id', component: CustomerFormComponent },
      { path: 'details/:id', component: CustomerDetailsComponent },
      { path: 'receipts', component: CustomerReceiptListComponent },
      { path: 'receipts/create', component: CustomerReceiptFormComponent },
      { path: 'receipts/edit/:id', component: CustomerReceiptFormComponent },
      { path: 'receipts/details/:id', component: CustomerReceiptDetailsComponent },
      { path: 'customer-list', redirectTo: 'list', pathMatch: 'full' },
      { path: 'customer-form', redirectTo: 'create', pathMatch: 'full' },
      { path: '', redirectTo: 'list', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CustomersRoutingModule { }
