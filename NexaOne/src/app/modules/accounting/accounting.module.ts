import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { AccountingRoutingModule } from './accounting-routing.module';
import { AccountingComponent } from './accounting/accounting.component';

@NgModule({
  declarations: [AccountingComponent],
  imports: [
    CommonModule,
    FormsModule,
    SharedModule,
    AccountingRoutingModule
  ]
})
export class AccountingModule {}
