import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { InventoryRoutingModule } from './inventory-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { StockAdjustmentComponent } from './stock-adjustment/stock-adjustment.component';
import { StockLevelComponent } from './stock-level/stock-level.component';
import { StockMovementComponent } from './stock-movement/stock-movement.component';
import { WarehouseComponent } from './warehouse/warehouse.component';


@NgModule({
  declarations: [
    StockLevelComponent,
    StockMovementComponent,
    StockAdjustmentComponent,
    WarehouseComponent
  ],
  imports: [
    CommonModule,
    InventoryRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule
  ]
})
export class InventoryModule { }
