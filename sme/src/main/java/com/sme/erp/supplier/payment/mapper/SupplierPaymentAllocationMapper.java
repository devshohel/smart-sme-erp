package com.sme.erp.supplier.payment.mapper;

import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.supplier.payment.dto.SupplierPaymentAllocationDTO;
import com.sme.erp.supplier.payment.entity.SupplierPaymentAllocation;
import org.springframework.stereotype.Component;

@Component
public class SupplierPaymentAllocationMapper {
    public SupplierPaymentAllocationDTO toDTO(SupplierPaymentAllocation entity) {
        SupplierPaymentAllocationDTO dto = new SupplierPaymentAllocationDTO();
        dto.setId(entity.getId());
        dto.setAllocatedAmount(entity.getAllocatedAmount());
        PurchaseOrder purchase = entity.getPurchaseOrder();
        if (purchase != null) {
            dto.setPurchaseOrderId(purchase.getId());
            dto.setPurchaseCode(purchase.getPurchaseCode());
            dto.setPurchaseDueAmount(purchase.getDueAmount());
        }
        return dto;
    }
}
