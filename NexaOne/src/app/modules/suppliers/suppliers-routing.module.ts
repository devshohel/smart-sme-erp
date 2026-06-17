import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SupplierFormComponent } from './supplier-form/supplier-form.component';
import { SupplierListComponent } from './supplier-list/supplier-list.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'list', component: SupplierListComponent, canActivate: [PermissionGuard], data: { permissions: ['SUPPLIER_VIEW'] } },
      { path: 'create', component: SupplierFormComponent, canActivate: [PermissionGuard], data: { permissions: ['SUPPLIER_CREATE'] } },
      { path: 'edit/:id', component: SupplierFormComponent, canActivate: [PermissionGuard], data: { permissions: ['SUPPLIER_EDIT'] } },
      { path: '', redirectTo: 'list', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SuppliersRoutingModule { }
