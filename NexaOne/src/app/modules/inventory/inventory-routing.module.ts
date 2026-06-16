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
      { path: 'stock-card', component: StockCardComponent },
      { path: 'transfers', component: StockTransferListComponent },
      { path: 'transfers/create', component: StockTransferFormComponent },
      { path: 'transfers/edit/:id', component: StockTransferFormComponent },
      { path: 'transfers/details/:id', component: StockTransferDetailsComponent },
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
