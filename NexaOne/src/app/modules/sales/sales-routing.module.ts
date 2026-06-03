import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InvoicesComponent } from './invoices/invoices.component';
import { OrdersComponent } from './orders/orders.component';
import { ReturnsComponent } from './returns/returns.component';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'orders', component: OrdersComponent },
      { path: 'invoices', component: InvoicesComponent },
      { path: 'returns', component: ReturnsComponent },
      { path: '', redirectTo: 'orders', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SalesRoutingModule { }
