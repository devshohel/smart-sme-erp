import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CategoryListComponent } from './category-list/category-list.component';
import { AddProductComponent } from './add-product/add-product.component';
import { BrandListComponent } from './brand-list/brand-list.component';
import { EditProductComponent } from './edit-product/edit-product.component';
import { ProductListComponent } from './product-list/product-list.component';
import { ProductDetailsComponent } from './product-details/product-details.component';
import { ProductFormComponent } from './product-form/product-form.component';
import { UomSettingComponent } from './uom-setting/uom-setting.component';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'products', component: ProductListComponent },
      { path: 'add-product', component: AddProductComponent },
      { path: 'edit-product/:id', component: EditProductComponent },
      { path: 'details/:id', component: ProductDetailsComponent },
      { path: 'categories', component: CategoryListComponent },
      { path: 'brands', component: BrandListComponent },
      { path: 'uom', component: UomSettingComponent },
      { path: '', redirectTo: 'products', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProductsRoutingModule { }
