import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { AuthGuard } from './modules/auth/auth.guard';

const routes: Routes = [
  { path: 'login', loadChildren: () => import('./modules/auth/auth.module').then(m => m.AuthModule) },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadChildren: () => import('./modules/dashboard/dashboard.module').then(m => m.DashboardModule) },
      { path: 'products', loadChildren: () => import('./modules/products/products.module').then(m => m.ProductsModule) },
      { path: 'inventory', loadChildren: () => import('./modules/inventory/inventory.module').then(m => m.InventoryModule) },
      { path: 'sales', loadChildren: () => import('./modules/sales/sales.module').then(m => m.SalesModule) },
      { path: 'customers', loadChildren: () => import('./modules/customers/customers.module').then(m => m.CustomersModule) },
      { path: 'suppliers', loadChildren: () => import('./modules/suppliers/suppliers.module').then(m => m.SuppliersModule) },
      { path: 'purchases', loadChildren: () => import('./modules/purchases/purchases.module').then(m => m.PurchasesModule) },
      { path: 'accounting', loadChildren: () => import('./modules/accounting/accounting.module').then(m => m.AccountingModule) },
      { path: 'reports', loadChildren: () => import('./pages/reports/reports.module').then(m => m.ReportsModule) },
      { path: 'settings', redirectTo: 'settings/company', pathMatch: 'full' },
      { path: 'settings', loadChildren: () => import('./modules/auth/auth.module').then(m => m.AuthModule) },
    ]
  }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
