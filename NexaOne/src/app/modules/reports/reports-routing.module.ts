import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReportsComponent } from './reports.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  { path: '', component: ReportsComponent, canActivate: [PermissionGuard], data: { permissions: ['REPORT_VIEW'], mode: 'center', breadcrumb: 'Report Center' } },
  { path: 'view/:type', component: ReportsComponent, canActivate: [PermissionGuard], data: { permissions: ['REPORT_VIEW'], mode: 'detail', breadcrumb: 'Report Details' } },
  { path: ':type', redirectTo: 'view/:type', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportsRoutingModule { }
