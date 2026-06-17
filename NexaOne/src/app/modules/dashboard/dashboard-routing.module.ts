import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  { path: '', component: DashboardComponent, canActivate: [PermissionGuard], data: { permissions: ['DASHBOARD_VIEW'] } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule {}
