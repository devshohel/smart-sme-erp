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
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'products', component: ProductListComponent, canActivate: [PermissionGuard], data: { permissions: ['PRODUCT_VIEW'] } },
      { path: 'add-product', component: AddProductComponent, canActivate: [PermissionGuard], data: { permissions: ['PRODUCT_CREATE'] } },
      { path: 'edit-product/:id', component: EditProductComponent, canActivate: [PermissionGuard], data: { permissions: ['PRODUCT_EDIT'] } },
      { path: 'details/:id', component: ProductDetailsComponent, canActivate: [PermissionGuard], data: { permissions: ['PRODUCT_VIEW'] } },
      { path: 'categories', component: CategoryListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['CATEGORY_VIEW', 'PRODUCT_VIEW'] } },
      { path: 'brands', component: BrandListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['BRAND_VIEW', 'PRODUCT_VIEW'] } },
      { path: 'uom', component: UomSettingComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['UOM_VIEW', 'PRODUCT_VIEW'] } },
      { path: '', redirectTo: 'products', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProductsRoutingModule { }
