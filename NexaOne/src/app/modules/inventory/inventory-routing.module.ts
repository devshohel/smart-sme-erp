import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { StockLevelComponent } from './stock-level/stock-level.component';
import { StockAdjustmentComponent } from './stock-adjustment/stock-adjustment.component';
import { StockMovementComponent } from './stock-movement/stock-movement.component';
import { WarehouseComponent } from './warehouse/warehouse.component';
import { StockCardComponent } from './stock-card/stock-card.component';
import { StockTransferListComponent } from './stock-transfer-list/stock-transfer-list.component';
import { StockTransferFormComponent } from './stock-transfer-form/stock-transfer-form.component';
import { StockTransferDetailsComponent } from './stock-transfer-details/stock-transfer-details.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'products', redirectTo: '/products/products', pathMatch: 'full' },
      { path: 'categories', redirectTo: '/products/categories', pathMatch: 'full' },
      { path: 'brands', redirectTo: '/products/brands', pathMatch: 'full' },
      { path: 'uom', redirectTo: '/products/uom', pathMatch: 'full' },
      { path: 'uom-settings', redirectTo: '/products/uom', pathMatch: 'full' },
      { path: 'warehouse', redirectTo: 'warehouses', pathMatch: 'full' },
      { path: 'warehouses', component: WarehouseComponent, canActivate: [PermissionGuard], data: { permissions: ['INVENTORY_VIEW'], breadcrumb: 'Warehouses' } },
      { path: 'stocks', component: StockLevelComponent, canActivate: [PermissionGuard], data: { permissions: ['INVENTORY_VIEW'], breadcrumb: 'Stock Levels' } },
      { path: 'stock-card', component: StockCardComponent, canActivate: [PermissionGuard], data: { permissions: ['INVENTORY_VIEW'], breadcrumb: 'Stock Card' } },
      { path: 'transfers', component: StockTransferListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['TRANSFER_VIEW', 'TRANSFER_EDIT'], breadcrumb: 'Stock Transfers' } },
      { path: 'transfers/create', component: StockTransferFormComponent, canActivate: [PermissionGuard], data: { permissions: ['TRANSFER_CREATE'], breadcrumb: 'Create Transfer' } },
      { path: 'transfers/edit/:id', component: StockTransferFormComponent, canActivate: [PermissionGuard], data: { permissions: ['TRANSFER_EDIT'], breadcrumb: 'Edit Transfer' } },
      { path: 'transfers/details/:id', component: StockTransferDetailsComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['TRANSFER_VIEW', 'TRANSFER_EDIT'], breadcrumb: 'Details' } },
      { path: 'adjustments', component: StockAdjustmentComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['STOCK_ADJUSTMENT_CREATE', 'STOCK_ADJUSTMENT_VIEW'], breadcrumb: 'Stock Adjustment' } },
      { path: 'movements', component: StockMovementComponent, canActivate: [PermissionGuard], data: { permissions: ['INVENTORY_VIEW'], breadcrumb: 'Stock Movements' } },
      { path: '', redirectTo: 'stocks', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InventoryRoutingModule { }
