import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PurchaseOrderComponent } from './purchase-order/purchase-order.component';
import { PurchaseReturnComponent } from './purchase-return/purchase-return.component';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'orders', component: PurchaseOrderComponent },
      { path: 'returns', component: PurchaseReturnComponent },
      { path: '', redirectTo: 'orders', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PurchasesRoutingModule { }
