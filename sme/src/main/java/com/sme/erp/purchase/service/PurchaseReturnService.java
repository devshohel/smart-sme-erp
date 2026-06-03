package com.sme.erp.purchase.service;

import com.sme.erp.purchase.dto.PurchaseReturnDTO;

import java.util.List;

public interface PurchaseReturnService {
    List<PurchaseReturnDTO> getAll();
    PurchaseReturnDTO getById(Long id);
    PurchaseReturnDTO create(PurchaseReturnDTO dto);
}
