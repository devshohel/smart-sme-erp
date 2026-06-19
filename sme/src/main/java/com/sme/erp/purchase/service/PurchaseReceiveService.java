package com.sme.erp.purchase.service;

import com.sme.erp.purchase.dto.PurchaseReceiveDTO;

import java.util.List;

public interface PurchaseReceiveService {
    List<PurchaseReceiveDTO> getAll();
    PurchaseReceiveDTO getById(Long id);
}
