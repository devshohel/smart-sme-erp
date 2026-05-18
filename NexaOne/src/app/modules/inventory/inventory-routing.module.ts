import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { StockLevelComponent } from './stock-level/stock-level.component';
import { StockAdjustmentComponent } from './stock-adjustment/stock-adjustment.component';
import { StockMovementComponent } from './stock-movement/stock-movement.component';
import { WarehouseComponent } from './warehouse/warehouse.component';

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
      { path: 'warehouses', component: WarehouseComponent },
      { path: 'stocks', component: StockLevelComponent },
      { path: 'adjustments', component: StockAdjustmentComponent },
      { path: 'movements', component: StockMovementComponent },
      { path: '', redirectTo: 'stocks', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InventoryRoutingModule { }
