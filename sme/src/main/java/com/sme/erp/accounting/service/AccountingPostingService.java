package com.sme.erp.accounting.service;

import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.entity.PurchaseReturn;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.entity.SalesReturn;

public interface AccountingPostingService {
    void postExpense(Expense expense);
    void reverseExpense(Expense expense, String reversalReason);
    void postSalesInvoice(SalesInvoice invoice);
    void reverseSalesInvoice(SalesInvoice invoice, String reversalReason);
    void postPurchase(PurchaseOrder purchaseOrder);
    void postSalesReturn(SalesReturn salesReturn);
    void postPurchaseReturn(PurchaseReturn purchaseReturn);
}
