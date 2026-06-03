package com.sme.erp.purchase.service;

import com.sme.erp.purchase.dto.PurchaseOrderDTO;

import java.util.List;

public interface PurchaseOrderService {
    List<PurchaseOrderDTO> getAll();
    PurchaseOrderDTO getById(Long id);
    PurchaseOrderDTO create(PurchaseOrderDTO dto);
    PurchaseOrderDTO update(Long id, PurchaseOrderDTO dto);
}
