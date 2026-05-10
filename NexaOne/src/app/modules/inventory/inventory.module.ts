import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { InventoryRoutingModule } from './inventory-routing.module';
import { ProductListComponent } from './product-list/product-list.component';
import { CategoryListComponent } from './category-list/category-list.component';
import { UomSettingComponent } from './uom-setting/uom-setting.component';
import { StockLevelComponent } from './stock-level/stock-level.component';
import { WarehouseComponent } from './warehouse/warehouse.component';


@NgModule({
  declarations: [
    ProductListComponent,
    CategoryListComponent,
    UomSettingComponent,
    StockLevelComponent,
    WarehouseComponent
  ],
  imports: [
    CommonModule,
    InventoryRoutingModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class InventoryModule { }
