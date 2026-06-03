import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CustomerFormComponent } from './customer-form/customer-form.component';
import { CustomerListComponent } from './customer-list/customer-list.component';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'list', component: CustomerListComponent },
      { path: 'create', component: CustomerFormComponent },
      { path: 'edit/:id', component: CustomerFormComponent },
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
