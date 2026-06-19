package com.sme.erp.purchase.service;

import com.sme.erp.purchase.dto.PurchaseReceiveDTO;
import com.sme.erp.purchase.dto.PurchaseOrderDTO;

import java.util.List;

public interface PurchaseOrderService {
    List<PurchaseOrderDTO> getAll();
    List<PurchaseOrderDTO> getUnpaidBySupplier(Long supplierId);
    PurchaseOrderDTO getById(Long id);
    PurchaseOrderDTO create(PurchaseOrderDTO dto);
    PurchaseOrderDTO update(Long id, PurchaseOrderDTO dto);
    PurchaseOrderDTO submit(Long id);
    PurchaseOrderDTO approve(Long id);
    PurchaseOrderDTO reject(Long id, String reason);
    PurchaseOrderDTO cancel(Long id);
    PurchaseOrderDTO receive(Long id, PurchaseReceiveDTO dto);
}
