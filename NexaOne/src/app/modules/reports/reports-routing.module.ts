import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReportsComponent } from './reports.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  { path: '', component: ReportsComponent, canActivate: [PermissionGuard], data: { permissions: ['REPORT_VIEW'], mode: 'center' } },
  { path: 'view/:type', component: ReportsComponent, canActivate: [PermissionGuard], data: { permissions: ['REPORT_VIEW'], mode: 'detail' } },
  { path: ':type', redirectTo: 'view/:type', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportsRoutingModule { }
