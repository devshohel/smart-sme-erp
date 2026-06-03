package com.sme.erp.customer.service;

import com.sme.erp.customer.dto.CustomerDTO;
import com.sme.erp.enums.Status;

import java.util.List;

public interface CustomerService {
    List<CustomerDTO> getAll(String keyword, Status status);
    CustomerDTO getById(Long id);
    CustomerDTO create(CustomerDTO dto);
    CustomerDTO update(Long id, CustomerDTO dto);
    void delete(Long id);
}
