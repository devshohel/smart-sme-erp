import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from '../../shared/shared.module';

import { ProductsRoutingModule } from './products-routing.module';

import { CategoryListComponent } from './category-list/category-list.component';
import { ProductListComponent } from './product-list/product-list.component';
import { UomSettingComponent } from './uom-setting/uom-setting.component';

@NgModule({
  declarations: [
    ProductListComponent,
    CategoryListComponent,
    UomSettingComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ProductsRoutingModule,
    SharedModule
  ]
})
export class ProductsModule { }