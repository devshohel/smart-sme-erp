import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ProductListComponent } from './product-list/product-list.component';
import { CategoryListComponent } from './category-list/category-list.component';
import { UomSettingComponent } from './uom-setting/uom-setting.component';
import { StockLevelComponent } from './stock-level/stock-level.component';
import { WarehouseComponent } from './warehouse/warehouse.component';

const routes: Routes = [
  {
    path: '', // main inventory path
    children: [
      { path: 'products', component: ProductListComponent },
      { path: 'categories', component: CategoryListComponent },
      { path: 'brands', component: ProductListComponent }, // ব্র্যান্ড তৈরি হলে পরে পাল্টে দেবেন
      { path: 'uom-settings', component: UomSettingComponent },
      { path: 'warehouse', component: WarehouseComponent },
      { path: 'stocks', component: StockLevelComponent },
      { path: 'adjustments', component: StockLevelComponent },
      { path: '', redirectTo: 'products', pathMatch: 'full' } // ডিফল্টভাবে প্রোডাক্টে নিয়ে যাবে
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InventoryRoutingModule { }
