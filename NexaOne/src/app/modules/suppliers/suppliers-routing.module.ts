import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SupplierFormComponent } from './supplier-form/supplier-form.component';
import { SupplierListComponent } from './supplier-list/supplier-list.component';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'list', component: SupplierListComponent },
      { path: 'create', component: SupplierFormComponent },
      { path: 'edit/:id', component: SupplierFormComponent },
      { path: '', redirectTo: 'list', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SuppliersRoutingModule { }
