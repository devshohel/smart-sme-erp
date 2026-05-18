import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from '../../shared/shared.module';

import { ProductsRoutingModule } from './products-routing.module';

import { CategoryListComponent } from './category-list/category-list.component';
import { ProductListComponent } from './product-list/product-list.component';
import { UomSettingComponent } from './uom-setting/uom-setting.component';
import { AddProductComponent } from './add-product/add-product.component';
import { BrandListComponent } from './brand-list/brand-list.component';
import { ProductFormComponent } from './product-form/product-form.component';
import { EditProductComponent } from './edit-product/edit-product.component';
import { ProductDetailsComponent } from './product-details/product-details.component';

@NgModule({
  declarations: [
    ProductListComponent,
    CategoryListComponent,
    UomSettingComponent,
    AddProductComponent,
    BrandListComponent,
    ProductFormComponent,
    EditProductComponent,
    ProductDetailsComponent
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
