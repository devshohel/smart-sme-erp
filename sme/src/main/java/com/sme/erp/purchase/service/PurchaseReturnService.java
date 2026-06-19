package com.sme.erp.purchase.service;

import com.sme.erp.purchase.dto.PurchaseReturnDTO;

import java.util.List;

public interface PurchaseReturnService {
    List<PurchaseReturnDTO> getAll();
    PurchaseReturnDTO getById(Long id);
    PurchaseReturnDTO create(PurchaseReturnDTO dto);
    PurchaseReturnDTO update(Long id, PurchaseReturnDTO dto);
    PurchaseReturnDTO submit(Long id);
    PurchaseReturnDTO approve(Long id);
    PurchaseReturnDTO reject(Long id, String reason);
    PurchaseReturnDTO post(Long id);
    PurchaseReturnDTO cancel(Long id);
}
