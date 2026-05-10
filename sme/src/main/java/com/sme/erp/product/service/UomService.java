package com.sme.erp.product.service;

import com.sme.erp.product.dto.UomDTO;

import java.util.List;

public interface UomService {

    UomDTO save(UomDTO dto);

    List<UomDTO> getAll();

    UomDTO getById(Long id);

    void delete(Long id);
}