package com.sme.erp.inventory.service;

import com.sme.erp.inventory.dto.WarehouseDTO;

import java.util.List;

public interface WarehouseService {

    WarehouseDTO save(WarehouseDTO dto);

    List<WarehouseDTO> getAll();

    WarehouseDTO getById(Long id);

    void delete(Long id);
}